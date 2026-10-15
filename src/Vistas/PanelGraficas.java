package Vistas;

import Conexion.BD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class PanelGraficas extends javax.swing.JPanel {
public void actualizarResumenInventario() {
    // Consulta MySQL para contar SKUs totales y productos con stock bajo (<= 5)
    String sql = "SELECT COUNT(*) AS total_skus, " +
                 "SUM(CASE WHEN stock <= 5 THEN 1 ELSE 0 END) AS stock_bajo " +
                 "FROM productos";

    try (Connection con = BD.getConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            int totalSkus = rs.getInt("total_skus");
            int stockBajo = rs.getInt("stock_bajo");
            if (stockBajo > 0) {
                lblStockBajo.setText(stockBajo + " [ALERTA]");
            } else {
                lblStockBajo.setText("0 (Óptimo)");
            }
            // Reemplaza 'lblTotalSkus' y 'lblStockBajo' por el nombre real de tus variables de los Label
            lblTotalSkus.setText(String.valueOf(totalSkus));
            
            if (stockBajo > 0) {
                lblStockBajo.setText(stockBajo + " 🔴");
            } else {
                lblStockBajo.setText("0 🟢");
            }
        }

    } catch (Exception e) {
        System.out.println("Error al actualizar el resumen: " + e.getMessage());
    }
}

    /**
     * Creates new form PanelGraficas
     */
    public PanelGraficas() {
        initComponents();
        cargarGrafica();
   // Comenta o borra esta línea si te sigue dando guerra con el nombre del icono:
// btnNuevoProducto.setIcon(...);

// Y si quieres que el botón luzca moderno, déjalo con su texto limpio de FlatLaf:
btnNuevoProducto.setText("Nuevo Producto");


// Para los botones de la zona turquesa (Fondo blanco, letras turquesas)
aplicarEstiloBoton(btnAjusteStock, java.awt.Color.WHITE, new java.awt.Color(14, 131, 136));
aplicarEstiloBoton(btnExportarExcel, java.awt.Color.WHITE, new java.awt.Color(14, 131, 136));

// Para el botón de "Nuevo Producto" (Color de acento)
aplicarEstiloBoton(btnNuevoProducto, new java.awt.Color(14, 131, 136), java.awt.Color.WHITE);
    
        
        // 1. Listener para la tabla (con validación anti-NullPointerException)
        tablaInventario.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaInventario.getSelectedRow();
                
                if (fila != -1) {
                    Object valCodigo = tablaInventario.getValueAt(fila, 0);
                    Object valNombre = tablaInventario.getValueAt(fila, 1);
                    
                    if (valCodigo != null && valNombre != null) {
                        String codigo = valCodigo.toString();
                        String nombre = valNombre.toString();

                        // Cargar imagen y detalles de forma segura
                        actualizarImagenProducto(codigo, nombre);
                        actualizarDetallesProducto(codigo);
                    }
                }
            }
        });

        // 2. Listener del buscador en tiempo real
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cargarTablaInventario(txtBuscar.getText());
            }
        });

        // Carga inicial al abrir
        cargarTablaInventario("");
        actualizarResumenInventario();
      
        
    }
   public void cargarTablaInventario(String busqueda) {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tablaInventario.getModel();
        modelo.setRowCount(0); // Limpia la tabla antes de meter los datos
        
        // Pone los nombres correctos en las cabeceras
        String[] titulos = {"Código", "Nombre", "Marca", "Sabor", "Presentación", "Costo", "Precio", "Stock"};
        modelo.setColumnIdentifiers(titulos);

        // Busca en tu tabla 'productos'
        String sql = "SELECT * FROM productos WHERE nombre LIKE ? OR marca LIKE ?";
        
        try (java.sql.Connection con = BD.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
             
             ps.setString(1, "%" + busqueda + "%");
             ps.setString(2, "%" + busqueda + "%");
             java.sql.ResultSet rs = ps.executeQuery();
             
             while (rs.next()) {
                 modelo.addRow(new Object[]{
                     rs.getString("codigo_barras"),
                     rs.getString("nombre"),
                     rs.getString("marca"),
                     rs.getString("sabor"),
                     rs.getString("presentacion"),
                     rs.getDouble("costo_compra"), // <--- ESTE ERA EL ERROR. Ahora sí coincide con tu BD.
                     rs.getDouble("precio_venta"),
                     rs.getInt("stock")
                 });
             }
        } catch (Exception e) {
             System.out.println("Error al cargar inventario: " + e.getMessage());
        }
    }
    public void aplicarEstiloBoton(javax.swing.JButton boton, java.awt.Color colorFondo, java.awt.Color colorTexto) {
        boton.setBackground(colorFondo);
        boton.setForeground(colorTexto);
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        boton.setFocusPainted(false);       // Quita el recuadro punteado al hacer clic
        boton.setBorderPainted(false);      // Quita el borde 3D viejo
        boton.setOpaque(true);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
    // Diseño HTML en JTextPane para la ficha del producto
public void mostrarDescripcionFormateada(String nombreProd, String marca, String presentacion, String descripcionRaw) {
        
        String textoSeguro = "[html]"
            + "[body style='font-family: Segoe UI, sans-serif; padding: 10px; color: #333333;']"
            + "[h2 style='color: #0E8388; margin-top: 0; margin-bottom: 5px; text-transform: uppercase;']" + nombreProd + "[/h2]"
            + "[div style='margin-bottom: 10px; color: #666666;']"
            + "[b]Marca:[/b] " + marca + " | [b]Presentación:[/b] " + presentacion + "[br]"
            + "[/div]"
            + "[div style='border-top: 1px solid #eeeeee; padding-top: 8px;']"
            + "[b]Descripción:[/b][br]"
            + "[p style='color: #555555; line-height: 1.4; margin: 4px 0 0 0;']" + descripcionRaw + "[/p]"
            + "[/div]"
            + "[/body]"
            + "[/html]";

        // Convierte los corchetes a etiquetas HTML reales para evitar el error del chat
        String htmlReal = textoSeguro.replace("[", "<").replace("]", ">");

        txtPaneDescripcion.setContentType("text/html");
        txtPaneDescripcion.setText(htmlReal);
        txtPaneDescripcion.setEditable(false);
    }
public void cargarGrafica() {
        // --- 1. HISTÓRICO DE VENTAS (Conectado a tu BD real) ---
        org.jfree.data.category.DefaultCategoryDataset datos = new org.jfree.data.category.DefaultCategoryDataset();
        
        // Consulta usando las columnas correctas de tu archivo SQL
        String sql = "SELECT DATE_FORMAT(fecha_hora, '%b') AS mes, SUM(total) AS total_mes " +
                     "FROM ventas GROUP BY YEAR(fecha_hora), MONTH(fecha_hora), mes ORDER BY YEAR(fecha_hora), MONTH(fecha_hora)";

        try (java.sql.Connection con = BD.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
             boolean tieneDatos = false;
             while (rs.next()) {
                 tieneDatos = true;
                 datos.addValue(rs.getDouble("total_mes"), "Ingresos", rs.getString("mes"));
             }
             if (!tieneDatos) datos.addValue(0, "Ingresos", "Sin Datos");
        } catch (Exception e) {
             System.out.println("Error BD Gráfica: " + e.getMessage());
             datos.addValue(0, "Ingresos", "Error");
        }

        org.jfree.chart.JFreeChart graficaPrincipal = org.jfree.chart.ChartFactory.createLineChart(
            "Histórico de Ingresos Mensuales", "Mes", "Total ($)", datos,
            org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false
        );

        org.jfree.chart.plot.CategoryPlot plot = graficaPrincipal.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
        
        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer = 
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesPaint(0, new java.awt.Color(14, 131, 136));
        renderer.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));

        // --- 1. CONFIGURACIÓN DEL PANEL PRINCIPAL ---
        org.jfree.chart.ChartPanel panelPrincipal = new org.jfree.chart.ChartPanel(graficaPrincipal);
        
        
        panelPrincipal.setMinimumDrawWidth(100); 
        panelPrincipal.setMaximumDrawWidth(2000);
        panelPrincipal.setPreferredSize(new java.awt.Dimension(510, 200)); // Tamaño base controlable

        // --- 2. PANELES LATERALES (ANILLOS) ---
        javax.swing.JPanel panelLateral = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 15));
        panelLateral.setBackground(java.awt.Color.WHITE);
        
        // Redujimos un poco el ancho del panel derecho (de 250 a 180) para garantizar que quepa en tu pantalla actual
        panelLateral.setPreferredSize(new java.awt.Dimension(200, 0)); 
        panelLateral.setMinimumSize(new java.awt.Dimension(200, 0));

        panelLateral.add(crearGraficaAnillo("Meta de Ventas", 50));
        panelLateral.add(crearGraficaAnillo("Abastecimiento", 15));

        // --- 3. ENSAMBLAJE FINAL ---
        panelGrafica.removeAll();
        panelGrafica.setLayout(new java.awt.BorderLayout(10, 0)); 
        panelGrafica.add(panelPrincipal, java.awt.BorderLayout.CENTER);
        panelGrafica.add(panelLateral, java.awt.BorderLayout.EAST); 
        
        panelGrafica.revalidate(); 
        panelGrafica.repaint();
    }
    private org.jfree.chart.ChartPanel crearGraficaAnillo(String titulo, int porcentaje) {
        org.jfree.data.general.DefaultPieDataset datosAnillo = new org.jfree.data.general.DefaultPieDataset();
        datosAnillo.setValue("Alcanzado", porcentaje);
        datosAnillo.setValue("Faltante", 100 - porcentaje);

        org.jfree.chart.plot.RingPlot plotAnillo = new org.jfree.chart.plot.RingPlot(datosAnillo);
        plotAnillo.setBackgroundPaint(java.awt.Color.WHITE);
        plotAnillo.setOutlineVisible(false);
        plotAnillo.setShadowPaint(null);
        plotAnillo.setSectionDepth(0.25);
        plotAnillo.setLabelGenerator(null); 
        
        plotAnillo.setSectionPaint("Alcanzado", new java.awt.Color(14, 131, 136));
        plotAnillo.setSectionPaint("Faltante", new java.awt.Color(235, 235, 235));
        
        plotAnillo.setCenterText(porcentaje + "%");
        plotAnillo.setCenterTextFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        plotAnillo.setCenterTextColor(new java.awt.Color(50, 50, 50));

        org.jfree.chart.JFreeChart graficaAnillo = new org.jfree.chart.JFreeChart(
            titulo, new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12), plotAnillo, false
        );
        graficaAnillo.setBackgroundPaint(java.awt.Color.WHITE);

        return new org.jfree.chart.ChartPanel(graficaAnillo);
    }

    public void actualizarDetallesProducto(String codigo) {
        String sql = "SELECT porciones, cafeina, descripcion FROM productos WHERE codigo_barras = ?";
        
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con == null) return;
            
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String porciones = rs.getString("porciones");
                    String cafeina = rs.getString("cafeina");
                    String descripcion = rs.getString("descripcion");
                    
                    txtPaneDescripcion.setText("Porciones: " + (porciones != null ? porciones : "N/D") + "\n"
                                     + "Cafeína: " + (cafeina != null ? cafeina : "N/D") + "\n\n"
                                     + "Descripción:\n" + (descripcion != null ? descripcion : "Sin descripción"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al cargar detalles: " + e.getMessage());
        }
    }

    public void actualizarImagenProducto(String codigo, String nombre) {
    try {
        int ancho = (lblImagenProducto.getWidth() > 0) ? lblImagenProducto.getWidth() : 200;
        int alto = (lblImagenProducto.getHeight() > 0) ? lblImagenProducto.getHeight() : 180;

        // Extensiones comunes a buscar
        String[] extensiones = {".jpg", ".png", ".jpeg"};
        java.net.URL resource = null;

        for (String ext : extensiones) {
            // Busca dentro del paquete/carpeta 'Imagenes' dentro de src
            String rutaRelativa = "/Imagenes/" + codigo + ext;
            resource = getClass().getResource(rutaRelativa);
            
            if (resource != null) {
                break; // Encontró la imagen, salimos del ciclo
            }
        }

        if (resource != null) {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(resource);
            java.awt.Image img = icon.getImage().getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH);
            
            lblImagenProducto.setIcon(new javax.swing.ImageIcon(img));
            lblImagenProducto.setText(""); // Limpiar texto de placeholder
        } else {
            lblImagenProducto.setIcon(null);
            lblImagenProducto.setText("Sin Imagen Disponible");
        }

    } catch (Exception e) {
        lblImagenProducto.setIcon(null);
        lblImagenProducto.setText("Sin Imagen Disponible");
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnNuevoProducto = new javax.swing.JButton();
        lblImagenProducto = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaInventario = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        txtBuscar = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblTotalSkus = new javax.swing.JLabel();
        lblStockBajo = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnAjusteStock = new javax.swing.JButton();
        btnExportarExcel = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtPaneDescripcion = new javax.swing.JTextPane();
        panelGrafica = new javax.swing.JPanel();

        jLabel1.setText("jLabel1");

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        btnNuevoProducto.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnNuevoProducto.setText("Nuevo Producto");
        btnNuevoProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProductoActionPerformed(evt);
            }
        });

        lblImagenProducto.setBackground(new java.awt.Color(255, 255, 255));

        tablaInventario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tablaInventario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaInventarioMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tablaInventarioMouseEntered(evt);
            }
        });
        jScrollPane1.setViewportView(tablaInventario);

        jScrollPane2.setViewportView(jScrollPane1);

        jTextField1.setText("jTextField1");

        txtBuscar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtBuscar.setText("Buscar ");
        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(0, 153, 153));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel2.setText("Resumen de Inventario");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Total SKUs:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Alerta Stock Bajo:");

        lblTotalSkus.setText("jLabel5");

        lblStockBajo.setText("jLabel6");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Operaciones Rápidas");

        btnAjusteStock.setText("AjusteStock");
        btnAjusteStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAjusteStockActionPerformed(evt);
            }
        });

        btnExportarExcel.setText("ExportarExcel");
        btnExportarExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarExcelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTotalSkus))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(32, 32, 32)
                                .addComponent(lblStockBajo))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(19, 19, 19))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(jLabel5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(btnAjusteStock)
                        .addGap(63, 63, 63)
                        .addComponent(btnExportarExcel)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel2)
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblTotalSkus))
                .addGap(28, 28, 28)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblStockBajo))
                .addGap(41, 41, 41)
                .addComponent(jLabel5)
                .addGap(57, 57, 57)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAjusteStock)
                    .addComponent(btnExportarExcel))
                .addContainerGap(344, Short.MAX_VALUE))
        );

        txtPaneDescripcion.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setViewportView(txtPaneDescripcion);

        panelGrafica.setBackground(new java.awt.Color(204, 204, 255));
        panelGrafica.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(43, 43, 43)
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNuevoProducto))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(lblImagenProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(59, 59, 59)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(panelGrafica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 741, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNuevoProducto)
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .addComponent(lblImagenProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelGrafica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(53, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarActionPerformed
     txtBuscar.putClientProperty(com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, "Buscar por nombre o categoría...");
    }//GEN-LAST:event_txtBuscarActionPerformed

    private void btnNuevoProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProductoActionPerformed
    FormNuevoProducto panel = new FormNuevoProducto();
javax.swing.JDialog dialog = new javax.swing.JDialog();


dialog.setTitle("Registrar Nuevo Producto");
dialog.setContentPane(panel);
dialog.pack(); // Ajusta el tamaño de la ventana al contenido
dialog.setLocationRelativeTo(null); // Centra la ventana en la pantalla
dialog.setVisible(true);

    }//GEN-LAST:event_btnNuevoProductoActionPerformed

    private void btnExportarExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarExcelActionPerformed
                                                
    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
    fileChooser.setDialogTitle("Guardar Lista de Inventario para Excel");
    fileChooser.setSelectedFile(new java.io.File("Inventario_FitnessFuelHub.csv"));

    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
        java.io.File archivoGuardar = fileChooser.getSelectedFile();

        if (!archivoGuardar.getAbsolutePath().toLowerCase().endsWith(".csv")) {
            archivoGuardar = new java.io.File(archivoGuardar.getAbsolutePath() + ".csv");
        }

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(archivoGuardar), 
                    java.nio.charset.StandardCharsets.UTF_8))) {

            javax.swing.table.TableModel model = tablaInventario.getModel();

            // BOM para asegurar compatibilidad de acentos en UTF-8
            pw.print('\ufeff');

            // 1. Escribir Encabezados (usando coma simple sin forzar comillas dobles externas)
            for (int i = 0; i < model.getColumnCount(); i++) {
                pw.print(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) pw.print(",");
            }
            pw.println();

            // 2. Escribir las filas de la tabla de forma limpia
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object val = model.getValueAt(r, c);
                    String texto = (val != null) ? val.toString().replace(",", "") : ""; // Evita romper columnas con comas internas
                    pw.print(texto);
                    if (c < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
            }

            javax.swing.JOptionPane.showMessageDialog(this, 
                "¡Lista exportada correctamente para Excel!", 
                "Exportación Exitosa", 
                javax.swing.JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Error al generar el archivo: " + e.getMessage(), 
                "Error", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    }//GEN-LAST:event_btnExportarExcelActionPerformed

    private void btnAjusteStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAjusteStockActionPerformed
                                                
    // 1. Revisar qué fila tiene seleccionada el usuario en la tabla
    int filaSeleccionada = tablaInventario.getSelectedRow();
    
    if (filaSeleccionada == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Por favor, selecciona un producto de la tabla primero.", 
            "Ningún producto seleccionado", 
            javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // 2. Extraer el código de barras de la primera columna de la fila seleccionada
    String codigoBarra = tablaInventario.getValueAt(filaSeleccionada, 0).toString();
    String nombreProducto = tablaInventario.getValueAt(filaSeleccionada, 1).toString();
    
    // 3. Preguntar cuántas unidades se van a sumar o restar
    String inputCantidad = javax.swing.JOptionPane.showInputDialog(this, 
        "Producto: " + nombreProducto + "\n\nIngresa la cantidad a ajustar:\n(Usa un número negativo para restar, ej. -2)", 
        "Ajuste de Stock", 
        javax.swing.JOptionPane.QUESTION_MESSAGE);
        
    if (inputCantidad == null || inputCantidad.trim().isEmpty()) {
        return; // Si el usuario canceló
    }
    
    try {
        int cantidadAjuste = Integer.parseInt(inputCantidad.trim());
        
        // 4. Ejecutar la sentencia en la base de datos MySQL
        String sql = "UPDATE productos SET stock = stock + ? WHERE codigo_barras = ?";
        
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, cantidadAjuste);
            ps.setString(2, codigoBarra);
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "¡Stock actualizado con éxito!");
                
                // 5. Refrescar la tabla y el resumen lateral automáticamente
                cargarTablaInventario("");
                actualizarResumenInventario();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "No se pudo encontrar el producto en la base de datos.");
            }
            
        } catch (Exception dbEx) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error de base de datos: " + dbEx.getMessage());
        }
        
    } catch (NumberFormatException nfe) {
        javax.swing.JOptionPane.showMessageDialog(this, "Por favor, introduce un número entero válido.", "Error de formato", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    btnAjusteStock.putClientProperty("FlatLaf.style", "arc: 20;");

    }//GEN-LAST:event_btnAjusteStockActionPerformed

    private void tablaInventarioMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaInventarioMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_tablaInventarioMouseEntered

    private void tablaInventarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaInventarioMouseClicked
                                            
    int fila = tablaInventario.getSelectedRow();
    
    if (fila != -1) {
        // 1. Extraemos los valores de la tabla (ajusta los números de columna si es necesario)
        String nombre = tablaInventario.getValueAt(fila, 1).toString();
        String marca = tablaInventario.getValueAt(fila, 2).toString();
        String presentacion = tablaInventario.getValueAt(fila, 4).toString();
        
        // 2. Descripción base (o sácala de tu Base de Datos si tienes la columna)
        String descripcion = "Suplemento de alta calidad para optimizar el rendimiento físico y recuperación muscular.";
        
        // 3. ¡Llamamos al método que inyecta el HTML al JTextPane!
        mostrarDescripcionFormateada(nombre, marca, presentacion, descripcion);
    }
      
    }//GEN-LAST:event_tablaInventarioMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAjusteStock;
    private javax.swing.JButton btnExportarExcel;
    private javax.swing.JButton btnNuevoProducto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblImagenProducto;
    private javax.swing.JLabel lblStockBajo;
    private javax.swing.JLabel lblTotalSkus;
    private javax.swing.JPanel panelGrafica;
    private javax.swing.JTable tablaInventario;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextPane txtPaneDescripcion;
    // End of variables declaration//GEN-END:variables
}
