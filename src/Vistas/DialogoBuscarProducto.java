package Vistas;

import Conexion.BD;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Diálogo de búsqueda de productos con cuadrícula de tarjetas (imagen, nombre,
 * precio, stock y botón "Agregar").
 */
public class DialogoBuscarProducto extends JDialog {

    private final Consumer<String> alAgregar;

    private final JTextField txtBuscar = new JTextField();
    private final JPanel panelResultados = new JPanel(new WrapLayout(java.awt.FlowLayout.LEFT, 12, 12));
    private final JLabel lblEstado = new JLabel(" ");

    private static final Color FONDO = new Color(43, 43, 43);
    private static final Color TARJETA = new Color(52, 55, 60);
    private static final Color BORDE_TARJETA = new Color(74, 78, 84);
    private static final Color ACENTO = new Color(14, 131, 136);

    public DialogoBuscarProducto(Frame owner, Consumer<String> alAgregar) {
        super(owner, "Buscar producto", true);
        this.alAgregar = alAgregar;

        construirUI();
        cargarProductos("");
    }

    private void construirUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(940, 620);
        setLocationRelativeTo(getOwner());

        JPanel raiz = new JPanel(new BorderLayout(0, 10));
        raiz.setBackground(FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(raiz);

        // Barra superior: buscador
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);

        JLabel lblIcono = new JLabel(FontIcon.of(MaterialDesign.MDI_MAGNIFY, 18, new Color(170, 170, 170)));

        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Escribe el nombre, marca o descripción...");
        txtBuscar.addActionListener(e -> cargarProductos(txtBuscar.getText()));

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setIcon(FontIcon.of(MaterialDesign.MDI_MAGNIFY, 16, Color.WHITE));
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBuscar.setBackground(ACENTO);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setOpaque(true);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> cargarProductos(txtBuscar.getText()));

        JPanel panelCampo = new JPanel(new BorderLayout(6, 0));
        panelCampo.setOpaque(false);
        panelCampo.add(lblIcono, BorderLayout.WEST);
        panelCampo.add(txtBuscar, BorderLayout.CENTER);

        barra.add(panelCampo, BorderLayout.CENTER);
        barra.add(btnBuscar, BorderLayout.EAST);
        raiz.add(barra, BorderLayout.NORTH);

        // Cuadrícula de tarjetas
        panelResultados.setBackground(FONDO);

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE_TARJETA, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(FONDO);
        raiz.add(scroll, BorderLayout.CENTER);

        // Pie
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        lblEstado.setForeground(new Color(180, 180, 180));
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setIcon(FontIcon.of(MaterialDesign.MDI_CLOSE, 16, Color.WHITE));
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setBackground(new Color(80, 84, 88));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setOpaque(true);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        pie.add(lblEstado, BorderLayout.WEST);
        pie.add(btnCerrar, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnBuscar);
    }

    private void cargarProductos(String termino) {
        panelResultados.removeAll();

        String sql = "SELECT codigo_barras, nombre, precio_venta, stock FROM productos "
                + "WHERE nombre LIKE ? OR descripcion LIKE ? OR marca LIKE ? "
                + "ORDER BY nombre LIMIT 200";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + (termino == null ? "" : termino.trim()) + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    String codigo = rs.getString("codigo_barras");
                    String nombre = rs.getString("nombre");
                    double precio = rs.getDouble("precio_venta");
                    int stock = rs.getInt("stock");
                    panelResultados.add(crearTarjeta(codigo, nombre, precio, stock));
                    contador++;
                }
                lblEstado.setText(contador + " producto(s) encontrado(s).");
                if (contador == 0) {
                    JLabel vacio = new JLabel("No se encontraron productos con ese criterio.");
                    vacio.setForeground(new Color(200, 120, 120));
                    vacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    panelResultados.add(vacio);
                }
            }
        } catch (SQLException e) {
            lblEstado.setText("Error al buscar: " + e.getMessage());
        }

        panelResultados.revalidate();
        panelResultados.repaint();
        txtBuscar.requestFocusInWindow();
    }

    private JPanel crearTarjeta(String codigo, String nombre, double precio, int stock) {
        JPanel tarjeta = new JPanel(new BorderLayout(6, 6));
        tarjeta.setPreferredSize(new Dimension(184, 262));
        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_TARJETA, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setPreferredSize(new Dimension(166, 110));
        ImageIcon icono = cargarImagen(codigo, 150, 100);
        if (icono != null) {
            lblImagen.setIcon(icono);
        } else {
            lblImagen.setText("Sin imagen");
            lblImagen.setForeground(Color.GRAY);
            lblImagen.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        }
        tarjeta.add(lblImagen, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel("<html><div style='width:158px'>" + escaparHtml(nombre) + "</div></html>");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrecio = new JLabel(String.format("$ %.2f", precio));
        lblPrecio.setForeground(new Color(0, 230, 118));
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblStock = new JLabel("Stock: " + stock);
        lblStock.setForeground(stock > 0 ? new Color(180, 180, 180) : new Color(255, 120, 120));
        lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStock.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblNombre);
        info.add(Box.createVerticalStrut(4));
        info.add(lblPrecio);
        info.add(Box.createVerticalStrut(2));
        info.add(lblStock);
        tarjeta.add(info, BorderLayout.CENTER);

        JButton btnAgregar = new JButton(stock > 0 ? "Agregar" : "Sin stock");
        btnAgregar.setIcon(FontIcon.of(MaterialDesign.MDI_CART_PLUS, 16, Color.WHITE));
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAgregar.setBackground(ACENTO);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setBorderPainted(false);
        btnAgregar.setOpaque(true);
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregar.setEnabled(stock > 0);
        btnAgregar.addActionListener(e -> {
            if (alAgregar != null) {
                alAgregar.accept(codigo);
                lblEstado.setText("Agregado: " + nombre);
            }
        });
        tarjeta.add(btnAgregar, BorderLayout.SOUTH);

        return tarjeta;
    }

    private ImageIcon cargarImagen(String codigo, int ancho, int alto) {
        if (codigo == null) {
            return null;
        }
        String[] extensiones = {".png", ".jpg", ".jpeg"};
        for (String ext : extensiones) {
            java.net.URL url = getClass().getResource("/Imagenes/" + codigo.trim() + ext);
            if (url != null) {
                ImageIcon original = new ImageIcon(url);
                Image escalada = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                return new ImageIcon(escalada);
            }
        }
        return null;
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
