import java.sql.*;
import java.nio.file.*;
import java.util.*;

/** Consulta de solo lectura. Las credenciales se reciben exclusivamente del entorno. */
public class DatabaseProbe {
    static String json(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
    public static void main(String[] args) throws Exception {
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            System.err.println("Se requiere una cuenta con contraseña no vacía.");
            System.exit(2);
        }
        String[] databases = {"bd_categoria", "bd_cliente", "bd_producto", "bd_pedido"};
        List<String> snapshots = new ArrayList<>();
        try {
            for (String db : databases) {
                try (Connection c = DriverManager.getConnection("jdbc:mariadb://localhost:3306/" + db + "?connectTimeout=5000", user, password)) {
                    List<String> tables = new ArrayList<>();
                    try (PreparedStatement q = c.prepareStatement("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_TYPE='BASE TABLE' ORDER BY TABLE_NAME")) {
                        q.setString(1, db);
                        try (ResultSet rs = q.executeQuery()) { while (rs.next()) tables.add(rs.getString(1)); }
                    }
                    List<String> counts = new ArrayList<>();
                    for (String table : tables) {
                        try (Statement q = c.createStatement(); ResultSet rs = q.executeQuery("SELECT COUNT(*) FROM `" + table.replace("`", "``") + "`")) {
                            rs.next(); counts.add(json(table) + ":" + rs.getLong(1));
                        }
                    }
                    snapshots.add("{\"database\":" + json(c.getCatalog()) + ",\"serverVersion\":" + json(c.getMetaData().getDatabaseProductVersion()) + ",\"tables\":{" + String.join(",", counts) + "}}");
                }
            }
            Files.writeString(Path.of(args[0]), "{\"timestamp\":" + json(java.time.Instant.now().toString()) + ",\"host\":\"localhost\",\"port\":3306,\"databases\":[" + String.join(",", snapshots) + "]}\n");
            System.out.println("Autenticación correcta. Las cuatro bases existen y se consultaron sin modificar datos.");
        } catch (SQLException ex) {
            // No imprimir mensajes del driver que pudieran contener datos de configuración.
            System.err.println("No se pudo verificar MariaDB. SQLState=" + ex.getSQLState() + "; código=" + ex.getErrorCode());
            System.exit(3);
        }
    }
}
