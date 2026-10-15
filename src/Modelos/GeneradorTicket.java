/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelos;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.DefaultTableModel;

public class GeneradorTicket {

    /**
     * Genera y permite imprimir el ticket de una venta.
     *
     * @param idVenta    Folio de la venta
     * @param modeloTabla Modelo de la tabla del carrito
     * @param total      Total pagado
     * @param pagaCon    Monto con el que pago el cliente
     * @param cambio     Cambio entregado
     * @param metodoPago Metodo de pago (Efectivo / Tarjeta)
     * @param cliente    Nombre del cliente (puede ser vacio o null)
     */
    public static void imprimirTicket(int idVenta, DefaultTableModel modeloTabla, double total, double pagaCon, double cambio, String metodoPago, String cliente) {
        StringBuilder ticket = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fecha = sdf.format(new Date());

        // Encabezado
        ticket.append("=========================================\n");
        ticket.append("        THE FITNESS FUEL HUB             \n");
        ticket.append("   Suplementos y Nutricion Deportiva     \n");
        ticket.append("=========================================\n");
        ticket.append("Folio Venta: #").append(idVenta).append("\n");
        ticket.append("Fecha: ").append(fecha).append("\n");
        if (cliente != null && !cliente.trim().isEmpty()) {
            ticket.append("Cliente: ").append(cliente.trim()).append("\n");
        }
        ticket.append("Atendido por: Caja 1\n");
        ticket.append("-----------------------------------------\n");
        ticket.append(String.format("%-20s %-4s %-7s %-7s\n", "Producto", "Cant", "Precio", "Subt"));
        ticket.append("-----------------------------------------\n");

        // Productos del Carrito (Mapeados a las columnas del carrito)
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String nombre = String.valueOf(modeloTabla.getValueAt(i, 1));
            if (nombre.length() > 18) {
                nombre = nombre.substring(0, 18); // Recortar si el nombre es muy largo
            }
            int cant = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
            double precio = Double.parseDouble(modeloTabla.getValueAt(i, 3).toString());
            double subtotal = Double.parseDouble(modeloTabla.getValueAt(i, 4).toString());

            ticket.append(String.format("%-20s %-4d $%-6.2f $%-6.2f\n", nombre, cant, precio, subtotal));
        }

        // Totales y Pago
        ticket.append("-----------------------------------------\n");
        ticket.append(String.format("TOTAL:                  $%.2f MXN\n", total));
        ticket.append(String.format("Metodo de Pago:         %s\n", metodoPago));
        ticket.append(String.format("Pago Con:               $%.2f MXN\n", pagaCon));
        ticket.append(String.format("Cambio:                 $%.2f MXN\n", cambio));
        ticket.append("=========================================\n");
        ticket.append("       ¡Gracias por tu compra!           \n");
        ticket.append("     Siguenos en redes sociales          \n");
        ticket.append("=========================================\n");

        // Mostrar vista previa y/o enviar a impresora
        JTextArea textArea = new JTextArea(ticket.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 9));

        // Preguntar si desea imprimir
        int opcion = JOptionPane.showConfirmDialog(null, new JScrollPane(textArea), "Ticket de Venta - Folio #" + idVenta, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opcion == JOptionPane.OK_OPTION) {
            try {
                boolean complete = textArea.print();
                if (complete) {
                    JOptionPane.showMessageDialog(null, "Ticket enviado a la impresora.", "Impresión", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null, "Error al imprimir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Genera una cotizacion (venta sin afectar inventario) en un archivo de texto.
     *
     * @param modeloTabla Modelo de la tabla del carrito
     * @param total       Total de la cotizacion
     * @param cliente     Nombre del cliente (puede ser vacio o null)
     */
    public static void generarCotizacion(DefaultTableModel modeloTabla, double total, String cliente) {
        StringBuilder texto = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat sdfArchivo = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date ahora = new Date();

        texto.append("=========================================\n");
        texto.append("        THE FITNESS FUEL HUB             \n");
        texto.append("          COTIZACION (PROFORMA)          \n");
        texto.append("=========================================\n");
        texto.append("Fecha: ").append(sdf.format(ahora)).append("\n");
        if (cliente != null && !cliente.trim().isEmpty()) {
            texto.append("Cliente: ").append(cliente.trim()).append("\n");
        }
        texto.append("Atendido por: Caja 1\n");
        texto.append("-----------------------------------------\n");
        texto.append(String.format("%-20s %-4s %-7s %-7s\n", "Producto", "Cant", "Precio", "Subt"));
        texto.append("-----------------------------------------\n");

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String nombre = String.valueOf(modeloTabla.getValueAt(i, 1));
            if (nombre.length() > 18) {
                nombre = nombre.substring(0, 18);
            }
            int cant = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
            double precio = Double.parseDouble(modeloTabla.getValueAt(i, 3).toString());
            double subtotal = Double.parseDouble(modeloTabla.getValueAt(i, 4).toString());
            texto.append(String.format("%-20s %-4d $%-6.2f $%-6.2f\n", nombre, cant, precio, subtotal));
        }

        texto.append("-----------------------------------------\n");
        texto.append(String.format("TOTAL:                  $%.2f MXN\n", total));
        texto.append("=========================================\n");
        texto.append("   Esta cotizacion NO es un comprobante   \n");
        texto.append("        de venta. Precios sujetos a       \n");
        texto.append("            cambio sin previo aviso.      \n");
        texto.append("=========================================\n");

        String nombreArchivo = "cotizacion_" + sdfArchivo.format(ahora) + ".txt";
        String ruta = System.getProperty("user.home") + File.separator + nombreArchivo;

        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {
            pw.print(texto.toString());
            int ver = JOptionPane.showConfirmDialog(null,
                    "Cotización guardada en:\n" + ruta + "\n\n¿Desea ver el contenido?",
                    "Cotización Guardada", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (ver == JOptionPane.YES_OPTION) {
                JTextArea area = new JTextArea(texto.toString());
                area.setFont(new Font("Monospaced", Font.PLAIN, 9));
                area.setEditable(false);
                JOptionPane.showMessageDialog(null, new JScrollPane(area), "Cotización", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar la cotización: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
