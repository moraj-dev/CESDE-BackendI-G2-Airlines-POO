package core.database;

import java.sql.Connection;

public class TestConection {
    public static void main(String[] args) {
        System.out.println("Intentando conectar a PostgreSQL...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ ¡Conexión exitosa a la base de datos de Aerolíneas!");
            } else {
                System.out.println("❌ La conexión es nula.");
            }
        } catch (Exception e) {
            System.out.println("🛑 Error de conexión: " + e.getMessage());
        }
    }
}
