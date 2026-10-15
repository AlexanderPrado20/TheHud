package Vistas;

import Modelos.InventarioRepository;
import Modelos.InventarioRepository.ItemStock;
import Modelos.InventarioRepository.KpisInventario;
import Modelos.InventarioRepository.ResumenInventario;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Tablero de control de Inventario.
 *
 * Muestra indicadores (KPIs), alertas de reabastecimiento y graficas que se
 * alimentan de la base de datos a traves de {@link Modelos.InventarioRepository}.
 * Llama a {@link #refrescar()} para recalcular todo; el menu lo invoca cada vez
 * que se abre este panel.
 */
public class PanelInventario extends javax.swing.JPanel {

    // ---- Paleta (oscura, a juego con el resto de la aplicacion) ----
    private static final Color FONDO = new Color(43, 43, 43);
    private static final Color TARJETA = new Color(48, 52, 56);
    private static final Color TARJETA_2 = new Color(40, 44, 48);
    private static final Color BORDE = new Color(66, 71, 76);
    private static final Color TEXTO = new Color(236, 240, 241);
    private static final Color TEXTO_SUAVE = new Color(158, 166, 174);
    private static final Color TURQUESA = new Color(0, 200, 150);
    private static final Color AZUL = new Color(66, 165, 245);
    private static final Color ROJO = new Color(231, 76, 60);
    private static final Color AMBAR = new Color(241, 196, 15);
    private static final Color VERDE = new Color(46, 204, 113);
    private static final Color MORADO = new Color(155, 89, 182);
    private static final Color GRIS = new Color(120, 120, 120);

    private final InventarioRepository repositorio = new InventarioRepository();

    private JSpinner spUmbral;
    private JButton btnActualizar;
    private JLabel lblActualizado;

    private JLabel lblSkus;
    private JLabel lblUnidades;
    private JLabel lblValorCosto;
    private JLabel lblValorVenta;
    private JLabel lblAgotados;
    private JLabel lblBajos;
    private JLabel lblSubBajos;

    private DefaultTableModel modeloReab;
    private JTable tablaReab;

    private ContenedorGrafica panelDistribucion;
    private ContenedorGrafica panelMenorStock;
    private ContenedorGrafica panelTopVendidos;
    private ContenedorGrafica panelIngresos;
    private ContenedorGrafica panelMargenes;
    private ContenedorGrafica panelMetodoPago;

    public PanelInventario() {
        setLayout(new BorderLayout());
        setBackground(FONDO);
        construirInterfaz();
        refrescar();
    }

    // =========================================================
    //  Construccion de la interfaz
    // =========================================================
    private void construirInterfaz() {
        add(crearCabecera(), BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        contenido.add(crearSeccionKpis());
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(crearSeccionAlertas());
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(crearSeccionGraficas());
        contenido.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setBackground(new Color(33, 37, 41));
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel izquierda = new JPanel();
        izquierda.setOpaque(false);
        izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Inventario");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        titulo.setIcon(FontIcon.of(MaterialDesign.MDI_PACKAGE_VARIANT, 24, TURQUESA));
        titulo.setIconTextGap(10);

        JLabel subtitulo = new JLabel("Tablero de control de existencias");
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitulo.setForeground(TEXTO_SUAVE);

        izquierda.add(titulo);
        izquierda.add(Box.createVerticalStrut(2));
        izquierda.add(subtitulo);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);

        JLabel lblUmbral = new JLabel("Umbral bajo stock:");
        lblUmbral.setForeground(TEXTO_SUAVE);
        lblUmbral.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        spUmbral = new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));
        spUmbral.setPreferredSize(new Dimension(72, 30));
        spUmbral.addChangeListener(e -> refrescar());

        btnActualizar = new JButton("Actualizar");
        btnActualizar.setIcon(FontIcon.of(MaterialDesign.MDI_REFRESH, 16, new Color(20, 22, 24)));
        btnActualizar.setBackground(TURQUESA);
        btnActualizar.setForeground(new Color(20, 22, 24));
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setOpaque(true);
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> refrescar());

        lblActualizado = new JLabel("--:--:--");
        lblActualizado.setForeground(TEXTO_SUAVE);
        lblActualizado.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        derecha.add(lblUmbral);
        derecha.add(spUmbral);
        derecha.add(btnActualizar);
        derecha.add(lblActualizado);

        cabecera.add(izquierda, BorderLayout.WEST);
        cabecera.add(derecha, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearSeccionKpis() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setOpaque(false);

        lblSkus = crearLabelValor();
        lblUnidades = crearLabelValor();
        lblValorCosto = crearLabelValor();
        lblValorVenta = crearLabelValor();
        lblAgotados = crearLabelValor();
        lblBajos = crearLabelValor();

        panel.add(crearTarjeta("Productos (SKUs)", MaterialDesign.MDI_PACKAGE_VARIANT, AZUL,
                lblSkus, new JLabel("referencias registradas")));
        panel.add(crearTarjeta("Unidades en almacén", MaterialDesign.MDI_CUBE_OUTLINE, TURQUESA,
                lblUnidades, new JLabel("piezas totales")));
        panel.add(crearTarjeta("Valor a costo", MaterialDesign.MDI_CASH, MORADO,
                lblValorCosto, new JLabel("inversión en inventario")));
        panel.add(crearTarjeta("Valor a precio de venta", MaterialDesign.MDI_CASH_MULTIPLE, VERDE,
                lblValorVenta, new JLabel("potencial de ingreso")));
        panel.add(crearTarjeta("Agotados", MaterialDesign.MDI_ALERT, ROJO,
                lblAgotados, new JLabel("sin existencias")));
        lblSubBajos = new JLabel("stock \u2264 10");
        panel.add(crearTarjeta("Bajo stock", MaterialDesign.MDI_ALERT_OUTLINE, AMBAR,
                lblBajos, lblSubBajos));

        return panel;
    }

    private JLabel crearLabelValor() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JPanel crearTarjeta(String titulo, Ikon icono, Color acento, JLabel lblValor, JLabel lblSub) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(acento);
        lblTitulo.setIcon(FontIcon.of(icono, 16, acento));
        lblTitulo.setIconTextGap(8);

        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(TEXTO_SUAVE);

        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(6));
        card.add(lblValor);
        card.add(Box.createVerticalStrut(2));
        card.add(lblSub);
        return card;
    }

    private JPanel crearSeccionAlertas() {
        JPanel seccion = new JPanel(new BorderLayout(0, 8));
        seccion.setOpaque(false);

        JLabel titulo = new JLabel("Reabastecimiento urgente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(Color.WHITE);
        titulo.setIcon(FontIcon.of(MaterialDesign.MDI_TRUCK_DELIVERY, 18, AMBAR));
        titulo.setIconTextGap(8);

        modeloReab = new DefaultTableModel(new Object[]{"Código", "Producto", "Marca", "Stock", "Faltante"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReab = new JTable(modeloReab);
        estilizarTabla(tablaReab);
        DefaultTableCellRenderer renderEstado = new RenderEstadoStock();
        tablaReab.getColumnModel().getColumn(3).setCellRenderer(renderEstado);
        tablaReab.getColumnModel().getColumn(4).setCellRenderer(renderEstado);
        tablaReab.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaReab.getColumnModel().getColumn(1).setPreferredWidth(360);
        tablaReab.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaReab.getColumnModel().getColumn(3).setPreferredWidth(70);
        tablaReab.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(tablaReab);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(TARJETA);
        scroll.setPreferredSize(new Dimension(400, 200));

        seccion.add(titulo, BorderLayout.NORTH);
        seccion.add(scroll, BorderLayout.CENTER);
        return seccion;
    }

    private JPanel crearSeccionGraficas() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));
        grid.setOpaque(false);

        panelDistribucion = new ContenedorGrafica("Estado del stock", MaterialDesign.MDI_CHART_PIE);
        panelMenorStock = new ContenedorGrafica("Reabastecimiento (menor stock)", MaterialDesign.MDI_CART_PLUS);
        panelTopVendidos = new ContenedorGrafica("Productos más vendidos", MaterialDesign.MDI_CHART_BAR);
        panelIngresos = new ContenedorGrafica("Ingresos últimos 7 días", MaterialDesign.MDI_CHART_LINE);
        panelMargenes = new ContenedorGrafica("Margen bruto por producto", MaterialDesign.MDI_TRENDING_UP);
        panelMetodoPago = new ContenedorGrafica("Ventas por método de pago", MaterialDesign.MDI_CREDIT_CARD);

        grid.add(panelDistribucion);
        grid.add(panelMenorStock);
        grid.add(panelTopVendidos);
        grid.add(panelIngresos);
        grid.add(panelMargenes);
        grid.add(panelMetodoPago);
        return grid;
    }

    private static void estilizarTabla(JTable tabla) {
        tabla.setBackground(TARJETA);
        tabla.setForeground(TEXTO);
        tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setRowHeight(26);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(new Color(0, 120, 100));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setBackground(new Color(33, 37, 41));
        tabla.getTableHeader().setForeground(TEXTO_SUAVE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    // =========================================================
    //  Refresco de datos
    // =========================================================

    /** Recalcula KPIs, alertas y graficas. Publico para que el menu lo invoque. */
    public void refrescar() {
        if (spUmbral == null) {
            return;
        }
        int umbral = ((Number) spUmbral.getValue()).intValue();

        ResumenInventario datos;
        try {
            datos = repositorio.cargar(umbral);
        } catch (RuntimeException e) {
            datos = new ResumenInventario();
            System.err.println("PanelInventario.refrescar: " + e.getMessage());
        }
        final ResumenInventario r = datos;

        KpisInventario k = r.kpis;
        lblSkus.setText(String.format("%,d", k.skus));
        lblUnidades.setText(String.format("%,d", k.unidades));
        lblValorCosto.setText(moneda(k.valorCosto));
        lblValorVenta.setText(moneda(k.valorVenta));
        lblAgotados.setText(String.format("%,d", k.agotados));
        lblBajos.setText(String.format("%,d", k.bajos));
        lblSubBajos.setText("stock \u2264 " + umbral);

        actualizarTabla(r);
        enSeguridad(() -> dibujarDistribucion(r));
        enSeguridad(() -> dibujarMenorStock(r));
        enSeguridad(() -> dibujarTopVendidos(r));
        enSeguridad(() -> dibujarIngresos(r));
        enSeguridad(() -> dibujarMargenes(r));
        enSeguridad(() -> dibujarMetodoPago(r));

        lblActualizado.setText("Actualizado " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    private void actualizarTabla(ResumenInventario r) {
        modeloReab.setRowCount(0);
        for (ItemStock it : r.reabastecimiento) {
            modeloReab.addRow(new Object[]{it.codigo, it.nombre, it.marca, it.stock, it.faltante});
        }
    }

    private void dibujarDistribucion(ResumenInventario r) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int total = 0;
        for (Map.Entry<String, Integer> e : r.distribucion.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
            total += e.getValue();
        }
        if (total <= 0) {
            dataset.setValue("Sin productos", 1);
        }

        JFreeChart chart = ChartFactory.createRingChart(null, dataset, true, false, false);
        estilizarFondo(chart);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.36);
        plot.setSeparatorsVisible(false);
        plot.setInteriorGap(0.03);
        plot.setSimpleLabels(false);
        plot.setShadowPaint(null);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(new Color(20, 22, 24, 190));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        plot.setSectionPaint("\u00d3ptimo", VERDE);
        plot.setSectionPaint("Bajo", AMBAR);
        plot.setSectionPaint("Agotado", ROJO);
        plot.setSectionPaint("Sin productos", GRIS);

        panelDistribucion.mostrar(chart);
    }

    private void dibujarMenorStock(ResumenInventario r) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        java.util.List<ItemStock> items = r.menorStock;
        java.awt.Paint[] colores = new java.awt.Paint[items.size()];
        Map<String, Integer> usados = new java.util.HashMap<>();
        int i = 0;
        for (ItemStock it : items) {
            dataset.addValue(it.stock, "Stock", etiquetaUnica(it.nombre, 26, usados));
            colores[i++] = it.stock <= 0 ? ROJO : (it.stock <= 5 ? AMBAR : TURQUESA);
        }

        JFreeChart chart = ChartFactory.createBarChart(null, "Producto", "Unidades", dataset,
                PlotOrientation.HORIZONTAL, false, false, false);
        CategoryPlot plot = estilizarCategoria(chart);
        BarRenderer render = new RenderBarrasColor(colores);
        render.setBarPainter(new StandardBarPainter());
        render.setShadowVisible(false);
        render.setDrawBarOutline(false);
        plot.setRenderer(render);

        panelMenorStock.mostrar(chart);
    }

    private void dibujarTopVendidos(ResumenInventario r) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> usados = new java.util.HashMap<>();
        for (ItemStock it : r.topVendidos) {
            dataset.addValue(it.unidades, "Unidades", etiquetaUnica(it.nombre, 26, usados));
        }

        JFreeChart chart = ChartFactory.createBarChart(null, "Producto", "Unidades vendidas", dataset,
                PlotOrientation.HORIZONTAL, false, false, false);
        CategoryPlot plot = estilizarCategoria(chart);
        plot.setRenderer(crearRenderBarras(AZUL));

        panelTopVendidos.mostrar(chart);
    }

    private void dibujarIngresos(ResumenInventario r) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> e : r.ingresosDias.entrySet()) {
            dataset.addValue(e.getValue(), "Ingresos", e.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(null, "Día", "Ingresos", dataset,
                PlotOrientation.VERTICAL, false, false, false);
        CategoryPlot plot = estilizarCategoria(chart);

        LineAndShapeRenderer render = new LineAndShapeRenderer(true, true);
        render.setSeriesPaint(0, TURQUESA);
        render.setSeriesStroke(0, new BasicStroke(2.4f));
        render.setDefaultShapesVisible(true);
        render.setDefaultShapesFilled(true);
        plot.setRenderer(render);

        if (plot.getRangeAxis() instanceof NumberAxis) {
            ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(new java.text.DecimalFormat("$#,##0"));
        }

        panelIngresos.mostrar(chart);
    }

    private void dibujarMargenes(ResumenInventario r) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> usados = new java.util.HashMap<>();
        for (ItemStock it : r.margenes) {
            dataset.addValue(it.margen, "Margen", etiquetaUnica(it.nombre, 26, usados));
        }

        JFreeChart chart = ChartFactory.createBarChart(null, "Producto", "Margen ($)", dataset,
                PlotOrientation.HORIZONTAL, false, false, false);
        CategoryPlot plot = estilizarCategoria(chart);
        plot.setRenderer(crearRenderBarras(VERDE));

        if (plot.getRangeAxis() instanceof NumberAxis) {
            ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(new java.text.DecimalFormat("$#,##0"));
        }

        panelMargenes.mostrar(chart);
    }

    private void dibujarMetodoPago(ResumenInventario r) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        double total = 0;
        for (Map.Entry<String, Double> e : r.porMetodo.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
            total += e.getValue();
        }
        if (total <= 0) {
            dataset.setValue("Sin ventas", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, false, false);
        estilizarFondo(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setOutlineVisible(false);
        plot.setInteriorGap(0.03);
        plot.setSimpleLabels(false);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(new Color(20, 22, 24, 190));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})",
                new java.text.DecimalFormat("$#,##0"), java.text.NumberFormat.getPercentInstance()));
        plot.setSectionPaint("Efectivo", VERDE);
        plot.setSectionPaint("Tarjeta", AZUL);
        plot.setSectionPaint("Sin ventas", GRIS);

        panelMetodoPago.mostrar(chart);
    }

    // =========================================================
    //  Utilidades de estilo de graficas
    // =========================================================
    private static void estilizarFondo(JFreeChart chart) {
        chart.setBackgroundPaint(TARJETA);
        chart.setBorderVisible(false);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(TARJETA);
            chart.getLegend().setItemPaint(TEXTO_SUAVE);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            chart.getLegend().setBorder(0, 0, 0, 0);
        }
    }

    private static CategoryPlot estilizarCategoria(JFreeChart chart) {
        estilizarFondo(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(TARJETA_2);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(64, 69, 74));
        plot.setRangeGridlineStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1f, new float[]{3f, 4f}, 0f));

        plot.getDomainAxis().setLabelPaint(TEXTO_SUAVE);
        plot.getDomainAxis().setTickLabelPaint(TEXTO_SUAVE);
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getDomainAxis().setAxisLinePaint(BORDE);
        plot.getDomainAxis().setTickMarkPaint(BORDE);

        if (plot.getRangeAxis() instanceof NumberAxis) {
            NumberAxis eje = (NumberAxis) plot.getRangeAxis();
            eje.setLabelPaint(TEXTO_SUAVE);
            eje.setTickLabelPaint(TEXTO_SUAVE);
            eje.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            eje.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
            eje.setAxisLinePaint(BORDE);
            eje.setTickMarkPaint(BORDE);
        }
        return plot;
    }

    private static BarRenderer crearRenderBarras(Color color) {
        BarRenderer render = new BarRenderer();
        render.setBarPainter(new StandardBarPainter());
        render.setShadowVisible(false);
        render.setDrawBarOutline(false);
        render.setSeriesPaint(0, color);
        return render;
    }

    private static String moneda(double valor) {
        return String.format("$%,.2f", valor);
    }

    private static void enSeguridad(Runnable tarea) {
        try {
            tarea.run();
        } catch (RuntimeException e) {
            System.err.println("PanelInventario: error al dibujar una grafica: " + e.getMessage());
        }
    }

    private static String etiquetaUnica(String base, int max, Map<String, Integer> usados) {
        String nombre = recortar(base, max);
        int n = usados.merge(nombre, 1, Integer::sum);
        if (n > 1) {
            nombre = recortar(nombre, Math.max(4, max - 4)) + " #" + n;
        }
        return nombre;
    }

    private static String recortar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, Math.max(1, max - 1)) + "...";
    }

    // =========================================================
    //  Clases internas
    // =========================================================

    /** Contenedor con titulo para una grafica (mantiene el ChartPanel entre refrescos). */
    private static class ContenedorGrafica extends JPanel {

        private final JPanel cuerpo = new JPanel(new BorderLayout());
        private ChartPanel chartPanel;

        ContenedorGrafica(String titulo, Ikon icono) {
            super(new BorderLayout(0, 6));
            setBackground(TARJETA);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE, 1, true),
                    BorderFactory.createEmptyBorder(10, 12, 12, 12)));

            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitulo.setForeground(TEXTO);
            lblTitulo.setIcon(FontIcon.of(icono, 16, TURQUESA));
            lblTitulo.setIconTextGap(8);

            cuerpo.setOpaque(false);
            cuerpo.setPreferredSize(new Dimension(420, 240));

            add(lblTitulo, BorderLayout.NORTH);
            add(cuerpo, BorderLayout.CENTER);
        }

        void mostrar(JFreeChart chart) {
            if (chartPanel == null) {
                chartPanel = crearChartPanel(chart);
                cuerpo.add(chartPanel, BorderLayout.CENTER);
            } else {
                chartPanel.setChart(chart);
            }
            cuerpo.revalidate();
            cuerpo.repaint();
        }

        private static ChartPanel crearChartPanel(JFreeChart chart) {
            ChartPanel panel = new ChartPanel(chart);
            panel.setPreferredSize(new Dimension(420, 240));
            panel.setMinimumDrawWidth(0);
            panel.setMinimumDrawHeight(0);
            panel.setMaximumDrawWidth(4000);
            panel.setMaximumDrawHeight(4000);
            panel.setMouseWheelEnabled(false);
            panel.setDomainZoomable(false);
            panel.setRangeZoomable(false);
            panel.setPopupMenu(null);
            panel.setBackground(TARJETA);
            panel.setOpaque(true);
            return panel;
        }
    }

    /** Pinta cada barra segun su categoria (colores de severidad). */
    private static class RenderBarrasColor extends BarRenderer {

        private final Paint[] colores;

        RenderBarrasColor(Paint[] colores) {
            this.colores = colores;
        }

        @Override
        public java.awt.Paint getItemPaint(int row, int column) {
            if (colores == null || colores.length == 0) {
                return TURQUESA;
            }
            return colores[Math.floorMod(column, colores.length)];
        }
    }

    /** Colorea la columna de stock / faltante segun su urgencia. */
    private static class RenderEstadoStock extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!isSelected) {
                c.setBackground(TARJETA);
                int stock = 0;
                try {
                    stock = Integer.parseInt(String.valueOf(table.getModel().getValueAt(row, 3)));
                } catch (NumberFormatException ignored) {
                    // valor no numerico: se ignora
                }
                c.setForeground(stock <= 0 ? ROJO : AMBAR);
            }
            return c;
        }
    }
}
