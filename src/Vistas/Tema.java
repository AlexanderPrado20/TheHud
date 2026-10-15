package Vistas;

/**
 * Tema visual centralizado de la aplicación (FlatLaf oscuro - Darcula).
 *
 * Debe llamarse una sola vez, antes de crear cualquier componente Swing.
 */
public final class Tema {

    private Tema() {
    }

    public static void aplicar() {
        try {
            com.formdev.flatlaf.FlatDarculaLaf.setup();
        } catch (Exception ex) {
            try {
                javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
            } catch (Exception ignored) {
                System.err.println("No se pudo iniciar FlatLaf: " + ex.getMessage());
            }
        }

        // Ajustes generales de la paleta oscura (bordes redondeados, scroll moderno)
        javax.swing.UIManager.put("Component.arc", 10);
        javax.swing.UIManager.put("Button.arc", 10);
        javax.swing.UIManager.put("TextComponent.arc", 8);
        javax.swing.UIManager.put("Component.focusWidth", 1);
        javax.swing.UIManager.put("ScrollBar.thumbArc", 999);
        javax.swing.UIManager.put("ScrollBar.width", 12);
        javax.swing.UIManager.put("Table.showHorizontalLines", true);
        javax.swing.UIManager.put("Table.showVerticalLines", false);
        javax.swing.UIManager.put("Table.rowHeight", 26);
    }
}
