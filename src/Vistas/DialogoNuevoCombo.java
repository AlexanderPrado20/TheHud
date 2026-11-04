package Vistas;

import Modelos.ComboRepository;
import Modelos.ComboRepository.Detalle;
import Modelos.ComboRepository.ProductoInventario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Diálogo para dar de alta un combo nuevo eligiendo productos del inventario.
 *
 * <p>Guarda el combo en la tabla {@code combos} y sus componentes en
 * {@code combo_detalle}. Al vender el combo, {@code PanelVentas} descuenta el
 * stock de cada producto según su cantidad.</p>
 */
public class DialogoNuevoCombo extends JDialog {

    private static final Color FONDO = new Color(43, 43, 43);
    private static final Color TARJETA = new Color(52, 55, 60);
    private static final Color BORDE = new Color(74, 78, 84);
    private static final Color ACENTO = new Color(14, 131, 136);
    private static final Color VERDE = new Color(0, 200, 120);
    private static final Color TEXTO = new Color(235, 238, 240);
    private static final Color TEXTO_SUAVE = new Color(170, 176, 182);

    private final ComboRepository repositorio = new ComboRepository();
    private final Runnable alGuardar;

    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtDescripcion = new JTextField(22);
    private final JTextField txtPrecio = new JTextField(10);
    private final JTextField txtInicio = new JTextField(10);
    private final JTextField txtFin = new JTextField(10);
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    private final JTextField txtBuscar = new JTextField(18);
    private final DefaultTableModel modeloProductos = new DefaultTableModel(
            new Object[]{"ID", "Código", "Producto", "Marca", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tablaProductos = new JTable(modeloProductos);

    private final DefaultTableModel modeloSeleccionados = new DefaultTableModel(
            new Object[]{"ID", "Producto", "Cantidad", "Stock"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 2;
        }
    };
    private final JTable tablaSeleccionados = new JTable(modeloSeleccionados);

    public DialogoNuevoCombo(Window owner, Runnable alGuardar) {
        super(owner, "Nuevo combo", Dialog.ModalityType.APPLICATION_MODAL);
        this.alGuardar = alGuardar;
        construirUI();
        cargarProductos("");
        setSize(980, 560);
        setLocationRelativeTo(owner);
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(FONDO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 18, 4, 18));

        JLabel titulo = new JLabel("Nuevo combo");
        titulo.setIcon(FontIcon.of(MaterialDesign.MDI_SALE, 24, ACENTO));
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitulo = new JLabel("Selecciona los productos del inventario que formarán el combo.");
        subtitulo.setForeground(TEXTO_SUAVE);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(javax.swing.Box.createVerticalStrut(2));
        textos.add(subtitulo);

        cabecera.add(textos, BorderLayout.WEST);
        return cabecera;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(14, 0));
        centro.setBackground(FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        centro.add(crearFormulario(), BorderLayout.WEST);
        centro.add(crearSelectorProductos(), BorderLayout.CENTER);
        return centro;
    }

    private JPanel crearFormulario() {
        JPanel form = new JPanel();
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.setBackground(TARJETA);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 14, 16)));
        form.setPreferredSize(new Dimension(300, 0));

        agregarCampo(form, "Nombre", txtNombre);
        agregarCampo(form, "Descripción", txtDescripcion);
        agregarCampo(form, "Precio del combo ($)", txtPrecio);
        agregarCampo(form, "Inicio (YYYY-MM-DD)", txtInicio);
        agregarCampo(form, "Fin (YYYY-MM-DD)", txtFin);

        chkActivo.setOpaque(false);
        chkActivo.setForeground(TEXTO);
        chkActivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkActivo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkActivo);

        JLabel ayuda = new JLabel("<html><div style='width:250px;color:#9aa0a6'>"
                + "Las fechas son opcionales. Si las dejas vacías, el combo no tendrá vigencia."
                + "</div></html>");
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ayuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(javax.swing.Box.createVerticalStrut(14));
        form.add(ayuda);

        return form;
    }

    private void agregarCampo(JPanel form, String etiqueta, JTextField campo) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setForeground(TEXTO_SUAVE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        campo.setBackground(new Color(38, 41, 45));
        campo.setForeground(TEXTO);
        campo.setCaretColor(TEXTO);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));

        form.add(lbl);
        form.add(javax.swing.Box.createVerticalStrut(4));
        form.add(campo);
        form.add(javax.swing.Box.createVerticalStrut(10));
    }

    private JPanel crearSelectorProductos() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setOpaque(false);

        // ---- Inventario ----
        JPanel izq = new JPanel(new BorderLayout(0, 6));
        izq.setOpaque(false);

        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barraBusqueda.setOpaque(false);
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setForeground(TEXTO_SUAVE);
        txtBuscar.setBackground(new Color(38, 41, 45));
        txtBuscar.setForeground(TEXTO);
        txtBuscar.setCaretColor(TEXTO);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                cargarProductos(txtBuscar.getText());
            }
        });
        barraBusqueda.add(lblBuscar);
        barraBusqueda.add(txtBuscar);

        estilizarTabla(tablaProductos);
        ocultarColumna(tablaProductos, 0);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregarSeleccionado();
                }
            }
        });

        JScrollPane scrollProd = new JScrollPane(tablaProductos);
        scrollProd.getViewport().setBackground(TARJETA);
        scrollProd.setBorder(BorderFactory.createLineBorder(BORDE));

        JButton btnAgregar = new JButton("Agregar →");
        estilizarBoton(btnAgregar, ACENTO);
        btnAgregar.addActionListener(e -> agregarSeleccionado());

        JPanel pieIzq = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pieIzq.setOpaque(false);
        pieIzq.add(btnAgregar);

        izq.add(barraBusqueda, BorderLayout.NORTH);
        izq.add(scrollProd, BorderLayout.CENTER);
        izq.add(pieIzq, BorderLayout.SOUTH);

        // ---- Seleccionados ----
        JPanel der = new JPanel(new BorderLayout(0, 6));
        der.setOpaque(false);

        JLabel lblSel = new JLabel("Productos del combo");
        lblSel.setForeground(TEXTO);
        lblSel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        estilizarTabla(tablaSeleccionados);
        ocultarColumna(tablaSeleccionados, 0);
        tablaSeleccionados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSeleccionados.getColumnModel().getColumn(1).setPreferredWidth(220);

        JScrollPane scrollSel = new JScrollPane(tablaSeleccionados);
        scrollSel.getViewport().setBackground(TARJETA);
        scrollSel.setBorder(BorderFactory.createLineBorder(BORDE));

        JButton btnQuitar = new JButton("← Quitar");
        estilizarBoton(btnQuitar, new Color(190, 60, 60));
        btnQuitar.addActionListener(e -> quitarSeleccionado());

        JPanel pieDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pieDer.setOpaque(false);
        pieDer.add(btnQuitar);

        der.add(lblSel, BorderLayout.NORTH);
        der.add(scrollSel, BorderLayout.CENTER);
        der.add(pieDer, BorderLayout.SOUTH);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, izq, der);
        split.setOpaque(false);
        split.setBorder(null);
        split.setResizeWeight(0.55);
        contenedor.add(split, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pie.setBackground(FONDO);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));

        JButton btnCancelar = new JButton("Cancelar");
        estilizarBoton(btnCancelar, new Color(90, 94, 98));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = new JButton("Guardar combo");
        btnGuardar.setIcon(FontIcon.of(MaterialDesign.MDI_CONTENT_SAVE, 16, Color.WHITE));
        estilizarBoton(btnGuardar, VERDE.darker());
        btnGuardar.addActionListener(e -> guardar());

        pie.add(btnCancelar);
        pie.add(btnGuardar);
        return pie;
    }

    private void cargarProductos(String filtro) {
        modeloProductos.setRowCount(0);
        List<ProductoInventario> productos = repositorio.cargarProductos(filtro);
        for (ProductoInventario p : productos) {
            modeloProductos.addRow(new Object[]{p.id, p.codigo, p.nombre, p.marca, p.stock});
        }
    }

    private void agregarSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto del inventario.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(tablaProductos.getValueAt(fila, 0).toString());
        String nombre = String.valueOf(tablaProductos.getValueAt(fila, 2));
        int stock = Integer.parseInt(tablaProductos.getValueAt(fila, 4).toString());

        // Si ya está, solo suma una unidad (sin pasar el stock).
        for (int i = 0; i < modeloSeleccionados.getRowCount(); i++) {
            if (Integer.parseInt(modeloSeleccionados.getValueAt(i, 0).toString()) == id) {
                int cant = Integer.parseInt(modeloSeleccionados.getValueAt(i, 2).toString());
                if (cant + 1 > stock) {
                    JOptionPane.showMessageDialog(this, "No hay más stock de " + nombre + ".",
                            "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modeloSeleccionados.setValueAt(cant + 1, i, 2);
                return;
            }
        }

        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, "Ese producto no tiene stock.",
                    "Sin stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloSeleccionados.addRow(new Object[]{id, nombre, 1, stock});
    }

    private void quitarSeleccionado() {
        int fila = tablaSeleccionados.getSelectedRow();
        if (fila >= 0) {
            modeloSeleccionados.removeRow(fila);
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe el nombre del combo.",
                    "Falta el nombre", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocusInWindow();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio no es válido.",
                    "Precio inválido", JOptionPane.WARNING_MESSAGE);
            txtPrecio.requestFocusInWindow();
            return;
        }
        if (precio <= 0) {
            JOptionPane.showMessageDialog(this, "El precio debe ser mayor a cero.",
                    "Precio inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate inicio = parsearFecha(txtInicio.getText());
        LocalDate fin = parsearFecha(txtFin.getText());
        if (!txtInicio.getText().trim().isEmpty() && inicio == null) {
            JOptionPane.showMessageDialog(this, "La fecha de inicio debe ser YYYY-MM-DD.",
                    "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!txtFin.getText().trim().isEmpty() && fin == null) {
            JOptionPane.showMessageDialog(this, "La fecha de fin debe ser YYYY-MM-DD.",
                    "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            JOptionPane.showMessageDialog(this, "La fecha de fin no puede ser anterior al inicio.",
                    "Fechas inválidas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (modeloSeleccionados.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Agrega al menos un producto al combo.",
                    "Combo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Detalle> detalles = new ArrayList<>();
        for (int i = 0; i < modeloSeleccionados.getRowCount(); i++) {
            int id = Integer.parseInt(modeloSeleccionados.getValueAt(i, 0).toString());
            String nom = String.valueOf(modeloSeleccionados.getValueAt(i, 1));
            int cant;
            try {
                cant = Integer.parseInt(modeloSeleccionados.getValueAt(i, 2).toString());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Cantidad inválida para " + nom + ".",
                        "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int stock = Integer.parseInt(modeloSeleccionados.getValueAt(i, 3).toString());
            if (cant <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad de " + nom + " debe ser mayor a cero.",
                        "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cant > stock) {
                JOptionPane.showMessageDialog(this, "Solo hay " + stock + " unidades de " + nom + ".",
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            detalles.add(new Detalle(id, nom, cant));
        }

        int id = repositorio.guardar(nombre, txtDescripcion.getText().trim(), precio,
                inicio, fin, chkActivo.isSelected(), detalles);

        if (id <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar el combo. Revisa la conexión y que exista la tabla combo_detalle.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Combo guardado correctamente.");
        if (alGuardar != null) {
            alGuardar.run();
        }
        dispose();
    }

    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void estilizarTabla(JTable tabla) {
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

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        if (tabla.getColumnModel().getColumnCount() > 4) {
            tabla.getColumnModel().getColumn(4).setCellRenderer(centrado);
        }
    }

    private void ocultarColumna(JTable tabla, int indice) {
        if (tabla.getColumnModel().getColumnCount() > indice) {
            tabla.getColumnModel().getColumn(indice).setMinWidth(0);
            tabla.getColumnModel().getColumn(indice).setMaxWidth(0);
            tabla.getColumnModel().getColumn(indice).setPreferredWidth(0);
        }
    }

    private void estilizarBoton(JButton boton, Color fondo) {
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
