package Vistas;

import Conexion.BD;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class FrmLogin extends javax.swing.JFrame {

    private static final Color BG_DARK = new Color(18, 20, 24);
    private static final Color BG_PANEL = new Color(28, 32, 38);
    private static final Color ACCENT = new Color(14, 131, 136);
    private static final Color ACCENT_HOVER = new Color(20, 160, 165);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private static final Color TEXT_SECONDARY = new Color(160, 165, 170);
    private static final Color BORDER_COLOR = new Color(60, 65, 70);

    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblSlogan;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JTextField txtUsuario;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JButton btnIngresar;
    private javax.swing.JPanel panelMarca;
    private javax.swing.JPanel panelLogin;

    /** Evita que un doble Enter/clic abra dos veces el menú principal. */
    private boolean ingresando = false;

    /** Creates new form FrmLogin */
    public FrmLogin() {
        initComponents();
        configurarUI();
    }

    private void configurarUI() {
        pack();
        setSize(920, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("The Fitness Fuel Hub - Iniciar sesión");

        // Enter en cualquier campo dispara el ingreso.
        // NOTA: los listeners de Enter ya se registran en initComponents();
        // no se agregan aquí para evitar que el login se ejecute dos veces.

        // Iconos
        btnIngresar.setIcon(FontIcon.of(MaterialDesign.MDI_LOGIN, 18, java.awt.Color.WHITE));
        lblUsuario.setIcon(FontIcon.of(MaterialDesign.MDI_ACCOUNT, 14, TEXT_SECONDARY));
        lblPassword.setIcon(FontIcon.of(MaterialDesign.MDI_LOCK, 14, TEXT_SECONDARY));

        txtUsuario.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        txtPassword.putClientProperty("JTextField.placeholderText", "Ingresa tu contraseña");
        txtPassword.putClientProperty("JPasswordField.showRevealButton", true);

        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Logo (2).png")).getImage());
        } catch (Exception ignored) {
        }

        txtUsuario.requestFocusInWindow();
    }

    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {
        // Un doble Enter/clic no debe abrir dos veces el menú principal.
        if (ingresando) {
            return;
        }
        ingresando = true;
        boolean exito = false;

        try {
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
                        exito = true;
                        dispose();
                    } else {
                        mostrarMensaje("Usuario o contraseña incorrectos.", true);
                        txtPassword.setText("");
                        txtPassword.requestFocusInWindow();
                    }
                }
            } finally {
                try {
                    con.close();
                } catch (java.sql.SQLException ignored) {
                }
            }
        } catch (java.sql.SQLException e) {
            mostrarMensaje("Error al conectar: " + e.getMessage(), true);
        } finally {
            if (!exito) {
                ingresando = false;
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
    private void initComponents() {

        // Panel principal con fondo oscuro
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // Panel izquierdo - marca/gym
        panelMarca = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradiente oscuro
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(10, 12, 16),
                    0, getHeight(), new Color(25, 30, 38)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Líneas decorativas (efecto gym)
                g2d.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30));
                g2d.setStroke(new BasicStroke(1));
                for (int i = 0; i < getWidth(); i += 40) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 40) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
                
                // Efecto de brillo
                g2d.setColor(new Color(255, 255, 255, 8));
                g2d.fillOval(getWidth() - 100, -50, 200, 200);
            }
        };
        panelMarca.setPreferredSize(new Dimension(360, 0));
        panelMarca.setLayout(new GridBagLayout());

        // Logo Login
        lblLogo = new JLabel();
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            java.awt.Image img = new javax.swing.ImageIcon(getClass().getResource("/Imagenes/LogoLogin.png")).getImage();
            java.awt.Image scaled = img.getScaledInstance(180, 180, java.awt.Image.SCALE_SMOOTH);
            lblLogo.setIcon(new javax.swing.ImageIcon(scaled));
        } catch (Exception ignored) {}

        // Título
        lblTitulo = new JLabel("THE FITNESS FUEL HUB");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Slogan
        lblSlogan = new JLabel("Sistema de Punto de Venta");
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSlogan.setForeground(new Color(180, 200, 200));
        lblSlogan.setHorizontalAlignment(SwingConstants.CENTER);

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Gestión de suplementos y nutrición");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(new Color(120, 140, 140));
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel de marca (layout)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panelMarca.add(lblLogo, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(10, 0, 0, 0);
        panelMarca.add(lblTitulo, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 0, 0);
        panelMarca.add(lblSlogan, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(2, 0, 0, 0);
        panelMarca.add(lblSubtitulo, gbc);

        // Panel derecho - login
        panelLogin = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo oscuro con gradiente
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(22, 26, 32),
                    0, getHeight(), new Color(30, 35, 42)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelLogin.setLayout(new GridBagLayout());

        // Card de login (efecto glassmorphism)
        JPanel cardLogin = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo semi-transparente
                g2d.setColor(new Color(40, 45, 52, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Borde sutil
                g2d.setColor(new Color(80, 85, 90, 100));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        cardLogin.setOpaque(false);
        cardLogin.setLayout(new BoxLayout(cardLogin, BoxLayout.Y_AXIS));
        cardLogin.setBorder(new EmptyBorder(40, 50, 40, 50));
        cardLogin.setPreferredSize(new Dimension(400, 380));

        // Bienvenida
        lblBienvenida = new JLabel("Bienvenido");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBienvenida.setForeground(TEXT_PRIMARY);
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Inicia sesión para continuar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Usuario
        lblUsuario = new JLabel("  Usuario");
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUsuario.setForeground(TEXT_SECONDARY);
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtUsuario.setBackground(new Color(35, 40, 46));
        txtUsuario.setForeground(TEXT_PRIMARY);
        txtUsuario.setCaretColor(ACCENT);
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        txtUsuario.setMaximumSize(new Dimension(300, 42));
        txtUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Contraseña
        lblPassword = new JLabel("  Contraseña");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPassword.setForeground(TEXT_SECONDARY);
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPassword.setBackground(new Color(35, 40, 46));
        txtPassword.setForeground(TEXT_PRIMARY);
        txtPassword.setCaretColor(ACCENT);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        txtPassword.setMaximumSize(new Dimension(300, 42));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mensaje
        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMensaje.setForeground(new Color(255, 120, 120));
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón ingresar
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnIngresar.setBackground(ACCENT);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setOpaque(true);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setMaximumSize(new Dimension(300, 44));
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIngresar.addActionListener(e -> btnIngresarActionPerformed(null));

        // Hover effect para el botón
        btnIngresar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnIngresar.setBackground(ACCENT_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnIngresar.setBackground(ACCENT);
            }
        });

        // Agregar componentes al card
        cardLogin.add(lblBienvenida);
        cardLogin.add(Box.createVerticalStrut(4));
        cardLogin.add(lblSub);
        cardLogin.add(Box.createVerticalStrut(30));
        cardLogin.add(lblUsuario);
        cardLogin.add(Box.createVerticalStrut(6));
        cardLogin.add(txtUsuario);
        cardLogin.add(Box.createVerticalStrut(16));
        cardLogin.add(lblPassword);
        cardLogin.add(Box.createVerticalStrut(6));
        cardLogin.add(txtPassword);
        cardLogin.add(Box.createVerticalStrut(12));
        cardLogin.add(lblMensaje);
        cardLogin.add(Box.createVerticalStrut(18));
        cardLogin.add(btnIngresar);

        // Agregar card al panel de login
        GridBagConstraints gbcLogin = new GridBagConstraints();
        gbcLogin.gridx = 0;
        gbcLogin.gridy = 0;
        gbcLogin.weightx = 1;
        gbcLogin.weighty = 1;
        gbcLogin.anchor = GridBagConstraints.CENTER;
        panelLogin.add(cardLogin, gbcLogin);

        // Layout principal
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelMarca, BorderLayout.WEST);
        getContentPane().add(panelLogin, BorderLayout.CENTER);

        // Icono de la ventana
        try {
            setIconImage(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Logo (2).png")).getImage());
        } catch (Exception ignored) {
        }

        // Teclas Enter
        txtUsuario.addActionListener(e -> btnIngresarActionPerformed(null));
        txtPassword.addActionListener(e -> btnIngresarActionPerformed(null));

        txtUsuario.requestFocusInWindow();
    }

    public static void main(String args[]) {
        Tema.aplicar();

        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
