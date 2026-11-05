package Conexion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a la base de datos.
 *
 * Los datos del servidor se pueden cambiar SIN RECOMPILAR editando el archivo
 * {@code config.properties} que debe estar en la carpeta del proyecto (o junto
 * al .jar). También se pueden definir con variables de entorno:
 *
 *   THEFHUD_DB_HOST, THEFHUD_DB_PORT, THEFHUD_DB_NAME,
 *   THEFHUD_DB_USER, THEFHUD_DB_PASSWORD
 *
 * Si no existe config.properties ni variables de entorno, se usan los valores
 * por defecto (la VM actual).
 */
public class BD {

    // IP actual de la VM. Se puede cambiar sin recompilar en config.properties.
    private static final String DEFAULT_HOST = "192.168.10.243";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DB = "tienda_suplementos";
    private static final String DEFAULT_USER = "app_user";
    private static final String DEFAULT_PASSWORD = "password_seguro";

    private static Properties config = null;

    public static Connection getConexion() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(getUrl(), getUser(), getPassword());
            System.out.println("----------------------------------------");
            System.out.println("¡CONEXIÓN EXITOSA A " + getDatabase().toUpperCase() + "!");
            System.out.println("Servidor: " + getHost() + ":" + getPort());
            System.out.println("----------------------------------------");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Falta el driver mysql-connector-j.jar en Libraries");
        } catch (SQLException e) {
            System.out.println("Error de SQL: " + e.getMessage());
            System.out.println("Revisa config.properties (db.host, db.port, db.user, db.password).");
        }
        return con;
    }

    public static String getUrl() {
        return "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabase()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static String getHost() {
        return getEnvOrConfig("THEFHUD_DB_HOST", "db.host", DEFAULT_HOST);
    }

    public static String getPort() {
        return getEnvOrConfig("THEFHUD_DB_PORT", "db.port", String.valueOf(DEFAULT_PORT));
    }

    public static String getDatabase() {
        return getEnvOrConfig("THEFHUD_DB_NAME", "db.name", DEFAULT_DB);
    }

    public static String getUser() {
        return getEnvOrConfig("THEFHUD_DB_USER", "db.user", DEFAULT_USER);
    }

    public static String getPassword() {
        return getEnvOrConfig("THEFHUD_DB_PASSWORD", "db.password", DEFAULT_PASSWORD);
    }

    private static String getEnvOrConfig(String envName, String propName, String defaultValue) {
        String env = System.getenv(envName);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String prop = getConfig().getProperty(propName);
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        return defaultValue;
    }

    private static synchronized Properties getConfig() {
        if (config != null) {
            return config;
        }
        config = new Properties();
        File archivo = buscarArchivoConfig();
        if (archivo != null) {
            try (InputStream in = new FileInputStream(archivo)) {
                config.load(in);
                System.out.println("Configuración cargada desde: " + archivo.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("No se pudo leer " + archivo + ": " + e.getMessage());
            }
        } else {
            System.out.println("No se encontró config.properties. Usando valores por defecto.");
        }
        return config;
    }

    private static File buscarArchivoConfig() {
        String rutaExplicita = System.getProperty("thefhud.config");
        if (rutaExplicita != null && !rutaExplicita.trim().isEmpty()) {
            File f = new File(rutaExplicita);
            if (f.isFile()) {
                return f;
            }
        }

        String userDir = System.getProperty("user.dir", ".");
        File[] candidatos = {
            new File(userDir, "config.properties"),
            new File(userDir, ".." + File.separator + "config.properties"),
            new File(System.getProperty("user.home", "."), "config.properties")
        };
        for (File f : candidatos) {
            if (f.isFile()) {
                return f;
            }
        }
        return null;
    }

    /** Vuelve a leer config.properties en la próxima conexión. */
    public static synchronized void recargarConfiguracion() {
        config = null;
    }

    public static void main(String[] args) {
        getConexion(); // Para probar dando Shift + F6
    }
}
