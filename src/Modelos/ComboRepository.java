package Modelos;

import Conexion.BD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Capa de datos de los combos y promociones.
 *
 * Centraliza la consulta a la tabla {@code combos} para que {@code PanelCombos}
 * solo se encargue de dibujar. Si la base de datos no esta disponible devuelve
 * una lista vacia en lugar de lanzar una excepcion.
 */
public class ComboRepository {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Un combo o promocion de la tienda. */
    public static class Combo {
        public int id;
        public String nombre;
        public String descripcion;
        public double precio;
        public LocalDate inicio;
        public LocalDate fin;
        public boolean activo;
        public boolean vigente;

        /** Texto de vigencia listo para mostrar en la tarjeta. */
        public String etiquetaVigencia() {
            if (!activo) {
                return "Inactivo";
            }
            LocalDate hoy = LocalDate.now();
            if (inicio == null || fin == null) {
                return "Sin vigencia";
            }
            if (hoy.isBefore(inicio)) {
                return "Inicia el " + FECHA.format(inicio);
            }
            if (hoy.isAfter(fin)) {
                return "Venci\u00f3 el " + FECHA.format(fin);
            }
            return "Vigente hasta el " + FECHA.format(fin);
        }
    }

    /**
     * Devuelve todos los combos registrados. El campo {@code vigente} indica si
     * hoy esta dentro de la vigencia y el combo esta activo.
     */
    public List<Combo> cargar() {
        List<Combo> lista = new ArrayList<>();
        Connection con = BD.getConexion();
        if (con == null) {
            return lista;
        }
        String sql = "SELECT id_combo, nombre, descripcion, precio, fecha_inicio, fecha_fin, estado "
                + "FROM combos ORDER BY estado DESC, nombre";
        try (con;
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            LocalDate hoy = LocalDate.now();
            while (rs.next()) {
                Combo c = new Combo();
                c.id = rs.getInt("id_combo");
                c.nombre = rs.getString("nombre");
                c.descripcion = rs.getString("descripcion");
                c.precio = rs.getDouble("precio");

                java.sql.Date ini = rs.getDate("fecha_inicio");
                java.sql.Date f = rs.getDate("fecha_fin");
                c.inicio = (ini == null) ? null : ini.toLocalDate();
                c.fin = (f == null) ? null : f.toLocalDate();

                c.activo = rs.getInt("estado") == 1;
                c.vigente = c.activo && c.inicio != null && c.fin != null
                        && !hoy.isBefore(c.inicio) && !hoy.isAfter(c.fin);
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("ComboRepository.cargar: " + e.getMessage());
        }
        return lista;
    }

    /** Producto del inventario disponible para armar un combo. */
    public static class ProductoInventario {
        public int id;
        public String codigo;
        public String nombre;
        public String marca;
        public double precio;
        public int stock;
    }

    /** Producto incluido dentro de un combo, con su cantidad. */
    public static class Detalle {
        public int idProducto;
        public String nombre;
        public int cantidad;

        public Detalle() {
        }

        public Detalle(int idProducto, String nombre, int cantidad) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.cantidad = cantidad;
        }
    }

    /**
     * Busca productos del inventario para el selector de combos.
     *
     * @param filtro texto libre (nombre, marca o codigo); vacio devuelve todo.
     */
    public List<ProductoInventario> cargarProductos(String filtro) {
        List<ProductoInventario> lista = new ArrayList<>();
        Connection con = BD.getConexion();
        if (con == null) {
            return lista;
        }
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        String sql = "SELECT id_producto, codigo_barras, nombre, marca, precio_venta, stock "
                + "FROM productos "
                + "WHERE nombre LIKE ? OR marca LIKE ? OR codigo_barras LIKE ? "
                + "ORDER BY nombre";
        try (con;
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoInventario p = new ProductoInventario();
                    p.id = rs.getInt("id_producto");
                    p.codigo = rs.getString("codigo_barras");
                    p.nombre = rs.getString("nombre");
                    p.marca = rs.getString("marca");
                    p.precio = rs.getDouble("precio_venta");
                    p.stock = rs.getInt("stock");
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("ComboRepository.cargarProductos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Guarda un combo nuevo junto con los productos que lo componen.
     *
     * @return id del combo creado, o -1 si hubo error.
     */
    public int guardar(String nombre, String descripcion, double precio,
            LocalDate inicio, LocalDate fin, boolean activo, List<Detalle> detalles) {
        Connection con = BD.getConexion();
        if (con == null) {
            return -1;
        }
        try {
            con.setAutoCommit(false);

            String sql = "INSERT INTO combos (nombre, descripcion, precio, fecha_inicio, fecha_fin, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, descripcion);
                ps.setDouble(3, precio);
                ps.setDate(4, inicio == null ? null : java.sql.Date.valueOf(inicio));
                ps.setDate(5, fin == null ? null : java.sql.Date.valueOf(fin));
                ps.setInt(6, activo ? 1 : 0);
                ps.executeUpdate();

                int idCombo;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        return -1;
                    }
                    idCombo = rs.getInt(1);
                }

                String sqlDetalle = "INSERT INTO combo_detalle (id_combo, id_producto, cantidad) VALUES (?, ?, ?)";
                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                    for (Detalle d : detalles) {
                        psDet.setInt(1, idCombo);
                        psDet.setInt(2, d.idProducto);
                        psDet.setInt(3, d.cantidad);
                        psDet.addBatch();
                    }
                    psDet.executeBatch();
                }

                con.commit();
                return idCombo;
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("ComboRepository.guardar: " + e.getMessage());
            return -1;
        } finally {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /** Devuelve los productos que componen un combo. */
    public List<Detalle> cargarDetalle(int idCombo) {
        List<Detalle> lista = new ArrayList<>();
        Connection con = BD.getConexion();
        if (con == null) {
            return lista;
        }
        String sql = "SELECT cd.id_producto, p.nombre, cd.cantidad "
                + "FROM combo_detalle cd "
                + "JOIN productos p ON p.id_producto = cd.id_producto "
                + "WHERE cd.id_combo = ? ORDER BY p.nombre";
        try (con;
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCombo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Detalle(rs.getInt("id_producto"),
                            rs.getString("nombre"), rs.getInt("cantidad")));
                }
            }
        } catch (SQLException e) {
            System.err.println("ComboRepository.cargarDetalle: " + e.getMessage());
        }
        return lista;
    }
}
