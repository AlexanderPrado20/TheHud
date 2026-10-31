package Vistas;

import Conexion.BD;
import Vistas.PanelVentas;
import Vistas.PanelFinanzas;
import Vistas.PanelGraficas;
import Vistas.PanelInventario;
import Vistas.PanelCombos;
import Vistas.FrmLogin;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;
import javax.swing.ImageIcon;

public class FrmMenuPrnciapal extends javax.swing.JFrame {

    private String usuario = "empleado";
    private String rol = "Empleado";

    // Componentes construidos por código (fuera del código generado)
    private javax.swing.JPanel panelContenido;
    private javax.swing.JPanel panelBarra;
    private javax.swing.JLabel lblSeccion;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JLabel lblCaja;
    private javax.swing.JButton btnCerrarSesion;

    // Paneles de contenido (se crean una sola vez para conservar su estado)
    private PanelVentas panelVentas;
    private PanelInventario panelInventario; // Tablero de control (KPIs + gráficas)
    private PanelFinanzas panelFinanzas;
    private PanelGraficas panelGraficas;
    private PanelCombos panelCombos; // Combos / promociones

    public FrmMenuPrnciapal() {
        this("empleado", "Empleado");
    }

    public FrmMenuPrnciapal(String usuario, String rol) {
        initComponents();
        this.usuario = usuario != null ? usuario : "empleado";
        this.rol = rol != null ? rol : "Empleado";
        configurarMenu();
    }

    private void configurarMenu() {
        setTitle("The Fitness Fuel Hub - Punto de Venta");
        setSize(1320, 780);
        setLocationRelativeTo(null);

        panelContenido = new javax.swing.JPanel(new java.awt.BorderLayout());
        panelContenido.setBackground(new java.awt.Color(43, 43, 43));

        construirBarraSuperior();

        panelDerecha.add(panelBarra, java.awt.BorderLayout.NORTH);
        panelDerecha.add(panelContenido, java.awt.BorderLayout.CENTER);

        estilizarMenu();

        // Primera pantalla: Venta
        mostrarVenta();
    }

    private void construirBarraSuperior() {
        panelBarra = new javax.swing.JPanel(new java.awt.BorderLayout());
        panelBarra.setBackground(new java.awt.Color(33, 37, 41));
        panelBarra.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 18, 8, 18));

        lblSeccion = new javax.swing.JLabel("Venta");
        lblSeccion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        lblSeccion.setForeground(java.awt.Color.WHITE);
        panelBarra.add(lblSeccion, java.awt.BorderLayout.WEST);

        javax.swing.JPanel panelInfo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 16, 0));
        panelInfo.setOpaque(false);

        lblUsuario = new javax.swing.JLabel(" " + usuario + "  ·  " + rol);
        // Icono de usuario - usando imagen estándar
        lblUsuario.setIcon(FontIcon.of(MaterialDesign.MDI_ACCOUNT_CIRCLE, 18, new java.awt.Color(170, 170, 170)));
        lblUsuario.setForeground(java.awt.Color.WHITE);
        lblUsuario.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

        lblCaja = new javax.swing.JLabel(" Caja 1");
        // Icono de caja - usando imagen estándar
        lblCaja.setIcon(FontIcon.of(MaterialDesign.MDI_STORE, 18, new java.awt.Color(0, 200, 150)));
        lblCaja.setForeground(new java.awt.Color(0, 230, 118));
        lblCaja.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        btnCerrarSesion = new javax.swing.JButton("Cerrar sesión");
        // Botón cerrar sesión - usando imagen estándar
        btnCerrarSesion.setIcon(FontIcon.of(MaterialDesign.MDI_LOGOUT, 16, java.awt.Color.WHITE));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnCerrarSesion.setBackground(new java.awt.Color(190, 60, 60));
        btnCerrarSesion.setForeground(java.awt.Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setOpaque(true);
        btnCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelInfo.add(lblUsuario);
        panelInfo.add(lblCaja);
        panelInfo.add(btnCerrarSesion);

        panelBarra.add(panelInfo, java.awt.BorderLayout.EAST);
    }

    private void estilizarMenu() {
        estilizarBotonMenu(btnVentas, MaterialDesign.MDI_CART);
        estilizarBotonMenu(btnProductos, MaterialDesign.MDI_PACKAGE_VARIANT);
        estilizarBotonMenu(btnFinanzas, MaterialDesign.MDI_CASH_MULTIPLE);
        estilizarBotonMenu(btnGraficas, MaterialDesign.MDI_CHART_BAR);
        estilizarBotonMenu(btnCombos, MaterialDesign.MDI_SALE);
        estilizarBotonMenu(btnExportarPdf, MaterialDesign.MDI_FILE_EXPORT);
        
        try {
            agregarBotonTema();
            agregarBotonProveedores();
        } catch (Exception e) {
            System.err.println("Error al agregar botones: " + e.getMessage());
        }
        
        aplicarPermisosPorRol();
    }
    
    private javax.swing.JButton btnTema;
    private javax.swing.JButton btnProveedores;
    private boolean temaOscuro = true;
    
    private void agregarBotonTema() {
        if (btnTema == null) {
            btnTema = new javax.swing.JButton("Modo Claro");
            btnTema.setIcon(FontIcon.of(MaterialDesign.MDI_WHITE_BALANCE_SUNNY, 16, java.awt.Color.WHITE));
            btnTema.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            btnTema.setIconTextGap(12);
            btnTema.setBackground(new java.awt.Color(65, 70, 75));
            btnTema.setForeground(java.awt.Color.WHITE);
            btnTema.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            btnTema.setFocusPainted(false);
            btnTema.setBorderPainted(false);
            btnTema.setOpaque(true);
            btnTema.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnTema.addActionListener(e -> cambiarTema());
        }
        
        if (btnProveedores == null) {
            agregarBotonProveedores();
        }
        
        if (jPanel1.getLayout() instanceof javax.swing.GroupLayout && btnTema != null && btnProveedores != null) {
            try {
                javax.swing.GroupLayout layout = (javax.swing.GroupLayout) jPanel1.getLayout();
                jPanel1.remove(btnTema);
                jPanel1.remove(btnProveedores);
                
                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnFinanzas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnGraficas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCombos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnExportarPdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnTema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(24, 24, 24))
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnFinanzas, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnGraficas, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnCombos, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnExportarPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnTema, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel1.add(btnProveedores);
                jPanel1.add(btnTema);
            } catch (Exception ex) {
                System.err.println("Error layout: " + ex.getMessage());
            }
        }
    }
    
    private void agregarBotonProveedores() {
        if (btnProveedores == null) {
            btnProveedores = new javax.swing.JButton("Proveedores");
            btnProveedores.setIcon(FontIcon.of(MaterialDesign.MDI_TRUCK_DELIVERY, 16, java.awt.Color.WHITE));
            btnProveedores.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            btnProveedores.setIconTextGap(12);
            btnProveedores.setBackground(new java.awt.Color(45, 49, 52));
            btnProveedores.setForeground(java.awt.Color.WHITE);
            btnProveedores.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btnProveedores.setFocusPainted(false);
            btnProveedores.setBorderPainted(false);
            btnProveedores.setOpaque(true);
            btnProveedores.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnProveedores.addActionListener(e -> mostrarProveedores());
        }
    }
    
    private void mostrarProveedores() {
        lblSeccion.setText("Proveedores");
        JPanel panelProv = new JPanel(new java.awt.BorderLayout());
        panelProv.setBackground(new java.awt.Color(43, 43, 43));
        
        JPanel header = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        header.setBackground(new java.awt.Color(52, 55, 60));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titulo = new JLabel("Historial de Compras a Proveedores");
        titulo.setForeground(java.awt.Color.WHITE);
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        header.add(titulo);
        panelProv.add(header, java.awt.BorderLayout.NORTH);
        
        String[] cols = {"Proveedor", "Última Compra", "Artículos", "Precio Total", "Teléfono", "Contacto"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        model.addRow(new Object[]{"Suplementos MX", "2026-10-25", "Whey, Creatina, Pre-entreno", "$12,450.00", "555-987-6543", "Ventas"});
        model.addRow(new Object[]{"Distribuidora Fit", "2026-10-20", "Colágeno, Aminoácidos", "$5,890.00", "555-456-7890", "Compras"});
        model.addRow(new Object[]{"Optimum Supply", "2026-10-15", "BCAAs, Glutamina", "$3,750.00", "555-123-9876", "Representante"});
        
        JTable tablaProv = new JTable(model);
        tablaProv.setBackground(new java.awt.Color(52, 55, 60));
        tablaProv.setForeground(java.awt.Color.WHITE);
        tablaProv.setGridColor(new java.awt.Color(74, 78, 84));
        tablaProv.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(tablaProv);
        scroll.getViewport().setBackground(new java.awt.Color(52, 55, 60));
        panelProv.add(scroll, java.awt.BorderLayout.CENTER);
        
        mostrarPanel(panelProv);
    }
    
    private void generarEstadoResultados() {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            String ruta = System.getProperty("user.home") + java.io.File.separator + "Estado_Resultado_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(ruta));
            document.open();
            
            // Agregar imagen del logo al PDF
            try {
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(
                    getClass().getResource("/Imagenes/ImagenPDF.png"));
                img.scaleToFit(120, 60);
                img.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(img);
                document.add(com.itextpdf.text.Chunk.NEWLINE);
            } catch (Exception imgEx) {
                System.err.println("No se pudo cargar la imagen del PDF: " + imgEx.getMessage());
            }
            
            com.itextpdf.text.Font tituloFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font subtituloFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("THE FITNESS FUEL HUB", tituloFont);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);
            com.itextpdf.text.Paragraph subtitulo = new com.itextpdf.text.Paragraph("ESTADO DE RESULTADOS", subtituloFont);
            subtitulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(subtitulo);
            document.add(new com.itextpdf.text.Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), normalFont));
            document.add(com.itextpdf.text.Chunk.NEWLINE);
            
            try (java.sql.Connection con = Conexion.BD.getConexion();
                 java.sql.Statement st = con.createStatement()) {
                
                document.add(new com.itextpdf.text.Paragraph("RESUMEN GENERAL", subtituloFont));
                java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) as num, COALESCE(SUM(total),0) as total FROM ventas");
                if (rs.next()) {
                    document.add(new com.itextpdf.text.Paragraph("Numero de ventas: " + rs.getInt("num"), normalFont));
                    document.add(new com.itextpdf.text.Paragraph("Total vendido: $" + String.format("%.2f", rs.getDouble("total")) + " MXN", normalFont));
                }
                document.add(com.itextpdf.text.Chunk.NEWLINE);
                
                document.add(new com.itextpdf.text.Paragraph("VENTAS POR METODO DE PAGO", subtituloFont));
                rs = st.executeQuery("SELECT metodo_pago, COUNT(*) as num, SUM(total) as total FROM ventas GROUP BY metodo_pago");
                while (rs.next()) {
                    document.add(new com.itextpdf.text.Paragraph(rs.getString("metodo_pago") + ": " + rs.getInt("num") + " ventas - $" + String.format("%.2f", rs.getDouble("total")) + " MXN", normalFont));
                }
                document.add(com.itextpdf.text.Chunk.NEWLINE);
                
                document.add(new com.itextpdf.text.Paragraph("TOP 5 PRODUCTOS MAS VENDIDOS", subtituloFont));
                rs = st.executeQuery("SELECT p.nombre, SUM(d.cantidad) as total_vendido, SUM(d.subtotal) as monto FROM detalle_ventas d JOIN productos p ON p.id_producto = d.id_producto GROUP BY p.id_producto, p.nombre ORDER BY total_vendido DESC LIMIT 5");
                while (rs.next()) {
                    document.add(new com.itextpdf.text.Paragraph(rs.getString("nombre") + " - " + rs.getInt("total_vendido") + " unidades - $" + String.format("%.2f", rs.getDouble("monto")) + " MXN", normalFont));
                }
                document.add(com.itextpdf.text.Chunk.NEWLINE);
                
                document.add(new com.itextpdf.text.Paragraph("INVENTARIO", subtituloFont));
                rs = st.executeQuery("SELECT COUNT(*) as num, COALESCE(SUM(stock),0) as stock, COALESCE(SUM(stock*precio_venta),0) as valor FROM productos");
                if (rs.next()) {
                    document.add(new com.itextpdf.text.Paragraph("SKUs: " + rs.getInt("num"), normalFont));
                    document.add(new com.itextpdf.text.Paragraph("Unidades totales: " + rs.getInt("stock"), normalFont));
                    document.add(new com.itextpdf.text.Paragraph("Valor en inventario: $" + String.format("%.2f", rs.getDouble("valor")) + " MXN", normalFont));
                }
            }
            
            document.close();
            int opt = JOptionPane.showConfirmDialog(this, "PDF generado correctamente en:\n" + ruta + "\n\n¿Deseas abrirlo?", "Estado de Resultados", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(ruta));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo abrir el PDF: " + ex.getMessage(), "Aviso", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cambiarTema() {
        if (temaOscuro) {
            try {
                com.formdev.flatlaf.FlatLaf.setup(new com.formdev.flatlaf.FlatIntelliJLaf());
                javax.swing.UIManager.put("Component.arc", 10);
                btnTema.setText("Modo Oscuro");
                btnTema.setIcon(FontIcon.of(MaterialDesign.MDI_WHITE_BALANCE_SUNNY, 16, java.awt.Color.WHITE));
                temaOscuro = false;
                if (jPanel1 != null) {
                    jPanel1.setBackground(new java.awt.Color(235, 238, 242));
                    if (jLabel1 != null) jLabel1.setForeground(new java.awt.Color(40, 44, 48));
                }
                this.getContentPane().setBackground(new java.awt.Color(246, 247, 249));
                if (panelContenido != null) panelContenido.setBackground(new java.awt.Color(246, 247, 249));
            } catch (Exception ex) {}
        } else {
            try {
                com.formdev.flatlaf.FlatDarculaLaf.setup();
                javax.swing.UIManager.put("Component.arc", 10);
                btnTema.setText("Modo Claro");
                btnTema.setIcon(FontIcon.of(MaterialDesign.MDI_WHITE_BALANCE_SUNNY, 16, java.awt.Color.WHITE));
                temaOscuro = true;
                if (jPanel1 != null) jPanel1.setBackground(new java.awt.Color(33, 37, 41));
                this.getContentPane().setBackground(new java.awt.Color(43, 43, 43));
                if (panelContenido != null) panelContenido.setBackground(new java.awt.Color(43, 43, 43));
            } catch (Exception ex) {}
        }
        javax.swing.SwingUtilities.updateComponentTreeUI(this);
    }
    
    private void aplicarPermisosPorRol() {
        boolean esAdmin = rol != null && rol.toLowerCase().contains("admin");
        
        if (!esAdmin) {
            // Empleado solo puede ver Venta y Combos
            if (btnProductos != null) btnProductos.setVisible(false);
            if (btnFinanzas != null) btnFinanzas.setVisible(false);
            if (btnGraficas != null) btnGraficas.setVisible(false);
            if (btnExportarPdf != null) btnExportarPdf.setVisible(false);
        }
    }

    private void estilizarBotonMenu(javax.swing.JButton boton, Ikon icono) {
        boton.setIcon(FontIcon.of(icono, 18, java.awt.Color.WHITE));
        boton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        boton.setIconTextGap(12);
        boton.setBackground(new java.awt.Color(45, 49, 52));
        boton.setForeground(java.awt.Color.WHITE);
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void mostrarPanel(javax.swing.JPanel vista) {
        if (vista == null) {
            return;
        }
        panelContenido.removeAll();
        panelContenido.add(vista, java.awt.BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarVenta() {
        if (panelVentas == null) {
            panelVentas = new PanelVentas();
        }
        lblSeccion.setText("Venta");
        mostrarPanel(panelVentas);
    }

    private void cerrarSesion() {
        int r = javax.swing.JOptionPane.showConfirmDialog(this, "¿Deseas cerrar sesión?",
                "Cerrar sesión", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (r == javax.swing.JOptionPane.YES_OPTION) {
            new FrmLogin().setVisible(true);
            dispose();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnVentas = new javax.swing.JButton();
        btnProductos = new javax.swing.JButton();
        btnFinanzas = new javax.swing.JButton();
        btnGraficas = new javax.swing.JButton();
        btnCombos = new javax.swing.JButton();
        btnExportarPdf = new javax.swing.JButton();
        panelDerecha = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Fitness Fuel Hub - Punto de Venta");

        jPanel1.setBackground(new java.awt.Color(33, 37, 41));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Logo (2).png"))); // NOI18N

        btnVentas.setText("Venta");
        btnVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentasActionPerformed(evt);
            }
        });

        btnProductos.setText("Indicadores ");
        btnProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductosActionPerformed(evt);
            }
        });

        btnFinanzas.setText("Cédulas Financieras");
        btnFinanzas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinanzasActionPerformed(evt);
            }
        });

        btnGraficas.setText("Productos / Inventario");
        btnGraficas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficasActionPerformed(evt);
            }
        });

        btnCombos.setText("Combos");
        btnCombos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCombosActionPerformed(evt);
            }
        });

        btnExportarPdf.setText("Imprimir PDF");
btnExportarPdf.addActionListener(e -> generarEstadoResultados());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnFinanzas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnGraficas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCombos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnExportarPdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(24, 24, 24))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGap(44, 44, 44)
                .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnFinanzas, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnGraficas, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnCombos, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnExportarPdf, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDerecha.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panelDerecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelDerecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarVenta();
    }

    private void btnFinanzasActionPerformed(java.awt.event.ActionEvent evt) {
        if (panelFinanzas == null) {
            panelFinanzas = new PanelFinanzas();
        }
        lblSeccion.setText("Cédulas Financieras");
        mostrarPanel(panelFinanzas);
    }

    private void btnProductosActionPerformed(java.awt.event.ActionEvent evt) {
        if (panelInventario == null) {
            panelInventario = new PanelInventario();
        } else {
            panelInventario.refrescar();
        }
        lblSeccion.setText("Productos / Inventario");
        mostrarPanel(panelInventario);
    }

    private void btnGraficasActionPerformed(java.awt.event.ActionEvent evt) {
        if (panelGraficas == null) {
            panelGraficas = new PanelGraficas();
        }
        lblSeccion.setText("Gráficas");
        mostrarPanel(panelGraficas);
    }

    private void btnCombosActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarCombos();
    }

    /** Abre (o refresca) la sección de combos. Al elegir uno salta a Venta y lo agrega. */
    private void mostrarCombos() {
        if (panelCombos == null) {
            panelCombos = new PanelCombos(combo -> {
                mostrarVenta();
                panelVentas.agregarCombo(combo.id, combo.nombre, combo.precio);
            });
        } else {
            panelCombos.refrescar();
        }
        lblSeccion.setText("Combos");
        mostrarPanel(panelCombos);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCombos;
    private javax.swing.JButton btnExportarPdf;
    private javax.swing.JButton btnFinanzas;
    private javax.swing.JButton btnGraficas;
    private javax.swing.JButton btnProductos;
    private javax.swing.JButton btnVentas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panelDerecha;
    // End of variables declaration//GEN-END:variables
}
