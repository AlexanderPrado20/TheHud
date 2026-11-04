package Vistas;

import Conexion.BD;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Reporte de ventas en formato de hoja de cálculo (tablas) con una lata
 * "Monster" que se llena según la meta diaria.
 *
 * <p>Incluye tres vistas: ventas de hoy por hora, del mes por día y el
 * histórico general por mes. Al seleccionar una fila, el panel inferior
 * muestra el desglose de productos vendidos (fecha, cliente, producto,
 * marca, cantidad, precio, subtotal). Se refresca automáticamente cada
 * 30 segundos mientras el panel está visible.</p>
 */
public class PanelReporteVentas extends javax.swing.JPanel {

    // ---- Paleta ----
    private static final Color FONDO = new Color(43, 43, 43);
    private static final Color TARJETA = new Color(48, 52, 56);
    private static final Color BORDE = new Color(66, 71, 76);
    private static final Color TEXTO = new Color(236, 240, 241);
    private static final Color TEXTO_SUAVE = new Color(158, 166, 174);
    private static final Color ACENTO = new Color(14, 131, 136);
    private static final Color VERDE = new Color(0, 200, 120);

    private final Preferences prefs = Preferences.userNodeForPackage(PanelReporteVentas.class);
    private double metaDiaria = 2000;

    private final JTabbedPane tabs = new JTabbedPane();
    private final JTable[] tablas = new JTable[3];
    private final DefaultTableModel[] modelos = new DefaultTableModel[3];
    private final JLabel[] resumenes = new JLabel[3];

    private JTable tablaDetalle;
    private DefaultTableModel modeloDetalle;
    private JLabel lblDetalleResumen;

    private final JTextField txtMeta = new JTextField(8);
    private final JLabel lblActualizado = new JLabel("--:--:--");
    private final MonsterCan lata = new MonsterCan();

    private final Timer autoRefresh;

    public PanelReporteVentas() {
        setLayout(new BorderLayout());
        setBackground(FONDO);

        construirInterfaz();
        anadirListenersTablas();
        cargarMeta();
        refrescar();

        autoRefresh = new Timer(30_000, e -> refrescar());
        autoRefresh.start();

        // Detener el refresco cuando el panel no se está mostrando.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                autoRefresh.start();
                refrescar();
            }

            @Override
            public void componentHidden(java.awt.event.ComponentEvent e) {
                autoRefresh.stop();
            }
        });
    }

    // =========================================================
    //  Interfaz
    // =========================================================

    private void construirInterfaz() {
        add(crearCabecera(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setBackground(new Color(33, 37, 41));
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel titulo = new JLabel("Reporte de ventas");
        titulo.setIcon(FontIcon.of(MaterialDesign.MDI_FILE_CHART, 26, ACENTO));
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitulo = new JLabel("Ventas del día, del mes e histórico general");
        subtitulo.setForeground(TEXTO_SUAVE);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        JLabel lblMeta = new JLabel("Meta diaria $");
        lblMeta.setForeground(TEXTO_SUAVE);
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtMeta.setBackground(new Color(38, 41, 45));
        txtMeta.setForeground(TEXTO);
        txtMeta.setCaretColor(TEXTO);
        txtMeta.setHorizontalAlignment(SwingConstants.RIGHT);
        txtMeta.setPreferredSize(new Dimension(90, 30));
        txtMeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        txtMeta.addActionListener(e -> aplicarMeta());

        JButton btnMeta = new JButton("Aplicar");
        estilizarBoton(btnMeta, ACENTO);
        btnMeta.addActionListener(e -> aplicarMeta());

        JButton btnExportar = new JButton("Exportar CSV");
        btnExportar.setIcon(FontIcon.of(MaterialDesign.MDI_FILE_EXPORT, 15, Color.WHITE));
        estilizarBoton(btnExportar, new Color(90, 94, 98));
        btnExportar.addActionListener(e -> exportarCsv());

        JButton btnExportarDetalle = new JButton("Exportar detalle");
        btnExportarDetalle.setIcon(FontIcon.of(MaterialDesign.MDI_DOWNLOAD, 15, Color.WHITE));
        estilizarBoton(btnExportarDetalle, new Color(90, 94, 98));
        btnExportarDetalle.addActionListener(e -> exportarDetalleCsv());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setIcon(FontIcon.of(MaterialDesign.MDI_REFRESH, 15, Color.WHITE));
        estilizarBoton(btnActualizar, new Color(0, 150, 100));
        btnActualizar.addActionListener(e -> refrescar());

        lblActualizado.setForeground(TEXTO_SUAVE);
        lblActualizado.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        acciones.add(lblMeta);
        acciones.add(txtMeta);
        acciones.add(btnMeta);
        acciones.add(btnExportar);
        acciones.add(btnExportarDetalle);
        acciones.add(btnActualizar);
        acciones.add(lblActualizado);

        cabecera.add(textos, BorderLayout.WEST);
        cabecera.add(acciones, BorderLayout.EAST);
        return cabecera;
    }

    private JComponent crearCuerpo() {
        // --- Panel superior: pestañas ---
        JPanel panPestanas = new JPanel(new BorderLayout());
        panPestanas.setBackground(FONDO);
        panPestanas.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        modelos[0] = crearModelo(new String[]{"Hora", "Ventas", "Total", "Ticket promedio"});
        modelos[1] = crearModelo(new String[]{"Fecha", "Ventas", "Total", "Ticket promedio"});
        modelos[2] = crearModelo(new String[]{"Periodo", "Ventas", "Total", "Ticket promedio"});

        resumenes[0] = crearResumen();
        resumenes[1] = crearResumen();
        resumenes[2] = crearResumen();

        tabs.addTab("Hoy", crearPanelTabla(modelos[0], resumenes[0], 0));
        tabs.addTab("Este mes", crearPanelTabla(modelos[1], resumenes[1], 1));
        tabs.addTab("General", crearPanelTabla(modelos[2], resumenes[2], 2));
        tabs.setBackground(TARJETA);
        tabs.setForeground(TEXTO);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.putClientProperty("JTabbedPane.tabHeight", 34);
        tabs.putClientProperty("JTabbedPane.tabSelectionHeight", 3);
        tabs.addChangeListener(e -> actualizarResumenPestana());

        panPestanas.add(tabs, BorderLayout.CENTER);

        // --- Panel inferior: detalle de la fila seleccionada ---
        modeloDetalle = new DefaultTableModel(
                new String[]{"Fecha", "Cliente", "Producto", "Marca", "Cantidad", "Precio", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaDetalle = new JTable(modeloDetalle);
        estilizarTablaDetalle(tablaDetalle);
        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.getViewport().setBackground(TARJETA);
        scrollDetalle.setBorder(BorderFactory.createLineBorder(BORDE));

        lblDetalleResumen = new JLabel("Selecciona una fila para ver el desglose");
        lblDetalleResumen.setForeground(TEXTO_SUAVE);
        lblDetalleResumen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetalleResumen.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));

        JPanel panelDetalle = new JPanel(new BorderLayout(4, 4));
        panelDetalle.setBackground(FONDO);
        panelDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panelDetalle.add(new JLabel("Desglose de venta"), BorderLayout.NORTH);
        panelDetalle.add(scrollDetalle, BorderLayout.CENTER);
        panelDetalle.add(lblDetalleResumen, BorderLayout.SOUTH);

        // --- Splitter vertical: 60% pestañas / 40% detalle ---
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panPestanas, panelDetalle);
        splitter.setOpaque(false);
        splitter.setBorder(null);
        splitter.setOneTouchExpandable(false);
        splitter.setDividerLocation(400);
        splitter.setResizeWeight(0.6);

        return splitter;
    }

    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private JLabel crearResumen() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(TEXTO_SUAVE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));
        return lbl;
    }

    private JPanel crearPanelTabla(DefaultTableModel modelo, JLabel resumen, int indice) {
        JTable tabla = new JTable(modelo);
        estilizarTabla(tabla);
        tablas[indice] = tabla;

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(TARJETA);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(resumen, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(TARJETA);
        lateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(14, 18, 16, 18)));
        lateral.setPreferredSize(new Dimension(230, 0));

        JLabel titulo = new JLabel("Meta del día");
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setIcon(FontIcon.of(MaterialDesign.MDI_FLAG_CHECKERED, 18, VERDE));
        titulo.setIconTextGap(8);

        lata.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pie = new JLabel("Se llena con las ventas de hoy");
        pie.setForeground(TEXTO_SUAVE);
        pie.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pie.setAlignmentX(Component.CENTER_ALIGNMENT);

        lateral.add(titulo);
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(lata);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(pie);
        lateral.add(Box.createVerticalGlue());
        return lateral;
    }

    private static void estilizarTabla(JTable tabla) {
        tabla.setBackground(TARJETA);
        tabla.setForeground(TEXTO);
        tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setRowHeight(28);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(new Color(0, 120, 100));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setBackground(new Color(33, 37, 41));
        tabla.getTableHeader().setForeground(TEXTO_SUAVE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centrada = new DefaultTableCellRenderer();
        centrada.setHorizontalAlignment(SwingConstants.CENTER);

        // Columnas: 0 texto, 1 centrada, 2 y 3 a la derecha.
        for (int i = 0; i < tabla.getColumnModel().getColumnCount(); i++) {
            if (i == 1) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(centrada);
            } else if (i >= 2) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(derecha);
            }
        }
        tabla.getColumnModel().getColumn(0).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
    }

    private static void estilizarTablaDetalle(JTable tabla) {
        tabla.setBackground(TARJETA);
        tabla.setForeground(TEXTO);
        tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(new Color(0, 120, 100));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setBackground(new Color(33, 37, 41));
        tabla.getTableHeader().setForeground(TEXTO_SUAVE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centrada = new DefaultTableCellRenderer();
        centrada.setHorizontalAlignment(SwingConstants.CENTER);

        // Columnas: 0 texto, 1 texto, 2 texto, 3 texto, 4 centrada, 5 derecha, 6 derecha.
        for (int i = 0; i < tabla.getColumnModel().getColumnCount(); i++) {
            if (i == 4) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(centrada);
            } else if (i >= 5) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(derecha);
            }
        }
        tabla.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(110);
    }

    private static void estilizarBoton(JButton boton, Color fondo) {
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // =========================================================
    //  Datos
    // =========================================================

    /** Recalcula todas las tablas y la lata de la meta diaria. */
    public void refrescar() {
        double totalHoy = consultarTotalHoy();
        double progreso = metaDiaria > 0 ? totalHoy / metaDiaria : 0;
        lata.setProgreso(progreso, String.format("$%,.2f / $%,.2f", totalHoy, metaDiaria));

        cargarHoy();
        cargarMes();
        cargarGeneral();
        actualizarResumenPestana();

        lblActualizado.setText("Actualizado " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void cargarHoy() {
        modelos[0].setRowCount(0);
        String sql = "SELECT DATE_FORMAT(fecha_hora, '%H:00') AS periodo, "
                + "COUNT(*) AS num, COALESCE(SUM(total),0) AS total, COALESCE(AVG(total),0) AS promedio "
                + "FROM ventas WHERE DATE(fecha_hora) = CURDATE() "
                + "GROUP BY DATE_FORMAT(fecha_hora, '%H:00') ORDER BY MIN(fecha_hora)";
        llenarModelo(0, sql, true);
    }

    private void cargarMes() {
        modelos[1].setRowCount(0);
        String sql = "SELECT DATE_FORMAT(fecha_hora, '%d/%m/%Y') AS periodo, "
                + "COUNT(*) AS num, COALESCE(SUM(total),0) AS total, COALESCE(AVG(total),0) AS promedio "
                + "FROM ventas WHERE YEAR(fecha_hora) = YEAR(CURDATE()) "
                + "AND MONTH(fecha_hora) = MONTH(CURDATE()) "
                + "GROUP BY DATE_FORMAT(fecha_hora, '%d/%m/%Y') ORDER BY MIN(fecha_hora)";
        llenarModelo(1, sql, true);
    }

    private void cargarGeneral() {
        modelos[2].setRowCount(0);
        String sql = "SELECT DATE_FORMAT(fecha_hora, '%Y-%m') AS periodo, "
                + "COUNT(*) AS num, COALESCE(SUM(total),0) AS total, COALESCE(AVG(total),0) AS promedio "
                + "FROM ventas GROUP BY DATE_FORMAT(fecha_hora, '%Y-%m') "
                + "ORDER BY MIN(fecha_hora)";
        llenarModelo(2, sql, false);
    }

    private void llenarModelo(int indice, String sql, boolean esPeriodoCorto) {
        DecimalFormat dinero = new DecimalFormat("$#,##0.00");
        double granTotal = 0;
        int granCantidad = 0;

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int num = rs.getInt("num");
                double total = rs.getDouble("total");
                double promedio = rs.getDouble("promedio");
                granTotal += total;
                granCantidad += num;
                modelos[indice].addRow(new Object[]{
                    rs.getString("periodo"), num, dinero.format(total), dinero.format(promedio)
                });
            }
        } catch (Exception e) {
            System.err.println("PanelReporteVentas: error al cargar datos: " + e.getMessage());
        }

        String etiqueta = indice == 0 ? "hoy" : (indice == 1 ? "este mes" : "en total");
        resumenes[indice].setText(String.format("%d venta(s) %s  ·  Total: $%,.2f  ·  Ticket promedio: $%,.2f",
                granCantidad, etiqueta, granTotal, granCantidad > 0 ? granTotal / granCantidad : 0));
    }

    private double consultarTotalHoy() {
        String sql = "SELECT COALESCE(SUM(total),0) AS total FROM ventas WHERE DATE(fecha_hora) = CURDATE()";
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            System.err.println("PanelReporteVentas: error al consultar total del día: " + e.getMessage());
        }
        return 0;
    }

    private void actualizarResumenPestana() {
        // Solo asegura que el resumen visible corresponda a la pestaña activa.
        int i = tabs.getSelectedIndex();
        if (i >= 0 && i < resumenes.length) {
            resumenes[i].repaint();
        }
    }

    // =========================================================
    //  Detalle de fila seleccionada
    // =========================================================

    /** Llena la tabla de detalle según la pestaña activa. */
    private void cargarDetalle(int indicePestana) {
        modeloDetalle.setRowCount(0);
        String sql = "";

        switch (indicePestana) {
            case 0: // Hoy
                sql = "SELECT v.fecha_hora, COALESCE(c.nombre,'Sin cliente') AS cliente, "
                        + "p.nombre AS producto, p.marca, "
                        + "d.cantidad, d.precio_unitario, d.subtotal "
                        + "FROM detalle_ventas d "
                        + "JOIN ventas v ON v.id_venta = d.id_venta "
                        + "LEFT JOIN clientes c ON c.id = v.id_cliente "
                        + "JOIN productos p ON p.id_producto = d.id_producto "
                        + "WHERE DATE(v.fecha_hora) = CURDATE() "
                        + "ORDER BY v.fecha_hora DESC";
                break;
            case 1: // Este mes
                sql = "SELECT v.fecha_hora, COALESCE(c.nombre,'Sin cliente') AS cliente, "
                        + "p.nombre AS producto, p.marca, "
                        + "d.cantidad, d.precio_unitario, d.subtotal "
                        + "FROM detalle_ventas d "
                        + "JOIN ventas v ON v.id_venta = d.id_venta "
                        + "LEFT JOIN clientes c ON c.id = v.id_cliente "
                        + "JOIN productos p ON p.id_producto = d.id_producto "
                        + "WHERE YEAR(v.fecha_hora) = YEAR(CURDATE()) "
                        + "AND MONTH(v.fecha_hora) = MONTH(CURDATE()) "
                        + "ORDER BY v.fecha_hora DESC";
                break;
            case 2: // General
                sql = "SELECT v.fecha_hora, COALESCE(c.nombre,'Sin cliente') AS cliente, "
                        + "p.nombre AS producto, p.marca, "
                        + "d.cantidad, d.precio_unitario, d.subtotal "
                        + "FROM detalle_ventas d "
                        + "JOIN ventas v ON v.id_venta = d.id_venta "
                        + "LEFT JOIN clientes c ON c.id = v.id_cliente "
                        + "JOIN productos p ON p.id_producto = d.id_producto "
                        + "ORDER BY v.fecha_hora DESC";
                break;
            default:
                sql = "SELECT 0, '', '', '', 0, 0, 0";
                break;
        }

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                    rs.getString("fecha_hora"),
                    rs.getString("cliente"),
                    rs.getString("producto"),
                    rs.getString("marca"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario"),
                    rs.getDouble("subtotal")
                };
                modeloDetalle.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("PanelReporteVentas.cargarDetalle: " + e.getMessage());
        }

        int num = modeloDetalle.getRowCount();
        double total = obtenerTotalColumna(6);
        lblDetalleResumen.setText(String.format("%d línea(s) · Total: $%,.2f", num, total));
    }

    private double obtenerTotalColumna(int colIndex) {
        double total = 0;
        for (int r = 0; r < modeloDetalle.getRowCount(); r++) {
            Object v = modeloDetalle.getValueAt(r, colIndex);
            if (v != null) {
                try { total += Double.parseDouble(v.toString()); } catch (Exception ignored) {}
            }
        }
        return total;
    }

    private void anadirListenersTablas() {
        // Usar MouseListener en lugar de ListSelectionListener para evitar
        // problemas de compilación con la inferencia de tipos lambda.
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            tablas[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (tablas[idx].getSelectedRow() >= 0) {
                        cargarDetalle(idx);
                    }
                }
            });
        }
    }

    // =========================================================
    //  Meta diaria
    // =========================================================

    private void cargarMeta() {
        metaDiaria = prefs.getDouble("meta_diaria", 2000);
        txtMeta.setText(String.format("%.0f", metaDiaria));
    }

    private void aplicarMeta() {
        try {
            double m = Double.parseDouble(txtMeta.getText().trim().replace(",", ""));
            if (m <= 0) {
                throw new NumberFormatException();
            }
            metaDiaria = m;
            prefs.putDouble("meta_diaria", m);
            refrescar();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe una meta válida mayor a cero.",
                    "Meta inválida", JOptionPane.WARNING_MESSAGE);
            txtMeta.requestFocusInWindow();
        }
    }

    // =========================================================
    //  Exportar CSV
    // =========================================================

    private void exportarCsv() {
        int i = tabs.getSelectedIndex();
        if (i < 0) {
            return;
        }
        JTable tabla = tablas[i];
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte de ventas");
        fc.setSelectedFile(new java.io.File("reporte_ventas_" + i + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File archivo = fc.getSelectedFile();
        if (!archivo.getAbsolutePath().toLowerCase().endsWith(".csv")) {
            archivo = new java.io.File(archivo.getAbsolutePath() + ".csv");
        }
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(archivo), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.print('\ufeff');
            for (int c = 0; c < tabla.getColumnCount(); c++) {
                pw.print(tabla.getColumnName(c));
                if (c < tabla.getColumnCount() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
            for (int r = 0; r < tabla.getRowCount(); r++) {
                for (int c = 0; c < tabla.getColumnCount(); c++) {
                    Object v = tabla.getValueAt(r, c);
                    String texto = v == null ? "" : v.toString().replace(",", "");
                    pw.print(texto);
                    if (c < tabla.getColumnCount() - 1) {
                        pw.print(",");
                    }
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Reporte exportado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarDetalleCsv() {
        if (modeloDetalle.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos de detalle para exportar.",
                    "Sin datos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar detalle de ventas");
        fc.setSelectedFile(new java.io.File("detalle_ventas.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File archivo = fc.getSelectedFile();
        if (!archivo.getAbsolutePath().toLowerCase().endsWith(".csv")) {
            archivo = new java.io.File(archivo.getAbsolutePath() + ".csv");
        }
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(archivo), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.print('\ufeff');
            for (int c = 0; c < modeloDetalle.getColumnCount(); c++) {
                pw.print(modeloDetalle.getColumnName(c));
                if (c < modeloDetalle.getColumnCount() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
            for (int r = 0; r < modeloDetalle.getRowCount(); r++) {
                for (int c = 0; c < modeloDetalle.getColumnCount(); c++) {
                    Object v = modeloDetalle.getValueAt(r, c);
                    String texto = v == null ? "" : v.toString().replace(",", "");
                    pw.print(texto);
                    if (c < modeloDetalle.getColumnCount() - 1) {
                        pw.print(",");
                    }
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Detalle exportado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    //  Lata Monster animada
    // =========================================================

    /** Lata que se llena de abajo hacia arriba según el avance de la meta. */
    static class MonsterCan extends JPanel {

        private double objetivo = 0;
        private double actual = 0;
        private String textoMonto = "$0.00 / $0.00";
        private Timer animacion;

        MonsterCan() {
            setOpaque(false);
            setPreferredSize(new Dimension(200, 330));
            setMaximumSize(new Dimension(200, 330));
            setMinimumSize(new Dimension(160, 280));
            animacion = new Timer(18, e -> {
                double dif = objetivo - actual;
                if (Math.abs(dif) < 0.005) {
                    actual = objetivo;
                    animacion.stop();
                } else {
                    actual += dif * 0.18;
                }
                repaint();
            });
        }

        void setProgreso(double progreso, String monto) {
            this.objetivo = Math.max(0, Math.min(1, progreso));
            this.textoMonto = monto;
            if (!animacion.isRunning()) {
                animacion.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int canW = Math.min(150, Math.max(110, w - 50));
            int canH = Math.min(280, Math.max(200, h - 60));
            int x = (w - canW) / 2;
            int y = (h - canH) / 2 - 6;

            // Tapa
            g2.setColor(new Color(190, 195, 198));
            g2.fillRoundRect(x + canW / 2 - 24, y - 12, 48, 16, 8, 8);

            // Cuerpo
            Shape cuerpo = new RoundRectangle2D.Float(x, y, canW, canH, 26, 26);
            g2.setColor(new Color(18, 19, 20));
            g2.fill(cuerpo);

            // Franja verde superior
            g2.setColor(new Color(0, 200, 83));
            g2.fillRoundRect(x, y + 16, canW, 34, 14, 14);

            // Relleno (recortado al cuerpo)
            Shape clipOriginal = g2.getClip();
            g2.clip(cuerpo);
            int rellenoAlto = (int) Math.round(canH * actual);
            if (rellenoAlto > 0) {
                int rellenoY = y + canH - rellenoAlto;
                GradientPaint gradiente = new GradientPaint(0, rellenoY, new Color(0, 230, 118),
                        0, y + canH, new Color(0, 110, 60));
                g2.setPaint(gradiente);
                g2.fillRect(x, rellenoY, canW, rellenoAlto);

                // Burbujas decorativas dentro del líquido
                g2.setColor(new Color(255, 255, 255, 55));
                for (int i = 0; i < 6; i++) {
                    int bx = x + 18 + (i * 23) % Math.max(1, canW - 36);
                    int by = y + canH - ((i * 41) % Math.max(1, rellenoAlto));
                    int r = 3 + (i % 3) * 2;
                    g2.fillOval(bx, by, r, r);
                }
            }
            g2.setClip(clipOriginal);

            // Texto de la marca
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 19));
            dibujarCentrado(g2, "MONSTER", x, y + 40, canW);

            g2.setColor(new Color(200, 255, 215));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            dibujarCentrado(g2, "ENERGY", x, y + 54, canW);

            // Porcentaje grande al centro
            int pct = (int) Math.round(actual * 100);
            g2.setColor(new Color(0, 0, 0, 120));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
            dibujarCentrado(g2, pct + "%", x + 1, y + canH / 2 + 12, canW);
            g2.setColor(Color.WHITE);
            dibujarCentrado(g2, pct + "%", x, y + canH / 2 + 11, canW);

            // Monto
            g2.setColor(new Color(235, 240, 236));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dibujarCentrado(g2, textoMonto, x, y + canH / 2 + 38, canW);

            // Contorno
            g2.setColor(new Color(95, 100, 105));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(cuerpo);

            g2.dispose();
        }

        private void dibujarCentrado(Graphics2D g2, String texto, int x, int y, int ancho) {
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (ancho - fm.stringWidth(texto)) / 2;
            g2.drawString(texto, tx, y);
        }
    }
}