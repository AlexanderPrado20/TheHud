package Modelos;

import Conexion.BD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PuntosRepository {
    
    public static class ConfigPuntos {
        public double puntosPorPeso;
        public double valorPunto;
        public int minimoCanje;
        public double maxPorcentajeCanje;
    }
    
    public ConfigPuntos obtenerConfiguracion() {
        ConfigPuntos config = new ConfigPuntos();
        config.puntosPorPeso = 1.00;
        config.valorPunto = 1.00;
        config.minimoCanje = 100;
        config.maxPorcentajeCanje = 100.00;
        
        String sql = "SELECT * FROM puntos_config WHERE activo = 1 ORDER BY id_config DESC LIMIT 1";
        
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                config.puntosPorPeso = rs.getDouble("puntos_por_peso");
                config.valorPunto = rs.getDouble("valor_punto");
                config.minimoCanje = rs.getInt("minimo_canje");
                config.maxPorcentajeCanje = rs.getDouble("max_porcentaje_canje");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuración de puntos: " + e.getMessage());
        }
        return config;
    }
    
    public int calcularPuntosGanados(double subtotalNeto) {
        ConfigPuntos config = obtenerConfiguracion();
        if (subtotalNeto <= 0) return 0;
        return (int) Math.floor(subtotalNeto * config.puntosPorPeso);
    }
    
    public double calcularDescuentoMaximo(double totalVenta) {
        ConfigPuntos config = obtenerConfiguracion();
        return totalVenta * (config.maxPorcentajeCanje / 100.00);
    }
    
    public ResultadoCanje calcularDescuentoPorPuntos(int puntosAUsar, int saldoDisponible, double totalVenta) {
        ResultadoCanje res = new ResultadoCanje();
        res.puntosUsados = 0;
        res.descuento = 0.00;
        res.valido = false;
        res.mensaje = "";
        
        ConfigPuntos config = obtenerConfiguracion();
        
        if (puntosAUsar <= 0) {
            res.mensaje = "Ingresa puntos a canjear";
            return res;
        }
        
        if (puntosAUsar < config.minimoCanje) {
            res.mensaje = "Mínimo para canjear: " + config.minimoCanje + " puntos";
            return res;
        }
        
        if (puntosAUsar > saldoDisponible) {
            res.mensaje = "No tienes suficientes puntos. Saldo: " + saldoDisponible;
            return res;
        }
        
        double descuentoCalculado = puntosAUsar * config.valorPunto;
        double descuentoMaximo = calcularDescuentoMaximo(totalVenta);
        
        if (descuentoCalculado > descuentoMaximo) {
            int puntosMaximos = (int) Math.floor(descuentoMaximo / config.valorPunto);
            res.mensaje = "Solo puedes canjear " + puntosMaximos + " puntos para este total (máx " + config.maxPorcentajeCanje + "%)";
            return res;
        }
        
        res.puntosUsados = puntosAUsar;
        res.descuento = descuentoCalculado;
        res.valido = true;
        res.mensaje = "Canje válido";
        return res;
    }
    
    public boolean registrarVentaConPuntos(Connection con, int idVenta, int idCliente, 
                                         int puntosUsados, double descuentoPuntos, int puntosGanados,
                                         int idUsuario) throws SQLException {
        
        if (idCliente <= 0) {
            return true;
        }
        
        ConfigPuntos config = obtenerConfiguracion();
        
        // Actualizar venta con datos de puntos
        String sqlUpdateVenta = "UPDATE ventas SET id_cliente = ?, puntos_usados = ?, descuento_puntos = ?, puntos_ganados = ? WHERE id_venta = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlUpdateVenta)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, puntosUsados);
            ps.setDouble(3, descuentoPuntos);
            ps.setInt(4, puntosGanados);
            ps.setInt(5, idVenta);
            ps.executeUpdate();
        }
        
        // Obtener saldo actual
        String sqlSaldo = "SELECT " +
                          "COALESCE(SUM(CASE WHEN puntos > 0 THEN puntos ELSE 0 END), 0) - " +
                          "COALESCE(SUM(ABS(puntos)) FILTER (WHERE puntos < 0), 0) AS saldo " +
                          "FROM puntos_movimientos WHERE id_cliente = ?";
        // Alternativa compatible con MySQL
        sqlSaldo = "SELECT " +
                   "(SELECT COALESCE(SUM(puntos), 0) FROM puntos_movimientos WHERE id_cliente = ? AND puntos > 0) - " +
                   "(SELECT COALESCE(SUM(ABS(puntos)), 0) FROM puntos_movimientos WHERE id_cliente = ? AND puntos < 0) AS saldo";
        
        int saldoActual = 0;
        try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, idCliente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                saldoActual = rs.getInt("saldo");
            }
        }
        
        // Registrar uso de puntos si aplica
        if (puntosUsados > 0) {
            int saldoNuevo = saldoActual - puntosUsados;
            String sqlUso = "INSERT INTO puntos_movimientos (id_cliente, id_venta, tipo, puntos, saldo_anterior, saldo_nuevo, motivo, id_usuario) VALUES (?, ?, 'USADO', ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlUso)) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idVenta);
                ps.setInt(3, -puntosUsados);
                ps.setInt(4, saldoActual);
                ps.setInt(5, saldoNuevo);
                ps.setString(6, "Canje de puntos en compra");
                ps.setObject(7, idUsuario);
                ps.executeUpdate();
            }
            saldoActual = saldoNuevo;
        }
        
        // Registrar ganancia de puntos
        if (puntosGanados > 0) {
            int saldoNuevo = saldoActual + puntosGanados;
            String sqlGana = "INSERT INTO puntos_movimientos (id_cliente, id_venta, tipo, puntos, saldo_anterior, saldo_nuevo, motivo, id_usuario) VALUES (?, ?, 'GANADO', ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlGana)) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idVenta);
                ps.setInt(3, puntosGanados);
                ps.setInt(4, saldoActual);
                ps.setInt(5, saldoNuevo);
                ps.setString(6, "Puntos ganados por compra");
                ps.setObject(7, idUsuario);
                ps.executeUpdate();
            }
        }
        
        return true;
    }
    
    public static class ResultadoCanje {
        public boolean valido;
        public int puntosUsados;
        public double descuento;
        public String mensaje;
    }
}
