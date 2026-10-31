package Modelos;

import Conexion.BD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de clientes. Incluye el cálculo del saldo de puntos, el
 * número de compras y el total gastado para mostrarlos en el punto de venta.
 */
public class ClienteRepository {

    private static final String SELECT_BASE =
            "SELECT c.*, "
            + "DATEDIFF(NOW(), c.fecha_alta) AS antiguedad_dias, "
            + "COALESCE((SELECT SUM(pm.puntos) FROM puntos_movimientos pm "
            + "          WHERE pm.id_cliente = c.id_cliente AND pm.puntos > 0), 0) - "
            + "COALESCE((SELECT SUM(ABS(pm.puntos)) FROM puntos_movimientos pm "
            + "          WHERE pm.id_cliente = c.id_cliente AND pm.puntos < 0), 0) AS saldo_puntos, "
            + "COALESCE((SELECT COUNT(*) FROM ventas v WHERE v.id_cliente = c.id_cliente), 0) AS total_compras, "
            + "COALESCE((SELECT SUM(v.total) FROM ventas v WHERE v.id_cliente = c.id_cliente), 0) AS total_gastado "
            + "FROM clientes c ";

    public Cliente buscarPorTelefono(String telefono) {
        String sql = SELECT_BASE + "WHERE c.telefono = ? AND c.activo = 1";
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por teléfono: " + e.getMessage());
        }
        return null;
    }

    public Cliente buscarPorId(int idCliente) {
        String sql = SELECT_BASE + "WHERE c.id_cliente = ? AND c.activo = 1";
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
        }
        return null;
    }

    /** Devuelve todos los clientes activos con sus puntos y estadísticas. */
    public List<Cliente> listarActivos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE c.activo = 1 ORDER BY c.nombre";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    /** Busca por nombre o teléfono (coincidencia parcial). */
    public List<Cliente> buscar(String termino) {
        List<Cliente> lista = new ArrayList<>();
        String sql = SELECT_BASE
                + "WHERE c.activo = 1 AND (c.nombre LIKE ? OR c.telefono LIKE ?) "
                + "ORDER BY c.nombre";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String patron = "%" + termino + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
        }
        return lista;
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        return buscar(nombre);
    }

    public int crearCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, telefono, email, direccion, fecha_alta, activo) "
                + "VALUES (?, ?, ?, ?, NOW(), 1)";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getDireccion());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
        }
        return -1;
    }

    public Cliente obtenerClienteFicticio() {
        return buscarPorTelefono("5551234567");
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        cliente.setDireccion(rs.getString("direccion"));

        java.sql.Timestamp ts = rs.getTimestamp("fecha_alta");
        if (ts != null) {
            cliente.setFechaAlta(ts.toLocalDateTime());
        }

        cliente.setActivo(rs.getBoolean("activo"));
        cliente.setAntiguedadDias(rs.getInt("antiguedad_dias"));
        cliente.setSaldoPuntos(rs.getInt("saldo_puntos"));
        cliente.setTotalCompras(rs.getInt("total_compras"));
        cliente.setTotalGastado(rs.getDouble("total_gastado"));
        return cliente;
    }
}
