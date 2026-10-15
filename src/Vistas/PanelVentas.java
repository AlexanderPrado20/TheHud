package vistas;

import Conexion.BD;
import Modelos.GeneradorTicket;
import Vistas.DialogoBuscarProducto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;


public class PanelVentas extends javax.swing.JPanel {

    DefaultTableModel modeloTabla;

    // Datos del último ticket generado, para poder reimprimirlo
    private DefaultTableModel ultimoTicketModelo = null;
    private int ultimoIdVenta = 0;
    private double ultimoTotal = 0;
    private double ultimoPagoCon = 0;
    private double ultimoCambio = 0;
    private String ultimoMetodoPago = "";
    private String ultimoCliente = "";

    // Columnas del carrito: 0=Código, 1=Producto, 2=Cantidad, 3=Precio U., 4=Subtotal, 5=Descuento %
    private static final int COL_CODIGO = 0;
    private static final int COL_PRODUCTO = 1;
    private static final int COL_CANTIDAD = 2;
    private static final int COL_PRECIO = 3;
    private static final int COL_SUBTOTAL = 4;
    private static final int COL_DESCUENTO = 5;

    // Prefijo del código con el que se identifican los combos en el carrito
    private static final String COMBO_PREFIJO = "COMBO-";

    // Grupo de botones de pago (fuera del código generado para que no se pierda al regenerar)
    private final javax.swing.ButtonGroup grupoPago = new javax.swing.ButtonGroup();


    public PanelVentas() {
        initComponents();

        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaVentas.getSelectedRow() != -1) {
                int fila = tablaVentas.getSelectedRow();
                String codigo = tablaVentas.getValueAt(fila, COL_CODIGO).toString();
                String nombre = tablaVentas.getValueAt(fila, COL_PRODUCTO).toString();
                actualizarImagenProducto(codigo, nombre);
                actualizarDetallesProducto(codigo);
            }
        });
        configurarTabla();
        configurarEstiloPago();
        configurarMetodoPago();
        configurarAtajos();
        configurarIconos();
    }

    // <editor-fold defaultstate="collapsed" desc="Logica del carrito">
    private void buscarYAgregarProducto(String codigo) {
        if (codigo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un código de producto.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT id_producto, codigo_barras, nombre, precio_venta, stock FROM productos WHERE codigo_barras = ?";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String codBarras = rs.getString("codigo_barras");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio_venta");
                int stock = rs.getInt("stock");

                if (stock <= 0) {
                    JOptionPane.showMessageDialog(this, "Producto sin stock disponible.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean encontrado = false;

                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    String codEnTabla = modeloTabla.getValueAt(i, COL_CODIGO).toString();
                    if (codEnTabla.equals(codBarras)) {
                        int cantActual = getInt(i, COL_CANTIDAD);
                        if (cantActual + 1 > stock) {
                            JOptionPane.showMessageDialog(this, "No hay suficiente stock para agregar más.", "Límite de Stock", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        double desc = getDouble(i, COL_DESCUENTO);
                        int nuevaCant = cantActual + 1;
                        double nuevoSubtotal = calcularSubtotal(nuevaCant, precio, desc);

                        modeloTabla.setValueAt(nuevaCant, i, COL_CANTIDAD);
                        modeloTabla.setValueAt(nuevoSubtotal, i, COL_SUBTOTAL);
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado) {
                    modeloTabla.addRow(new Object[]{codBarras, nombre, 1, precio, precio, 0.0});
                }

                calcularTotal();
                txtCodigoBarras.setText("");
                txtCodigoBarras.requestFocus();

            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al consultar la BD: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Agrega una promoción/combo al carrito. La fila se identifica con el
     * prefijo "COMBO-" y al cobrar se registra en la tabla detalle_combos
     * (los combos no descuentan stock de productos).
     */
    public void agregarCombo(int idCombo, String nombre, double precio) {
        String codigo = COMBO_PREFIJO + idCombo;

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String codEnTabla = modeloTabla.getValueAt(i, COL_CODIGO).toString();
            if (codEnTabla.equals(codigo)) {
                int cantActual = getInt(i, COL_CANTIDAD);
                int nuevaCant = cantActual + 1;
                double desc = getDouble(i, COL_DESCUENTO);
                modeloTabla.setValueAt(nuevaCant, i, COL_CANTIDAD);
                modeloTabla.setValueAt(calcularSubtotal(nuevaCant, precio, desc), i, COL_SUBTOTAL);
                calcularTotal();
                return;
            }
        }

        modeloTabla.addRow(new Object[]{codigo, nombre, 1, precio, precio, 0.0});
        calcularTotal();
        txtCodigoBarras.requestFocus();
    }

    /** Recalcula el subtotal de una fila usando cantidad, precio y descuento (%). */
    private void recalcularFila(int fila) {        int cant = getInt(fila, COL_CANTIDAD);
        double precio = getDouble(fila, COL_PRECIO);
        double desc = getDouble(fila, COL_DESCUENTO);
        modeloTabla.setValueAt(calcularSubtotal(cant, precio, desc), fila, COL_SUBTOTAL);
    }

    private double calcularSubtotal(int cant, double precio, double descPct) {
        double sub = cant * precio;
        if (descPct > 0) {
            sub = sub * (1.0 - (descPct / 100.0));
        }
        return sub;
    }

    private int getInt(int fila, int col) {
        Object v = modeloTabla.getValueAt(fila, col);
        return (v == null) ? 0 : (int) Double.parseDouble(v.toString());
    }

    private double getDouble(int fila, int col) {
        Object v = modeloTabla.getValueAt(fila, col);
        return (v == null) ? 0.0 : Double.parseDouble(v.toString());
    }

    /** Consulta el stock actual de un producto por código de barras. */
    private int obtenerStock(String codigoBarras) {
        String sql = "SELECT stock FROM productos WHERE codigo_barras = ?";
        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoBarras);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("stock");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener stock: " + e.getMessage());
        }
        return -1;
    }

    private void aumentarCantidad() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la lista.");
            return;
        }
        String cod = modeloTabla.getValueAt(fila, COL_CODIGO).toString();
        int cant = getInt(fila, COL_CANTIDAD);
        int stock = obtenerStock(cod);
        if (stock >= 0 && cant + 1 > stock) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock para agregar más.", "Límite de Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloTabla.setValueAt(cant + 1, fila, COL_CANTIDAD);
        recalcularFila(fila);
        calcularTotal();
    }

    private void disminuirCantidad() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la lista.");
            return;
        }
        int cant = getInt(fila, COL_CANTIDAD);
        if (cant <= 1) {
            // Al quedar en cero, se elimina la fila del carrito
            modeloTabla.removeRow(fila);
        } else {
            modeloTabla.setValueAt(cant - 1, fila, COL_CANTIDAD);
            recalcularFila(fila);
        }
        calcularTotal();
    }

    private void aplicarDescuento() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para aplicar el descuento.");
            return;
        }
        String entrada = JOptionPane.showInputDialog(this, "Descuento (%) para el producto seleccionado:", "0");
        if (entrada == null) {
            return;
        }
        try {
            double desc = Double.parseDouble(entrada.trim().replace(",", "."));
            if (desc < 0 || desc > 100) {
                JOptionPane.showMessageDialog(this, "El descuento debe estar entre 0 y 100.", "Valor inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
            modeloTabla.setValueAt(desc, fila, COL_DESCUENTO);
            recalcularFila(fila);
            calcularTotal();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPorNombre() {
        java.awt.Window ventana = javax.swing.SwingUtilities.getWindowAncestor(this);
        java.awt.Frame owner = (ventana instanceof java.awt.Frame) ? (java.awt.Frame) ventana : null;
        DialogoBuscarProducto dialogo = new DialogoBuscarProducto(owner, codigo -> buscarYAgregarProducto(codigo));
        dialogo.setVisible(true);
    }

    private void vaciarCarrito() {
        if (modeloTabla.getRowCount() == 0) {
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Deseas vaciar todo el carrito?",
                "Vaciar Carrito", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar == JOptionPane.YES_OPTION) {
            modeloTabla.setRowCount(0);
            calcularTotal();
            limpiarVistaPrevia();
        }
    }

    private void guardarCotizacion() {
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío, no hay nada que cotizar.", "Carrito vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        GeneradorTicket.generarCotizacion(copiarModelo(), obtenerTotal(), txtCliente.getText());
    }

    private void reimprimirUltimoTicket() {
        if (ultimoTicketModelo == null) {
            JOptionPane.showMessageDialog(this, "Todavía no se ha generado ningún ticket en esta sesión.", "Sin tickets", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GeneradorTicket.imprimirTicket(ultimoIdVenta, ultimoTicketModelo, ultimoTotal, ultimoPagoCon, ultimoCambio, ultimoMetodoPago, ultimoCliente);
    }

    private DefaultTableModel copiarModelo() {
        DefaultTableModel copia = new DefaultTableModel(null, new String[]{
            "Código", "Producto", "Cantidad", "Precio U.", "Subtotal", "Descuento"
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Object[] fila = new Object[modeloTabla.getColumnCount()];
            for (int c = 0; c < modeloTabla.getColumnCount(); c++) {
                fila[c] = modeloTabla.getValueAt(i, c);
            }
            copia.addRow(fila);
        }
        return copia;
    }

    private double obtenerTotal() {
        double total = 0.0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            total += getDouble(i, COL_SUBTOTAL);
        }
        return total;
    }

    private void limpiarVistaPrevia() {
        if (lblImagen != null) {
            lblImagen.setIcon(null);
            lblImagen.setText("Sin imagen");
        }
        if (lblNombreProduct != null) {
            lblNombreProduct.setText("Seleccione un artículo");
        }
        if (txtDetallesProducto != null) {
            txtDetallesProducto.setText("");
        }
    }

    private void limpiarPantalla() {
        modeloTabla.setRowCount(0);
        lblTotal.setText("$ 0.00 MXN");
        lblCambio.setText("$ 0.00 MXN");
        lblCambio.setForeground(java.awt.Color.WHITE);
        txtPagaCon.setText("");
        txtCodigoBarras.setText("");
        txtCliente.setText("");
        rbEfectivo.setSelected(true);
        limpiarVistaPrevia();
        txtCodigoBarras.requestFocus();
    }

    /** Da al total un aspecto llamativo dentro de la paleta teal del programa. */
    private void configurarEstiloPago() {
        lblTotal.setOpaque(true);
        lblTotal.setBackground(new java.awt.Color(0, 153, 153));
        lblTotal.setForeground(java.awt.Color.WHITE);
        lblTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 34));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTotal.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 204, 204), 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 14, 6, 14)));

        lblCambio.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        lblCambio.setForeground(java.awt.Color.WHITE);

        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLabel2.setForeground(java.awt.Color.WHITE);
    }

    /** Agrupa Efectivo/Tarjeta y deja Efectivo seleccionado por defecto. */
    private void configurarMetodoPago() {
        grupoPago.add(rbEfectivo);
        grupoPago.add(rbTargeta);
        rbEfectivo.setSelected(true);
    }

    /** Atajos de teclado del punto de venta. */
    private void configurarAtajos() {
        javax.swing.InputMap im = getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = getActionMap();

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "atajoBuscar");
        am.put("atajoBuscar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtCodigoBarras.requestFocusInWindow();
                txtCodigoBarras.selectAll();
            }
        });

        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0), "atajoCobrar");
        am.put("atajoCobrar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnCobrar.doClick();
            }
        });

        // Supr elimina la fila solo cuando la tabla tiene el foco
        tablaVentas.getInputMap(javax.swing.JComponent.WHEN_FOCUSED)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "atajoEliminarFila");
        tablaVentas.getActionMap().put("atajoEliminarFila", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnEliminarActionPerformed(null);
            }
        });
    }

    /** Actualiza el cambio mostrado y le da color verde cuando es válido. */
    private void actualizarCambio(double cambio) {
        if (cambio >= 0) {
            lblCambio.setText(String.format("$ %.2f MXN", cambio));
            lblCambio.setForeground(new java.awt.Color(0, 230, 118)); // verde claro
        } else {
            lblCambio.setText("$ 0.00 MXN");
            lblCambio.setForeground(java.awt.Color.WHITE);
        }
    }
    // </editor-fold>

    public void aplicarEstiloBoton(javax.swing.JButton boton, java.awt.Color colorFondo, java.awt.Color colorTexto) {
        boton.setBackground(colorFondo);
        boton.setForeground(colorTexto);
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        boton.setFocusPainted(false);       // Quita el recuadro punteado al hacer clic
        boton.setBorderPainted(false);      // Quita el borde 3D viejo
        boton.setOpaque(true);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    /** Coloca íconos Material en los botones principales del punto de venta. */
    private void configurarIconos() {
        ponerIcono(btnBuscarNombre, MaterialDesign.MDI_MAGNIFY, 16);
        ponerIcono(btnAgregar, MaterialDesign.MDI_CART_PLUS, 16);
        ponerIcono(btnMasCantidad, MaterialDesign.MDI_PLUS, 16);
        ponerIcono(btnMenosCantidad, MaterialDesign.MDI_MINUS, 16);
        ponerIcono(btnEliminar, MaterialDesign.MDI_DELETE, 16);
        ponerIcono(btnVaciarCarrito, MaterialDesign.MDI_DELETE_SWEEP, 16);
        ponerIcono(btnCancelar, MaterialDesign.MDI_CLOSE, 16);
        ponerIcono(btnDescuento, MaterialDesign.MDI_PERCENT, 16);
        ponerIcono(btnGuardarCotizacion, MaterialDesign.MDI_CONTENT_SAVE, 16);
        ponerIcono(btnReimprimirTicket, MaterialDesign.MDI_PRINTER, 16);
        ponerIcono(btnCobrar, MaterialDesign.MDI_CASH, 18);
    }

    private void ponerIcono(javax.swing.JButton boton, org.kordamp.ikonli.Ikon icono, int tamano) {
        if (boton == null) {
            return;
        }
        boton.setIcon(FontIcon.of(icono, tamano, java.awt.Color.WHITE));
        boton.setIconTextGap(6);
    }

    private void calcularTotal() {
        lblTotal.setText(String.format("$ %.2f MXN", obtenerTotal()));
    }

    private JPanel crearPanelVistaPrevia() {
        pnlpre = new JPanel(new BorderLayout(5, 5));
        pnlpre.setBackground(Color.WHITE);
        pnlpre.setBorder(BorderFactory.createTitledBorder("Producto Seleccionado"));
        pnlpre.setPreferredSize(new Dimension(200, 240));

        // Label para la foto del producto
        lblImagen = new JLabel("Sin imagen", SwingConstants.CENTER);
        lblImagen.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblImagen.setForeground(Color.GRAY);

        // Label inferior para el nombre
        lblNombreProduct = new JLabel("Seleccione un artículo", SwingConstants.CENTER);
        lblNombreProduct.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblNombreProduct.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        pnlpre.add(lblImagen, BorderLayout.CENTER);
        pnlpre.add(lblNombreProduct, BorderLayout.SOUTH);

        return pnlpre;
    }

    public void actualizarImagenProducto(String codigoProducto, String nombreProducto) {
        if (codigoProducto == null || codigoProducto.trim().isEmpty()) {
            lblImagen.setIcon(null);
            lblImagen.setText("Foto no disponible");
            return;
        }

        lblNombreProduct.setText(nombreProducto);
        String codigoLimpio = codigoProducto.trim();

        // Imprimir en la consola para depurar
        System.out.println("Buscando imagen para el código: '" + codigoLimpio + "'");

        java.net.URL urlImagen = getClass().getResource("/Imagenes/" + codigoLimpio + ".png");

        if (urlImagen == null) {
            urlImagen = getClass().getResource("/Imagenes/" + codigoLimpio + ".jpg");
        }

        if (urlImagen != null) {
            ImageIcon iconOriginal = new ImageIcon(urlImagen);

            int ancho = lblImagen.getWidth() > 0 ? lblImagen.getWidth() : 180;
            int alto = lblImagen.getHeight() > 0 ? lblImagen.getHeight() : 220;

            Image imgEscalada = iconOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            lblImagen.setIcon(new ImageIcon(imgEscalada));
            lblImagen.setText("");
        } else {
            lblImagen.setIcon(null);
            lblImagen.setText("Foto no disponible");
            System.out.println("ERROR: No se encontró la ruta /Imagenes/" + codigoLimpio + ".png");
        }
    }

    private void configurarTabla() {
        String[] columnas = {"Código", "Producto", "Cantidad", "Precio U.", "Subtotal", "Descuento %"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVentas.setModel(modeloTabla);
    }

    public void actualizarDetallesProducto(String codigoProducto) {
        if (codigoProducto == null || codigoProducto.trim().isEmpty()) {
            txtDetallesProducto.setText("Seleccione un producto.");
            return;
        }

        String sql = "SELECT porciones, cafeina, descripcion FROM productos WHERE codigo_barras = ?";

        try (Connection con = BD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigoProducto.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String p = rs.getString("porciones");
                String c = rs.getString("cafeina");
                String d = rs.getString("descripcion");

                String valP = (p != null) ? p : "N/D";
                String valC = (c != null) ? c : "N/D";
                String valD = (d != null) ? d : "Sin descripción disponible.";

                String detalle = "INFORMACIÓN TÉCNICA\n"
                        + "----------------------------------------\n"
                        + "• Porciones: " + valP + "\n"
                        + "• Cafeína: " + valC + "\n\n"
                        + "Descripción:\n" + valD;

                txtDetallesProducto.setText(detalle);
                txtDetallesProducto.setCaretPosition(0);

            } else {
                txtDetallesProducto.setText("Sin detalles registrados en BD.");
            }

        } catch (Exception e) {
            System.out.println("Error al cargar detalles: " + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelIzquierdo = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtCodigoBarras = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        btnBuscarNombre = new javax.swing.JButton();
        lblCliente = new javax.swing.JLabel();
        txtCliente = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaVentas = new javax.swing.JTable();
        jPanelBotonesCarrito = new javax.swing.JPanel();
        pnlpre = new javax.swing.JPanel();
        lblImagen = new javax.swing.JLabel();
        lblNombreProduct = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDetallesProducto = new javax.swing.JTextArea();
        btnMasCantidad = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnReimprimirTicket = new javax.swing.JButton();
        btnGuardarCotizacion = new javax.swing.JButton();
        btnVaciarCarrito = new javax.swing.JButton();
        btnDescuento = new javax.swing.JButton();
        btnMenosCantidad = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        rbEfectivo = new javax.swing.JRadioButton();
        rbTargeta = new javax.swing.JRadioButton();
        txtPagaCon = new javax.swing.JTextField();
        lblCambio = new javax.swing.JLabel();
        btnCobrar = new javax.swing.JButton();

        panelIzquierdo.setBackground(new java.awt.Color(153, 153, 153));

        jLabel7.setText("Buscar / Código de Barras");

        jLabel8.setText("LOGO");

        txtCodigoBarras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoBarrasActionPerformed(evt);
            }
        });

        btnAgregar.setBackground(new java.awt.Color(102, 102, 102));
        btnAgregar.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setText("Agregar");
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        btnBuscarNombre.setBackground(new java.awt.Color(102, 102, 102));
        btnBuscarNombre.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnBuscarNombre.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscarNombre.setText("Buscar por Nombre");
        btnBuscarNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarNombreActionPerformed(evt);
            }
        });

        lblCliente.setText("Cliente:");

        tablaVentas.setBackground(new java.awt.Color(102, 102, 102));
        tablaVentas.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        tablaVentas.setForeground(new java.awt.Color(255, 255, 255));
        tablaVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Producto", "Cantidad", "Precio U.", "Subtotal", "Descuento %"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaVentas);

        jPanelBotonesCarrito.setBackground(new java.awt.Color(153, 153, 153));
        jPanelBotonesCarrito.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));

        javax.swing.GroupLayout pnlpreLayout = new javax.swing.GroupLayout(pnlpre);
        pnlpre.setLayout(pnlpreLayout);
        pnlpreLayout.setHorizontalGroup(
            pnlpreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlpreLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblImagen, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlpreLayout.setVerticalGroup(
            pnlpreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlpreLayout.createSequentialGroup()
                .addComponent(lblImagen)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        lblNombreProduct.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblNombreProduct.setText("Producto:");

        txtDetallesProducto.setColumns(20);
        txtDetallesProducto.setLineWrap(true);
        txtDetallesProducto.setRows(5);
        txtDetallesProducto.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtDetallesProducto);

        btnMasCantidad.setBackground(new java.awt.Color(102, 102, 102));
        btnMasCantidad.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnMasCantidad.setForeground(new java.awt.Color(255, 255, 255));
        btnMasCantidad.setText("+ Cantidad");
        btnMasCantidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMasCantidadActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(102, 102, 102));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar Producto");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(102, 102, 102));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cancelar Venta");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnReimprimirTicket.setBackground(new java.awt.Color(102, 102, 102));
        btnReimprimirTicket.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnReimprimirTicket.setForeground(new java.awt.Color(255, 255, 255));
        btnReimprimirTicket.setText("Reimprimir Último Ticket");
        btnReimprimirTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReimprimirTicketActionPerformed(evt);
            }
        });

        btnGuardarCotizacion.setBackground(new java.awt.Color(102, 102, 102));
        btnGuardarCotizacion.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnGuardarCotizacion.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarCotizacion.setText("Guardar Cotización");
        btnGuardarCotizacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarCotizacionActionPerformed(evt);
            }
        });

        btnVaciarCarrito.setBackground(new java.awt.Color(102, 102, 102));
        btnVaciarCarrito.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnVaciarCarrito.setForeground(new java.awt.Color(255, 255, 255));
        btnVaciarCarrito.setText("Vaciar Carrito");
        btnVaciarCarrito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVaciarCarritoActionPerformed(evt);
            }
        });

        btnDescuento.setBackground(new java.awt.Color(102, 102, 102));
        btnDescuento.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnDescuento.setForeground(new java.awt.Color(255, 255, 255));
        btnDescuento.setText("Descuento %");
        btnDescuento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDescuentoActionPerformed(evt);
            }
        });

        btnMenosCantidad.setBackground(new java.awt.Color(102, 102, 102));
        btnMenosCantidad.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        btnMenosCantidad.setForeground(new java.awt.Color(255, 255, 255));
        btnMenosCantidad.setText("- Cantidad");
        btnMenosCantidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMenosCantidadActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 102, 102));

        jLabel1.setText("PUNTO DE VENTA - ");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("TOTAL A PAGAR");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("MÉTODO DE PAGO");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("PAGA CON");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("CAMBIO");

        jLabel6.setText("THE FITNESS FUEL HUB");

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 34)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(255, 255, 255));
        lblTotal.setText("$ 0.00 MXN");

        rbEfectivo.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        rbEfectivo.setForeground(new java.awt.Color(255, 255, 255));
        rbEfectivo.setText("Efectivo");

        rbTargeta.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        rbTargeta.setForeground(new java.awt.Color(255, 255, 255));
        rbTargeta.setText("Tarjeta");

        txtPagaCon.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        txtPagaCon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPagaConActionPerformed(evt);
            }
        });

        lblCambio.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblCambio.setForeground(new java.awt.Color(255, 255, 255));
        lblCambio.setText("$ 0.00 MXN");

        btnCobrar.setBackground(new java.awt.Color(0, 153, 153));
        btnCobrar.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCobrar.setForeground(new java.awt.Color(255, 255, 255));
        btnCobrar.setText("COBRAR (F12)");
        btnCobrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCobrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(lblCambio)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(rbEfectivo)
                            .addComponent(rbTargeta)
                            .addComponent(jLabel2)
                            .addComponent(txtPagaCon, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTotal)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(58, 58, 58)
                .addComponent(jLabel2)
                .addGap(29, 29, 29)
                .addComponent(lblTotal)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(rbEfectivo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbTargeta)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(txtPagaCon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblCambio)
                .addGap(18, 18, 18)
                .addComponent(btnCobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(206, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelIzquierdoLayout = new javax.swing.GroupLayout(panelIzquierdo);
        panelIzquierdo.setLayout(panelIzquierdoLayout);
        panelIzquierdoLayout.setHorizontalGroup(
            panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelIzquierdoLayout.createSequentialGroup()
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7)
                            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAgregar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBuscarNombre)
                                .addGap(18, 18, 18)
                                .addComponent(lblNombreProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                .addComponent(lblCliente)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                .addComponent(pnlpre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 10, Short.MAX_VALUE))
                    .addGroup(panelIzquierdoLayout.createSequentialGroup()
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnMasCantidad)
                            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                .addComponent(jPanelBotonesCarrito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnMenosCantidad)))
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                .addGap(66, 66, 66)
                                .addComponent(btnDescuento)
                                .addGap(62, 62, 62)
                                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnVaciarCarrito)
                                    .addComponent(btnCancelar))
                                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                        .addGap(101, 101, 101)
                                        .addComponent(btnGuardarCotizacion))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelIzquierdoLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnReimprimirTicket))))
                            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnEliminar)
                                .addGap(461, 461, 461)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
        );
        panelIzquierdoLayout.setVerticalGroup(
            panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIzquierdoLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregar)
                    .addComponent(btnBuscarNombre)
                    .addComponent(lblNombreProduct))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCliente)
                    .addComponent(txtCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(pnlpre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelIzquierdoLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jPanelBotonesCarrito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelIzquierdoLayout.createSequentialGroup()
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnMenosCantidad)
                            .addComponent(btnDescuento))
                        .addGap(18, 18, 18)
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEliminar)
                            .addComponent(btnMasCantidad)))
                    .addGroup(panelIzquierdoLayout.createSequentialGroup()
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnVaciarCarrito)
                            .addComponent(btnGuardarCotizacion))
                        .addGap(18, 18, 18)
                        .addGroup(panelIzquierdoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnReimprimirTicket)
                            .addComponent(btnCancelar))))
                .addContainerGap(101, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelIzquierdo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelIzquierdo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCobrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCobrarActionPerformed

        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        double total = obtenerTotal();
        double pagaCon;
        String txtPago = txtPagaCon.getText().trim().replace(",", ".");
        if (txtPago.isEmpty()) {
            pagaCon = total;
        } else {
            try {
                pagaCon = Double.parseDouble(txtPago);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El monto en 'Paga con' no es válido. Escribe solo números.", "Monto inválido", JOptionPane.ERROR_MESSAGE);
                txtPagaCon.requestFocusInWindow();
                return;
            }
        }
        double cambio = pagaCon - total;

        if (pagaCon < total) {
            JOptionPane.showMessageDialog(this, "El monto pagado es menor al total.", "Error de cobro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String metodoPago = rbEfectivo.isSelected() ? "Efectivo" : "Tarjeta";
        String cliente = txtCliente.getText().trim();

        int confirmar = JOptionPane.showConfirmDialog(this,
            String.format("¿Confirmar venta?\n\nTotal: $%.2f\nMétodo de pago: %s\nRecibe: $%.2f\nCambio: $%.2f",
                total, metodoPago, pagaCon, cambio),
            "Confirmar Venta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        Connection con = null;
        try {
            con = BD.getConexion();
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar Encabezado de Venta
            String sqlVenta = "INSERT INTO ventas (total, pago_con, cambio, metodo_pago) VALUES (?, ?, ?, ?)";
            PreparedStatement psVenta = con.prepareStatement(sqlVenta, java.sql.Statement.RETURN_GENERATED_KEYS);
            psVenta.setDouble(1, total);
            psVenta.setDouble(2, pagaCon);
            psVenta.setDouble(3, cambio);
            psVenta.setString(4, metodoPago);
            psVenta.executeUpdate();

            ResultSet rsKey = psVenta.getGeneratedKeys();
            int idVenta = 0;
            if (rsKey.next()) {
                idVenta = rsKey.getInt(1);
            }

            // PrepareStatements para Detalle, Stock y Consulta de ID por Código de Barras
            String sqlSelectId = "SELECT id_producto FROM productos WHERE codigo_barras = ?";
            String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";
            String sqlDetalleCombo = "INSERT INTO detalle_combos (id_venta, id_combo, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement psSelectId = con.prepareStatement(sqlSelectId);
            PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
            PreparedStatement psStock = con.prepareStatement(sqlStock);
            PreparedStatement psDetalleCombo = con.prepareStatement(sqlDetalleCombo);

            // 2. Recorrer la tabla del carrito
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                String codBarras = modeloTabla.getValueAt(i, COL_CODIGO).toString();
                int cant = getInt(i, COL_CANTIDAD);
                double precioU = getDouble(i, COL_PRECIO);
                double subtotal = getDouble(i, COL_SUBTOTAL);

                // Los combos/promociones se guardan aparte y no descuentan stock.
                if (codBarras.startsWith(COMBO_PREFIJO)) {
                    int idCombo = Integer.parseInt(codBarras.substring(COMBO_PREFIJO.length()));
                    psDetalleCombo.setInt(1, idVenta);
                    psDetalleCombo.setInt(2, idCombo);
                    psDetalleCombo.setInt(3, cant);
                    psDetalleCombo.setDouble(4, precioU);
                    psDetalleCombo.setDouble(5, subtotal);
                    psDetalleCombo.executeUpdate();
                    continue;
                }

                // Obtener id_producto mediante el código de barras
                psSelectId.setString(1, codBarras);
                ResultSet rsProd = psSelectId.executeQuery();
                int idProd = 0;
                if (rsProd.next()) {
                    idProd = rsProd.getInt("id_producto");
                }
                rsProd.close();

                // Insertar Detalle
                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, idProd);
                psDetalle.setInt(3, cant);
                psDetalle.setDouble(4, precioU);
                psDetalle.setDouble(5, subtotal);
                psDetalle.executeUpdate();

                // Descontar Inventario
                psStock.setInt(1, cant);
                psStock.setInt(2, idProd);
                psStock.executeUpdate();
            }

            con.commit(); // Confirmar transacción

            // Guardar datos del ticket para poder reimprimirlo después
            ultimoTicketModelo = copiarModelo();
            ultimoIdVenta = idVenta;
            ultimoTotal = total;
            ultimoPagoCon = pagaCon;
            ultimoCambio = cambio;
            ultimoMetodoPago = metodoPago;
            ultimoCliente = cliente;

            GeneradorTicket.imprimirTicket(idVenta, modeloTabla, total, pagaCon, cambio, metodoPago, cliente);

            limpiarPantalla();

            JOptionPane.showMessageDialog(this, "¡Venta realizada con éxito!\nCambio: $" + String.format("%.2f", cambio));

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(this, "Error al procesar la venta: " + e.getMessage());
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }//GEN-LAST:event_btnCobrarActionPerformed

    private void txtPagaConActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPagaConActionPerformed

        try {
            double total = obtenerTotal();
            double pagaCon = Double.parseDouble(txtPagaCon.getText().trim().replace(",", "."));
            actualizarCambio(pagaCon - total);
        } catch (NumberFormatException e) {
            lblCambio.setText("$ 0.00 MXN");
            lblCambio.setForeground(java.awt.Color.WHITE);
        }
    }//GEN-LAST:event_txtPagaConActionPerformed

    private void btnMenosCantidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenosCantidadActionPerformed
        disminuirCantidad();
    }//GEN-LAST:event_btnMenosCantidadActionPerformed

    private void btnDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDescuentoActionPerformed
        aplicarDescuento();
    }//GEN-LAST:event_btnDescuentoActionPerformed

    private void btnVaciarCarritoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVaciarCarritoActionPerformed
        vaciarCarrito();
    }//GEN-LAST:event_btnVaciarCarritoActionPerformed

    private void btnGuardarCotizacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarCotizacionActionPerformed
        guardarCotizacion();
    }//GEN-LAST:event_btnGuardarCotizacionActionPerformed

    private void btnReimprimirTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReimprimirTicketActionPerformed
        reimprimirUltimoTicket();
    }//GEN-LAST:event_btnReimprimirTicketActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Desea cancelar la venta actual?",
            "Cancelar Venta",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmar == JOptionPane.YES_OPTION) {
            limpiarPantalla();
        }
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed

        int fila = tablaVentas.getSelectedRow();
        if (fila >= 0) {
            modeloTabla.removeRow(fila);
            calcularTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la lista para eliminar.");
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnMasCantidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMasCantidadActionPerformed
        aumentarCantidad();
    }//GEN-LAST:event_btnMasCantidadActionPerformed

    private void btnBuscarNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarNombreActionPerformed
        buscarPorNombre();
    }//GEN-LAST:event_btnBuscarNombreActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed

        buscarYAgregarProducto(txtCodigoBarras.getText());
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void txtCodigoBarrasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoBarrasActionPerformed
        buscarYAgregarProducto(txtCodigoBarras.getText());
    }//GEN-LAST:event_txtCodigoBarrasActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscarNombre;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCobrar;
    private javax.swing.JButton btnDescuento;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardarCotizacion;
    private javax.swing.JButton btnMasCantidad;
    private javax.swing.JButton btnMenosCantidad;
    private javax.swing.JButton btnReimprimirTicket;
    private javax.swing.JButton btnVaciarCarrito;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelBotonesCarrito;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCambio;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblImagen;
    private javax.swing.JLabel lblNombreProduct;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelIzquierdo;
    private javax.swing.JPanel pnlpre;
    private javax.swing.JRadioButton rbEfectivo;
    private javax.swing.JRadioButton rbTargeta;
    private javax.swing.JTable tablaVentas;
    private javax.swing.JTextField txtCliente;
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JTextArea txtDetallesProducto;
    private javax.swing.JTextField txtPagaCon;
    // End of variables declaration//GEN-END:variables
}
