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
}
