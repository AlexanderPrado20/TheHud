package Vistas;

import Modelos.Cliente;
import Modelos.ClienteRepository;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Diálogo para buscar y seleccionar un cliente. Muestra el saldo de puntos,
 * la antigüedad y el historial de compras de cada cliente.
 */
public class DialogoBuscarCliente extends JDialog {

    private final Consumer<Cliente> alSeleccionar;
    private final ClienteRepository clienteRepo = new ClienteRepository();

    private final JTextField txtBuscar = new JTextField();
    private final JPanel panelResultados = new JPanel(new Vistas.WrapLayout(FlowLayout.LEFT, 12, 12));
    private final JLabel lblEstado = new JLabel(" ");

    private static final Color FONDO = new Color(43, 43, 43);
    private static final Color TARJETA = new Color(52, 55, 60);
    private static final Color BORDE_TARJETA = new Color(74, 78, 84);
    private static final Color ACENTO = new Color(14, 131, 136);
    private static final Color VERDE_PUNTOS = new Color(0, 230, 118);

    public DialogoBuscarCliente(Frame owner, Consumer<Cliente> alSeleccionar) {
        super(owner, "Buscar Cliente - Fidelidad", true);
        this.alSeleccionar = alSeleccionar;
        construirUI();
    }

    private void construirUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 540);
        setLocationRelativeTo(getOwner());

        JPanel raiz = new JPanel(new BorderLayout(0, 10));
        raiz.setBackground(FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(raiz);

        // Barra superior
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);

        JLabel lblIcono = new JLabel(FontIcon.of(MaterialDesign.MDI_ACCOUNT_SEARCH, 18, new Color(170, 170, 170)));

        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre o teléfono...");
        txtBuscar.addActionListener(e -> buscarClientes(txtBuscar.getText()));

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setIcon(FontIcon.of(MaterialDesign.MDI_MAGNIFY, 16, Color.WHITE));
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBuscar.setBackground(ACENTO);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setOpaque(true);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> buscarClientes(txtBuscar.getText()));

        JButton btnClientePrueba = new JButton("Usar Cliente Ficticio");
        btnClientePrueba.setIcon(FontIcon.of(MaterialDesign.MDI_ACCOUNT_STAR, 16, Color.WHITE));
        btnClientePrueba.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClientePrueba.setBackground(new Color(100, 149, 237));
        btnClientePrueba.setForeground(Color.WHITE);
        btnClientePrueba.setFocusPainted(false);
        btnClientePrueba.setBorderPainted(false);
        btnClientePrueba.setOpaque(true);
        btnClientePrueba.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClientePrueba.addActionListener(e -> usarClienteFicticio());

        JPanel panelCampo = new JPanel(new BorderLayout(6, 0));
        panelCampo.setOpaque(false);
        panelCampo.add(lblIcono, BorderLayout.WEST);
        panelCampo.add(txtBuscar, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnBuscar);
        panelBotones.add(btnClientePrueba);

        barra.add(panelCampo, BorderLayout.CENTER);
        barra.add(panelBotones, BorderLayout.EAST);
        raiz.add(barra, BorderLayout.NORTH);

        // Resultados
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

        cargarClientes();
    }

    private void cargarClientes() {
        mostrarLista(clienteRepo.listarActivos(), "cliente(s)");
    }

    private void buscarClientes(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            cargarClientes();
            return;
        }
        mostrarLista(clienteRepo.buscar(termino.trim()), "cliente(s) encontrado(s)");
    }

    private void mostrarLista(List<Cliente> lista, String sufijoEstado) {
        panelResultados.removeAll();

        if (lista == null || lista.isEmpty()) {
            JLabel vacio = new JLabel("No se encontraron clientes.");
            vacio.setForeground(new Color(200, 120, 120));
            vacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panelResultados.add(vacio);
            lblEstado.setText("0 " + sufijoEstado);
        } else {
            for (Cliente c : lista) {
                panelResultados.add(crearTarjetaCliente(c));
            }
            lblEstado.setText(lista.size() + " " + sufijoEstado);
        }

        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private void usarClienteFicticio() {
        Cliente cliente = clienteRepo.obtenerClienteFicticio();
        if (cliente != null) {
            if (alSeleccionar != null) {
                alSeleccionar.accept(cliente);
            }
            dispose();
        } else {
            cargarClientes();
            lblEstado.setText("Cliente ficticio no encontrado. Mostrando lista completa.");
        }
    }

    private JPanel crearTarjetaCliente(Cliente cliente) {
        JPanel tarjeta = new JPanel(new BorderLayout(8, 8));
        tarjeta.setPreferredSize(new Dimension(340, 160));
        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_TARJETA, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblNombre = new JLabel(cliente.getNombre());
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(lblNombre, BorderLayout.WEST);

        JLabel lblPuntos = new JLabel(cliente.getSaldoPuntos() + " PTS");
        lblPuntos.setForeground(VERDE_PUNTOS);
        lblPuntos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPuntos.setIcon(FontIcon.of(MaterialDesign.MDI_STAR_CIRCLE, 18, VERDE_PUNTOS));
        header.add(lblPuntos, BorderLayout.EAST);
        tarjeta.add(header, BorderLayout.NORTH);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblTelefono = new JLabel("Teléfono: " + cliente.getTelefono());
        lblTelefono.setForeground(new Color(200, 200, 200));
        lblTelefono.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTelefono.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAntiguedad = new JLabel("Antigüedad: " + cliente.getAntiguedadTexto());
        lblAntiguedad.setForeground(new Color(200, 200, 200));
        lblAntiguedad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAntiguedad.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCompras = new JLabel("Compras: " + cliente.getTotalCompras()
                + " | Total: $" + String.format("%.2f", cliente.getTotalGastado()));
        lblCompras.setForeground(new Color(180, 180, 180));
        lblCompras.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCompras.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblTelefono);
        info.add(Box.createVerticalStrut(2));
        info.add(lblAntiguedad);
        info.add(Box.createVerticalStrut(2));
        info.add(lblCompras);
        tarjeta.add(info, BorderLayout.CENTER);

        // Botón seleccionar
        JButton btnSeleccionar = new JButton("Seleccionar Cliente");
        btnSeleccionar.setIcon(FontIcon.of(MaterialDesign.MDI_CHECK_CIRCLE, 16, Color.WHITE));
        btnSeleccionar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSeleccionar.setBackground(ACENTO);
        btnSeleccionar.setForeground(Color.WHITE);
        btnSeleccionar.setFocusPainted(false);
        btnSeleccionar.setBorderPainted(false);
        btnSeleccionar.setOpaque(true);
        btnSeleccionar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSeleccionar.addActionListener(e -> {
            if (alSeleccionar != null) {
                alSeleccionar.accept(cliente);
            }
            dispose();
        });
        tarjeta.add(btnSeleccionar, BorderLayout.SOUTH);

        return tarjeta;
    }
}
