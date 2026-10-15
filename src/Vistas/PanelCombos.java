package Vistas;

import Modelos.ComboRepository;
import Modelos.ComboRepository.Combo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Seccion de combos y promociones.
 *
 * Muestra una cuadricula de tarjetas (imagen, nombre, descripcion, precio y
 * vigencia). Al pulsar "Agregar a venta" avisa al menu mediante el callback
 * {@code alAgregar} para que el combo se sume al carrito del punto de venta.
 */
public class PanelCombos extends JPanel {

    private final Consumer<Combo> alAgregar;

    private final JPanel panelResultados = new JPanel(new WrapLayout(java.awt.FlowLayout.LEFT, 16, 16));
    private final JLabel lblEstado = new JLabel(" ");

    private static final Color FONDO = new Color(43, 43, 43);
    private static final Color TARJETA = new Color(52, 55, 60);
    private static final Color BORDE_TARJETA = new Color(74, 78, 84);
    private static final Color ACENTO = new Color(14, 131, 136);
    private static final Color VERDE = new Color(0, 230, 118);
    private static final Color GRIS = new Color(180, 180, 180);
    private static final Color ALERTA = new Color(235, 160, 60);

    public PanelCombos(Consumer<Combo> alAgregar) {
        this.alAgregar = alAgregar;
        construirUI();
        refrescar();
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        // ---- Cabecera ----
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JLabel lblTitulo = new JLabel("  Combos y promociones");
        lblTitulo.setIcon(FontIcon.of(MaterialDesign.MDI_SALE, 26, ACENTO));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel lblSubtitulo = new JLabel("Selecciona un combo para agregarlo a la venta actual.");
        lblSubtitulo.setForeground(GRIS);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel panelTitulos = new JPanel();
        panelTitulos.setOpaque(false);
        panelTitulos.setLayout(new BoxLayout(panelTitulos, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelTitulos.add(lblTitulo);
        panelTitulos.add(Box.createVerticalStrut(4));
        panelTitulos.add(lblSubtitulo);
        cabecera.add(panelTitulos, BorderLayout.WEST);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setIcon(FontIcon.of(MaterialDesign.MDI_REFRESH, 16, Color.WHITE));
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        estilizarBoton(btnActualizar, ACENTO);
        btnActualizar.addActionListener(e -> refrescar());

        JPanel panelAcciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        panelAcciones.setOpaque(false);
        panelAcciones.add(btnActualizar);
        cabecera.add(panelAcciones, BorderLayout.EAST);

        add(cabecera, BorderLayout.NORTH);

        // ---- Cuadricula de tarjetas ----
        panelResultados.setBackground(FONDO);

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE_TARJETA, 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(FONDO);
        add(scroll, BorderLayout.CENTER);

        // ---- Pie de estado ----
        lblEstado.setForeground(GRIS);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));
        add(lblEstado, BorderLayout.SOUTH);
    }

    /** Recarga los combos desde la base de datos y vuelve a dibujar las tarjetas. */
    public void refrescar() {
        panelResultados.removeAll();

        List<Combo> combos = new ComboRepository().cargar();
        for (Combo c : combos) {
            panelResultados.add(crearTarjeta(c));
        }

        if (combos.isEmpty()) {
            JLabel vacio = new JLabel("No hay combos registrados.");
            vacio.setForeground(new Color(200, 120, 120));
            vacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panelResultados.add(vacio);
            lblEstado.setText("0 combos.");
        } else {
            lblEstado.setText(combos.size() + " combo(s) registrado(s).");
        }

        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private JPanel crearTarjeta(final Combo c) {
        JPanel tarjeta = new JPanel(new BorderLayout(6, 6));
        tarjeta.setPreferredSize(new Dimension(250, 360));
        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_TARJETA, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Imagen del combo
        JLabel lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setPreferredSize(new Dimension(228, 150));
        ImageIcon icono = cargarImagen(c.id, 220, 145);
        if (icono != null) {
            lblImagen.setIcon(icono);
        } else {
            lblImagen.setText("Sin imagen");
            lblImagen.setForeground(Color.GRAY);
            lblImagen.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        }
        tarjeta.add(lblImagen, BorderLayout.NORTH);

        // Nombre, descripcion, precio y vigencia
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel("<html><div style='width:224px'>" + escaparHtml(c.nombre) + "</div></html>");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDescripcion = new JLabel("<html><div style='width:224px'>" + escaparHtml(c.descripcion) + "</div></html>");
        lblDescripcion.setForeground(GRIS);
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrecio = new JLabel(String.format("$ %.2f", c.precio));
        lblPrecio.setForeground(VERDE);
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVigencia = new JLabel(c.etiquetaVigencia());
        lblVigencia.setForeground(c.vigente ? VERDE : ALERTA);
        lblVigencia.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVigencia.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblNombre);
        info.add(Box.createVerticalStrut(6));
        info.add(lblDescripcion);
        info.add(Box.createVerticalStrut(8));
        info.add(lblPrecio);
        info.add(Box.createVerticalStrut(2));
        info.add(lblVigencia);
        tarjeta.add(info, BorderLayout.CENTER);

        // Boton agregar
        JButton btnAgregar = new JButton(c.vigente ? "Agregar a venta" : "No disponible");
        btnAgregar.setIcon(FontIcon.of(MaterialDesign.MDI_CART_PLUS, 16, Color.WHITE));
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        estilizarBoton(btnAgregar, c.vigente ? ACENTO : new Color(80, 84, 88));
        btnAgregar.setEnabled(c.vigente);
        btnAgregar.addActionListener(e -> {
            if (alAgregar != null) {
                alAgregar.accept(c);
            }
        });
        tarjeta.add(btnAgregar, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void estilizarBoton(JButton boton, Color fondo) {
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Busca la imagen del combo. Se admiten varios nombres:
     * /Imagenes/COMBO{id}.png (recomendado), y tambien /Imagenes/COM{id}.png.
     * Nota: en Windows no conviene usar "COM1"/"COM2" porque choca con nombres
     * de dispositivo reservados, por eso se prefiere "COMBO{id}".
     */
    private ImageIcon cargarImagen(int id, int ancho, int alto) {
        String[] nombres = {"COMBO" + id, "combo" + id, "COM" + id, "com" + id};
        String[] extensiones = {".png", ".jpg", ".jpeg"};
        for (String nombre : nombres) {
            for (String ext : extensiones) {
                java.net.URL url = getClass().getResource("/Imagenes/" + nombre + ext);
                if (url != null) {
                    ImageIcon original = new ImageIcon(url);
                    Image escalada = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                    return new ImageIcon(escalada);
                }
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
