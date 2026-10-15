package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PruebaConexion {

    // Configura tus datos de MySQL aquí
    private static final String URL = "jdbc:mysql://localhost:3306/tienda_suplementos?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";     // Cambia por tu usuario de MySQL
    private static final String PASSWORD = "";     // Cambia por tu contraseña de MySQL

    public static void main(String[] args) {
        System.out.println("Intentando conectar a la base de datos MySQL...");
        
        try {
            // 1. Cargar el Driver JDBC de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Intentar establecer la conexión
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            
            if (con != null && !con.isClosed()) {
                System.out.println("--------------------------------------------------");
                System.out.println("¡CONEXIÓN EXITOSA CON LA BASE DE DATOS!");
                System.out.println("Base de datos conectada: tienda_suplementos");
                System.out.println("--------------------------------------------------");
                
                // Cerrar la conexión de prueba
                con.close();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: No se encontró el driver JDBC de MySQL.");
            System.err.println("Asegúrate de haber agregado 'mysql-connector-j.jar' en la carpeta Libraries del proyecto.");
        } catch (SQLException e) {
            System.err.println("❌ ERROR DE CONEXIÓN SQL: " + e.getMessage());
            System.err.println("Verifica que el servicio de MySQL esté iniciado y que el usuario/contraseña o puerto sean correctos.");
        }
    }
}