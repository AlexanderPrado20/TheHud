package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BD {

    // URL con parámetros obligatorios para MySQL 8.0+
    private static final String URL = "jdbc:mysql://localhost:3306/tienda_suplementos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static Connection getConexion() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("----------------------------------------");
            System.out.println("¡CONEXIÓN EXITOSA A TIENDA_SUPLEMENTOS!");
            System.out.println("----------------------------------------");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Falta el driver mysql-connector-j.jar en Libraries");
        } catch (SQLException e) {
            System.out.println("Error de SQL: " + e.getMessage());
        }
        return con;
    }

    public static void main(String[] args) {
        getConexion(); // Para probar dando Shift + F6
    }
}