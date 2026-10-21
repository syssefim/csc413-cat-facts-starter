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
        CREATE TABLE if NOT EXISTS facts (
            id   INTEGER PRIMARY KEY AUTOINCREMENT,
            fact TEXT    NOT NULL
        )
        """;

    public static void main(String[] args) throws IOException, SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTable(conn);
            System.out.println("Table ready");

            // TODO (Section 3): call copyFacts here.
        }
    }

    /** Section 2: creates the facts table. */
    static void createTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
        }
    }

    /** Section 3: copies every fact from facts.csv into the facts table, as one transaction. */
    static void copyFacts(Connection conn) throws IOException, SQLException {
        App.loadFromCsv();
        System.out.println("Read " + App.facts.size() + " facts from facts.csv");

        // TODO: turn off auto-commit, so nothing is saved until you commit.

        String sql = "INSERT INTO facts (id, fact) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int inserted = 0;
            for (App.CatFact fact : App.facts) {
                // TODO: fill the two ? placeholders with this fact's id and text.

                // TODO: run the insert, and add the number of rows it changed to inserted.

            }
            // TODO: make every insert permanent, all at once.

            System.out.println("Migrated " + inserted + " facts");
        } catch (SQLException e) {
            // TODO: undo every insert made since auto-commit was turned off.

            System.out.println("Migration failed, rolled back. Nothing was saved.");
            throw e;
        }
    }
}
