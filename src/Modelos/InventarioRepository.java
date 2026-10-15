package Modelos;

import Conexion.BD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Capa de datos del tablero de Inventario.
 *
 * Centraliza todas las consultas SQL para que {@code PanelInventario} s\u00f3lo se
 * encargue de dibujar. Abre una sola conexi\u00f3n por refresco y, si algo falla,
 * devuelve la estructura vac\u00eda en lugar de lanzar excepciones (as\u00ed el panel
 * nunca se rompe aunque la base de datos no est\u00e9 disponible).
 */
public class InventarioRepository {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    /** D\u00edas que muestra la gr\u00e1fica de ingresos. */
    public static final int DIAS_INGRESOS = 7;
    /** M\u00e1ximo de filas en la tabla de reabastecimiento. */
    public static final int MAX_REABASTECIMIENTO = 50;
    /** Barras en la gr\u00e1fica de menor stock. */
    public static final int MAX_MENOR_STOCK = 8;
    /** Barras en el top de productos m\u00e1s vendidos. */
    public static final int MAX_TOP_VENDIDOS = 5;
    /** Barras en la gr\u00e1fica de margen. */
    public static final int MAX_MARGENES = 6;

    /** M\u00e9tricas generales del inventario. */
    public static class KpisInventario {
        public int skus;
        public long unidades;
        public double valorCosto;
        public double valorVenta;
        public int agotados;
        public int bajos;
        public double margenPromedio;
    }

    /** Fila de producto usada en tablas y gr\u00e1ficas comparativas. */
    public static class ItemStock {
        public String codigo;
        public String nombre;
        public String marca;
        public int stock;
        public int faltante;
        public double costo;
        public double precio;
        public double margen;
        public int unidades;

        public ItemStock(String codigo, String nombre, String marca, int stock) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.marca = marca;
            this.stock = stock;
        }
    }

    /** Todo lo que el panel necesita en un solo refresco. */
    public static class ResumenInventario {
        public final KpisInventario kpis = new KpisInventario();
        public final Map<String, Integer> distribucion = new LinkedHashMap<>();
        public final List<ItemStock> reabastecimiento = new ArrayList<>();
        public final List<ItemStock> menorStock = new ArrayList<>();
        public final List<ItemStock> topVendidos = new ArrayList<>();
        public final Map<String, Double> ingresosDias = new LinkedHashMap<>();
        public final Map<String, Double> porMetodo = new LinkedHashMap<>();
        public final List<ItemStock> margenes = new ArrayList<>();
    }

    /**
     * Carga todos los datos del tablero con una sola conexi\u00f3n.
     *
     * @param umbral productos con stock menor o igual se consideran "bajos"
     */
    public ResumenInventario cargar(int umbral) {
        ResumenInventario r = new ResumenInventario();

        // Valores por defecto (por si no hay base de datos).
        r.distribucion.put("Agotado", 0);
        r.distribucion.put("Bajo", 0);
        r.distribucion.put("\u00d3ptimo", 0);
        LocalDate hoy = LocalDate.now();
        for (int i = DIAS_INGRESOS - 1; i >= 0; i--) {
            r.ingresosDias.put(FORMATO_DIA.format(hoy.minusDays(i)), 0.0);
        }

        Connection con = BD.getConexion();
        if (con == null) {
            return r;
        }
        try (con) {
            cargarKpis(con, umbral, r);
            cargarDistribucion(con, umbral, r);
            cargarReabastecimiento(con, umbral, r);
            cargarMenorStock(con, r);
            cargarTopVendidos(con, r);
            cargarIngresos(con, r);
            cargarVentasPorMetodo(con, r);
            cargarMargenes(con, r);
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargar: " + e.getMessage());
        }
        return r;
    }

    private void cargarKpis(Connection con, int umbral, ResumenInventario r) {
        String sql = "SELECT COUNT(*) AS skus, "
                + " COALESCE(SUM(stock), 0) AS unidades, "
                + " COALESCE(SUM(stock * costo_compra), 0) AS valor_costo, "
                + " COALESCE(SUM(stock * precio_venta), 0) AS valor_venta, "
                + " COALESCE(SUM(CASE WHEN stock <= 0 THEN 1 ELSE 0 END), 0) AS agotados, "
                + " COALESCE(SUM(CASE WHEN stock > 0 AND stock <= ? THEN 1 ELSE 0 END), 0) AS bajos, "
                + " COALESCE(AVG(CASE WHEN precio_venta > 0 "
                + "        THEN (precio_venta - costo_compra) / precio_venta * 100 END), 0) AS margen "
                + "FROM productos";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KpisInventario k = r.kpis;
                    k.skus = rs.getInt("skus");
                    k.unidades = rs.getLong("unidades");
                    k.valorCosto = rs.getDouble("valor_costo");
                    k.valorVenta = rs.getDouble("valor_venta");
                    k.agotados = rs.getInt("agotados");
                    k.bajos = rs.getInt("bajos");
                    k.margenPromedio = rs.getDouble("margen");
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarKpis: " + e.getMessage());
        }
    }

    private void cargarDistribucion(Connection con, int umbral, ResumenInventario r) {
        String sql = "SELECT "
                + " COALESCE(SUM(CASE WHEN stock <= 0 THEN 1 ELSE 0 END), 0) AS agotado, "
                + " COALESCE(SUM(CASE WHEN stock > 0 AND stock <= ? THEN 1 ELSE 0 END), 0) AS bajo, "
                + " COALESCE(SUM(CASE WHEN stock > ? THEN 1 ELSE 0 END), 0) AS optimo "
                + "FROM productos";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            ps.setInt(2, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.distribucion.put("Agotado", rs.getInt("agotado"));
                    r.distribucion.put("Bajo", rs.getInt("bajo"));
                    r.distribucion.put("\u00d3ptimo", rs.getInt("optimo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarDistribucion: " + e.getMessage());
        }
    }

    private void cargarReabastecimiento(Connection con, int umbral, ResumenInventario r) {
        String sql = "SELECT codigo_barras, nombre, marca, stock, costo_compra, precio_venta "
                + "FROM productos WHERE stock <= ? ORDER BY stock ASC, nombre ASC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            ps.setInt(2, MAX_REABASTECIMIENTO);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemStock it = new ItemStock(
                            rs.getString("codigo_barras"),
                            rs.getString("nombre"),
                            rs.getString("marca"),
                            rs.getInt("stock"));
                    it.faltante = Math.max(1, umbral - it.stock);
                    it.costo = rs.getDouble("costo_compra");
                    it.precio = rs.getDouble("precio_venta");
                    it.margen = it.precio - it.costo;
                    r.reabastecimiento.add(it);
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarReabastecimiento: " + e.getMessage());
        }
    }

    private void cargarMenorStock(Connection con, ResumenInventario r) {
        String sql = "SELECT codigo_barras, nombre, marca, stock, costo_compra, precio_venta "
                + "FROM productos ORDER BY stock ASC, nombre ASC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, MAX_MENOR_STOCK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemStock it = new ItemStock(
                            rs.getString("codigo_barras"),
                            rs.getString("nombre"),
                            rs.getString("marca"),
                            rs.getInt("stock"));
                    it.costo = rs.getDouble("costo_compra");
                    it.precio = rs.getDouble("precio_venta");
                    it.margen = it.precio - it.costo;
                    r.menorStock.add(it);
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarMenorStock: " + e.getMessage());
        }
    }

    private void cargarTopVendidos(Connection con, ResumenInventario r) {
        String sql = "SELECT p.nombre, p.marca, SUM(d.cantidad) AS unidades "
                + "FROM detalle_ventas d JOIN productos p ON p.id_producto = d.id_producto "
                + "GROUP BY p.id_producto, p.nombre, p.marca ORDER BY unidades DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, MAX_TOP_VENDIDOS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemStock it = new ItemStock(null, rs.getString("nombre"), rs.getString("marca"), 0);
                    it.unidades = rs.getInt("unidades");
                    r.topVendidos.add(it);
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarTopVendidos: " + e.getMessage());
        }
    }

    private void cargarIngresos(Connection con, ResumenInventario r) {
        String sql = "SELECT DATE(fecha_hora) AS dia, SUM(total) AS monto FROM ventas "
                + "WHERE fecha_hora >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY DATE(fecha_hora)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, DIAS_INGRESOS - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date dia = rs.getDate("dia");
                    if (dia == null) {
                        continue;
                    }
                    String clave = FORMATO_DIA.format(dia.toLocalDate());
                    if (r.ingresosDias.containsKey(clave)) {
                        r.ingresosDias.put(clave, rs.getDouble("monto"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarIngresos: " + e.getMessage());
        }
    }

    private void cargarVentasPorMetodo(Connection con, ResumenInventario r) {
        String sql = "SELECT metodo_pago, SUM(total) AS monto FROM ventas "
                + "GROUP BY metodo_pago ORDER BY monto DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                r.porMetodo.put(rs.getString("metodo_pago"), rs.getDouble("monto"));
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarVentasPorMetodo: " + e.getMessage());
        }
    }

    private void cargarMargenes(Connection con, ResumenInventario r) {
        String sql = "SELECT nombre, marca, stock, costo_compra, precio_venta, "
                + "(precio_venta - costo_compra) AS margen FROM productos "
                + "ORDER BY margen DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, MAX_MARGENES);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemStock it = new ItemStock(null,
                            rs.getString("nombre"),
                            rs.getString("marca"),
                            rs.getInt("stock"));
                    it.costo = rs.getDouble("costo_compra");
                    it.precio = rs.getDouble("precio_venta");
                    it.margen = rs.getDouble("margen");
                    r.margenes.add(it);
                }
            }
        } catch (SQLException e) {
            System.err.println("InventarioRepository.cargarMargenes: " + e.getMessage());
        }
    }
}
