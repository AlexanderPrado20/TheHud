package Vistas;

import Conexion.BD;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class PanelInventario extends javax.swing.JPanel {

    private JLabel lblTotalProductos;
    private JLabel lblProductosBajoStock;
    private JLabel lblValorInventario;
    private JButton btnActualizarGraficas;
    private JLabel jLabel1;
    private JPanel panelGraficaBarras;
    private JPanel panelGraficaPie;

    public PanelInventario() {
        initComponents();
        initComponentsCustom();
    }

    private void initComponentsCustom() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));

        panelGraficaPie = new JPanel(new BorderLayout());
        panelGraficaPie.setBackground(Color.WHITE);
        panelGraficaBarras = new JPanel(new BorderLayout());
        panelGraficaBarras.setBackground(Color.WHITE);

        btnActualizarGraficas = new JButton("Actualizar Gráficas");
        btnActualizarGraficas.addActionListener(e -> {
            actualizarMeticasKPI();
            cargarGraficasJFreeChart();
        });

        add(crearPanelKPIs(), BorderLayout.NORTH);
        add(crearPanelGraficas(), BorderLayout.CENTER);
    }

    private JPanel crearPanelKPIs() {
        JPanel panelKPIs = new JPanel(new GridLayout(1, 3, 15, 15));
        panelKPIs.setOpaque(false);
        panelKPIs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTotalProductos = new JLabel("0", 0);
        JPanel card1 = crearTarjetaKPI("TOTAL PRODUCTOS", lblTotalProductos, new Color(41, 128, 185));

        lblProductosBajoStock = new JLabel("0", 0);
        JPanel card2 = crearTarjetaKPI("POR REABASTECER (≤ 5)", lblProductosBajoStock, new Color(231, 76, 60));

        lblValorInventario = new JLabel("$ 0.00", 0);
        JPanel card3 = crearTarjetaKPI("VALOR DEL INVENTARIO", lblValorInventario, new Color(39, 174, 96));

        panelKPIs.add(card1);
        panelKPIs.add(card2);
        panelKPIs.add(card3);
        return panelKPIs;
    }

    private JPanel crearTarjetaKPI(String titulo, JLabel lblValor, Color colorEncabezado) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));

        JLabel lblTitulo = new JLabel(titulo, 0);
        lblTitulo.setOpaque(true);
        lblTitulo.setBackground(colorEncabezado);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", 1, 12));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(6, 5, 6, 5));

        lblValor.setFont(new Font("SansSerif", 1, 22));
        lblValor.setForeground(new Color(40, 40, 40));
        lblValor.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelGraficas() {
        JPanel panelCentral = new JPanel(new BorderLayout(5, 5));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelContenedorGraficas = new JPanel(new GridLayout(1, 2, 10, 10));
        panelContenedorGraficas.setOpaque(false);

        panelGraficaPie.setBorder(BorderFactory.createTitledBorder("Distribución de Inventario"));
        panelGraficaBarras.setBorder(BorderFactory.createTitledBorder("Top 5 Reabastecimiento"));

        panelContenedorGraficas.add(panelGraficaPie);
        panelContenedorGraficas.add(panelGraficaBarras);

        JPanel panelBarraBoton = new JPanel(new FlowLayout(2));
        panelBarraBoton.setOpaque(false);
        panelBarraBoton.add(btnActualizarGraficas);

        panelCentral.add(panelBarraBoton, BorderLayout.NORTH);
        panelCentral.add(panelContenedorGraficas, BorderLayout.CENTER);
        return panelCentral;
    }

    public void actualizarMeticasKPI() {
        String sql = "SELECT   COUNT(*) AS total_skus, "
                   + "  SUM(CASE WHEN stock <= 5 THEN 1 ELSE 0 END) AS bajo_stock, "
                   + "  SUM(stock * costo_compra) AS valor_total "
                   + "FROM productos";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int totalSkus = rs.getInt("total_skus");
                int bajoStock = rs.getInt("bajo_stock");
                double valorTotal = rs.getDouble("valor_total");

                lblTotalProductos.setText(String.valueOf(totalSkus));
                lblProductosBajoStock.setText(String.valueOf(bajoStock));
                lblValorInventario.setText(String.format("$ %.2f MXN", valorTotal));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al calcular métricas: " + e.getMessage(),
                    "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarGraficasJFreeChart() {
        DefaultPieDataset datasetPie = new DefaultPieDataset();
        DefaultCategoryDataset datasetBar = new DefaultCategoryDataset();

        String sql = "SELECT nombre, stock FROM productos ORDER BY stock ASC";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int optimo = 0;
            int bajoStock = 0;
            int agotado = 0;
            int contadorTop5 = 0;

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int stock = rs.getInt("stock");

                if (stock <= 0) {
                    agotado++;
                } else if (stock <= 5) {
                    bajoStock++;
                } else {
                    optimo++;
                }

                if (contadorTop5 < 5) {
                    String nombreCorto = (nombre.length() > 10) ? nombre.substring(0, 8) + ".." : nombre;
                    datasetBar.addValue(stock, "Stock", nombreCorto);
                    contadorTop5++;
                }
            }

            datasetPie.setValue("Stock Óptimo (>5)", optimo);
            datasetPie.setValue("Bajo Stock (1-5)", bajoStock);
            datasetPie.setValue("Agotados (0)", agotado);

            // ==========================================
            // 1. CONFIGURACIÓN ESTÉTICA: GRÁFICA DE PASTEL
            // ==========================================
            JFreeChart chartPie = ChartFactory.createPieChart(
                    null, datasetPie, true, true, false
            );
            chartPie.setBackgroundPaint(Color.WHITE);

            PiePlot plotPie = (PiePlot) chartPie.getPlot();
            plotPie.setBackgroundPaint(Color.WHITE);
            plotPie.setOutlinePaint(null);
            plotPie.setSimpleLabels(true);

            plotPie.setSectionPaint("Stock Óptimo (>5)", new Color(46, 204, 113));
            plotPie.setSectionPaint("Bajo Stock (1-5)", new Color(241, 196, 15));
            plotPie.setSectionPaint("Agotados (0)", new Color(231, 76, 60));

            ChartPanel panelPieChart = new ChartPanel(chartPie);
            panelPieChart.setPreferredSize(new java.awt.Dimension(320, 200));
            panelPieChart.setMinimumDrawWidth(0);
            panelPieChart.setMinimumDrawHeight(0);

            panelGraficaPie.removeAll();
            panelGraficaPie.add(panelPieChart, BorderLayout.CENTER);
            panelGraficaPie.revalidate();
            panelGraficaPie.repaint();

            // ==========================================
            // 2. CONFIGURACIÓN ESTÉTICA: GRÁFICA DE BARRAS
            // ==========================================
            JFreeChart chartBar = ChartFactory.createBarChart(
                    null, "Producto", "Unidades", datasetBar,
                    PlotOrientation.VERTICAL, false, true, false
            );
            chartBar.setBackgroundPaint(Color.WHITE);

            CategoryPlot plotBar = chartBar.getCategoryPlot();
            plotBar.setBackgroundPaint(new Color(245, 245, 245));
            plotBar.setRangeGridlinePaint(Color.LIGHT_GRAY);

            CategoryAxis domainAxis = plotBar.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

            BarRenderer renderer = (BarRenderer) plotBar.getRenderer();
            renderer.setSeriesPaint(0, new Color(52, 152, 219));

            ChartPanel panelBarChart = new ChartPanel(chartBar);
            panelBarChart.setPreferredSize(new java.awt.Dimension(320, 200));
            panelBarChart.setMinimumDrawWidth(0);
            panelBarChart.setMinimumDrawHeight(0);

            panelGraficaBarras.removeAll();
            panelGraficaBarras.add(panelBarChart, BorderLayout.CENTER);
            panelGraficaBarras.revalidate();
            panelGraficaBarras.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar gráficas: " + e.getMessage(),
                    "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        panelGraficaPie = new JPanel();
        panelGraficaBarras = new JPanel();
        btnActualizarGraficas = new JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 2, 18)); // NOI18N
        jLabel1.setText("Iventario");

        panelGraficaPie.setBackground(new java.awt.Color(255, 255, 204));
        panelGraficaPie.setForeground(new java.awt.Color(255, 255, 204));
        GroupLayout panelGraficaPieLayout = new GroupLayout(panelGraficaPie);
        panelGraficaPie.setLayout(panelGraficaPieLayout);
        panelGraficaPieLayout.setHorizontalGroup(
            panelGraficaPieLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 202, Short.MAX_VALUE)
        );
        panelGraficaPieLayout.setVerticalGroup(
            panelGraficaPieLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        panelGraficaBarras.setBackground(new java.awt.Color(255, 255, 204));
        panelGraficaBarras.setForeground(new java.awt.Color(255, 255, 204));
        GroupLayout panelGraficaBarrasLayout = new GroupLayout(panelGraficaBarras);
        panelGraficaBarras.setLayout(panelGraficaBarrasLayout);
        panelGraficaBarrasLayout.setHorizontalGroup(
            panelGraficaBarrasLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 220, Short.MAX_VALUE)
        );
        panelGraficaBarrasLayout.setVerticalGroup(
            panelGraficaBarrasLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 194, Short.MAX_VALUE)
        );

        btnActualizarGraficas.setText("jButton1");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnActualizarGraficas))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(panelGraficaPie, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(panelGraficaBarras, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(66, 66, 66))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelGraficaBarras, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelGraficaPie, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnActualizarGraficas)
                .addContainerGap(156, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
