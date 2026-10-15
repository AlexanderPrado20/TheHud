package Vistas;

import vistas.PanelVentas; // PanelVentas vive en el paquete 'vistas'
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

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
    private PanelInventario panelInventario; // CRUD de productos
    private PanelFinanzas panelFinanzas;
    private PanelGraficas panelGraficas;

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
        lblUsuario.setIcon(FontIcon.of(MaterialDesign.MDI_ACCOUNT_CIRCLE, 18, new java.awt.Color(170, 170, 170)));
        lblUsuario.setForeground(java.awt.Color.WHITE);
        lblUsuario.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

        lblCaja = new javax.swing.JLabel(" Caja 1");
        lblCaja.setIcon(FontIcon.of(MaterialDesign.MDI_STORE, 18, new java.awt.Color(0, 200, 150)));
        lblCaja.setForeground(new java.awt.Color(0, 230, 118));
        lblCaja.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        btnCerrarSesion = new javax.swing.JButton("Cerrar sesión");
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
        estilizarBotonMenu(btnExportarPdf, MaterialDesign.MDI_FILE_EXPORT);
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

        btnProductos.setText("Productos / Inventario");
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

        btnGraficas.setText("Gráficas");
        btnGraficas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraficasActionPerformed(evt);
            }
        });

        btnExportarPdf.setText("Exportar Reporte PDF");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnVentas, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(btnProductos, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(btnFinanzas, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(btnGraficas, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(btnExportarPdf, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
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
                .addComponent(panelDerecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelDerecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
