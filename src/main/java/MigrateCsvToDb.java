import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * One-shot program: creates the facts table and copies every fact from
 * facts.csv into it. Run it on its own; it is not part of the API.
 */
public class MigrateCsvToDb {

    static final String DB_URL = "jdbc:sqlite:catfacts.db";

    static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS facts (
            id   INTEGER PRIMARY KEY AUTOINCREMENT,
            fact TEXT    NOT NULL
        )
        """;

    public static void main(String[] args) throws IOException, SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTable(conn);
            System.out.println("Table ready");
            copyFacts(conn);
        }
    }

    static void createTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
        }
    }

    static void copyFacts(Connection conn) throws IOException, SQLException {
        App.loadFromCsv();
        System.out.println("Read " + App.facts.size() + " facts from facts.csv");

        conn.setAutoCommit(false);
        String sql = "INSERT INTO facts (id, fact) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int inserted = 0;
            for (App.CatFact fact : App.facts) {
                pstmt.setInt(1, fact.id());
                pstmt.setString(2, fact.fact());
                inserted += pstmt.executeUpdate();
            }
            conn.commit();
            System.out.println("Migrated " + inserted + " facts");
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Migration failed, rolled back. Nothing was saved.");
            throw e;
        }
    }
}
