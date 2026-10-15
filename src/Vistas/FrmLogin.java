package Vistas;

import Conexion.BD;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;


public class FrmLogin extends javax.swing.JFrame {

    /** Creates new form FrmLogin */
    public FrmLogin() {
        initComponents();
        configurarUI();
    }

    private void configurarUI() {
        pack();
        setSize(820, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("The Fitness Fuel Hub - Iniciar sesión");

        // Enter en cualquier campo dispara el ingreso
        java.awt.event.ActionListener enter = e -> btnIngresarActionPerformed(null);
        txtUsuario.addActionListener(enter);
        txtPassword.addActionListener(enter);

        // Íconos (se aplican aquí para que no se pierdan al regenerar el formulario)
        btnIngresar.setIcon(FontIcon.of(MaterialDesign.MDI_LOGIN, 18, java.awt.Color.WHITE));
        lblUsuario.setIcon(FontIcon.of(MaterialDesign.MDI_ACCOUNT, 14, new java.awt.Color(160, 160, 160)));
        lblPassword.setIcon(FontIcon.of(MaterialDesign.MDI_LOCK, 14, new java.awt.Color(160, 160, 160)));

        txtUsuario.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        txtPassword.putClientProperty("JTextField.placeholderText", "Ingresa tu contraseña");
        txtPassword.putClientProperty("JPasswordField.showRevealButton", true);

        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Logo (2).png")).getImage());
        } catch (Exception ignored) {
            // Si no está el logo, se ignora
        }

        txtUsuario.requestFocusInWindow();
    }

    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor, llena todos los campos.", true);
            return;
        }

        java.sql.Connection con = BD.getConexion();
        if (con == null) {
            mostrarMensaje("No se pudo conectar con la base de datos.", true);
            return;
        }

        String sql = "SELECT rol FROM usuarios WHERE usuario = ? AND password = ?";
        try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("rol");
                    FrmMenuPrnciapal principal = new FrmMenuPrnciapal(usuario, rol);
                    principal.setVisible(true);
                    dispose();
                } else {
                    mostrarMensaje("Usuario o contraseña incorrectos.", true);
                    txtPassword.setText("");
                    txtPassword.requestFocusInWindow();
                }
            }
        } catch (java.sql.SQLException e) {
            mostrarMensaje("Error al conectar: " + e.getMessage(), true);
        } finally {
            try {
                con.close();
            } catch (java.sql.SQLException ignored) {
            }
        }
    }

    private void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.setForeground(error
                ? new java.awt.Color(255, 120, 120)
                : new java.awt.Color(120, 220, 150));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMarca = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblSlogan = new javax.swing.JLabel();
        panelLogin = new javax.swing.JPanel();
        lblBienvenida = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblMensaje = new javax.swing.JLabel();
        btnIngresar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelMarca.setBackground(new java.awt.Color(14, 131, 136));

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Logo (2).png"))); // NOI18N

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("THE FITNESS FUEL HUB");

        lblSlogan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSlogan.setForeground(new java.awt.Color(214, 240, 240));
        lblSlogan.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlogan.setText("Sistema de Punto de Venta");

        javax.swing.GroupLayout panelMarcaLayout = new javax.swing.GroupLayout(panelMarca);
        panelMarca.setLayout(panelMarcaLayout);
        panelMarcaLayout.setHorizontalGroup(
            panelMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMarcaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSlogan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelMarcaLayout.setVerticalGroup(
            panelMarcaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMarcaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblLogo)
                .addGap(18, 18, 18)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSlogan)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblBienvenida.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblBienvenida.setText("Iniciar sesión");

        lblUsuario.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblUsuario.setText("  Usuario");

        txtUsuario.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N

        lblPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPassword.setText("  Contraseña");

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N

        lblMensaje.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblMensaje.setText(" ");

        btnIngresar.setBackground(new java.awt.Color(14, 131, 136));
        btnIngresar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnIngresar.setForeground(new java.awt.Color(255, 255, 255));
        btnIngresar.setText("Ingresar");
        btnIngresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIngresarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLoginLayout = new javax.swing.GroupLayout(panelLogin);
        panelLogin.setLayout(panelLoginLayout);
        panelLoginLayout.setHorizontalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLoginLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBienvenida)
                    .addComponent(lblUsuario)
                    .addComponent(lblPassword)
                    .addComponent(lblMensaje)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(60, 60, 60))
        );
        panelLoginLayout.setVerticalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLoginLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblBienvenida)
                .addGap(24, 24, 24)
                .addComponent(lblUsuario)
                .addGap(6, 6, 6)
                .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblPassword)
                .addGap(6, 6, 6)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblMensaje)
                .addGap(18, 18, 18)
                .addComponent(btnIngresar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(panelLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMarca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Tema.aplicar();

        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIngresar;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSlogan;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel panelLogin;
    private javax.swing.JPanel panelMarca;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
