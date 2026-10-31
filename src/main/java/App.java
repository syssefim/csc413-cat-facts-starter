import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cat Facts API, backed by a CSV file.
 *
 *   GET  /facts        every fact
 *   GET  /facts/{id}   one fact, or 404
 *   POST /facts        add a fact; body is {"fact": "..."}
 */
public class App {

    static final int PORT = 8080;
    static final Path CSV_FILE = Path.of("facts.csv");
    static final String DB_URL = "jdbc:sqlite:catfacts.db";

    record CatFact(int id, String fact) {}
    record NewFact(String fact) {}
    record ErrorBody(String error) {}

    static final List<CatFact> facts = new ArrayList<>();
    static int nextId = 1;

    /** Reads every line of facts.csv into the in-memory list. */
    static void loadFromCsv() throws IOException {
        for (String line : Files.readAllLines(CSV_FILE, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            // Limit of 2: split at the FIRST comma only, so commas in the fact survive.
            String[] parts = line.split(",", 2);
            int id = Integer.parseInt(parts[0].trim());
            String fact = parts[1].trim();
            facts.add(new CatFact(id, fact));
            nextId = Math.max(nextId, id + 1);
        }
    }

    /** Appends one fact to the end of facts.csv as "id,fact". */
    

    static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()   // keep ' and = readable instead of '
            .create();

    public static void main(String[] args) throws IOException {


        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/facts", new FactsHandler());
        server.start();
        System.out.println("Cat Facts API running on http://localhost:" + PORT + "/facts");
    }

    static class FactsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                route(exchange);
            } catch (Exception e) {
                // Print the real problem in the server terminal, send a clean 500 to the client.
                e.printStackTrace();
                sendJson(exchange, 500, new ErrorBody("internal server error"));
            }
        }

        /** Decides which endpoint a request is for. */
        void route(HttpExchange exchange) throws IOException, SQLException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // "/facts" or "/facts/"  ->  returns the whole collection
            if (path.equals("/facts") || path.equals("/facts/")) {
                switch (method) {
                    case "GET"  -> sendJson(exchange, 200, getAllFacts(DB_URL));
                    case "POST" -> createFact(exchange);
                    default     -> sendJson(exchange, 405, new ErrorBody("method not allowed"));
                }
                return;
            }

            // Anything else that merely starts with "/facts", like "/factsabc"
            if (!path.startsWith("/facts/")) {
                sendJson(exchange, 404, new ErrorBody("not found"));
                return;
            }

            // "/facts/{id}"  ->  returns one fact with the provided id
            String idText = path.substring("/facts/".length());
            int id;
            try {
                id = Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, new ErrorBody("id must be a whole number"));
                return;
            }
            if (method.equals("GET")) {
                Optional<CatFact> found = findFactById(DB_URL, id);
                if (found.isPresent()) {
                    sendJson(exchange, 200, found.get());
                } else {
                    sendJson(exchange, 404, new ErrorBody("no fact with id " + id));
                }
            } else if (method.equals("DELETE")) {
                deleteFact(exchange, id);
            } else {
                sendJson(exchange, 405, new ErrorBody("method not allowed"));
            }
        }

        /** Reads every fact from the database at dbUrl. */
        List<CatFact> getAllFacts(String dbUrl) throws SQLException {
            List<CatFact> result = new ArrayList<>();
            String sql = "SELECT id, fact FROM facts ORDER BY id";

            try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new CatFact(rs.getInt("id"), rs.getString("fact")));
                }
            }

            return result;
        }

        /** Looks up one fact in the database at dbUrl; empty if no row has that id. */
        Optional<CatFact> findFactById(String dbUrl, int id) throws SQLException {
            String sql = "SELECT id, fact FROM facts WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(dbUrl);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        CatFact fact = new CatFact(rs.getInt("id"), rs.getString("fact"));
                        return Optional.of(fact);
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }

        /** DELETE /facts/{id} */
        void deleteFact(HttpExchange exchange, int id)
                throws IOException, SQLException {
            String sql = "DELETE FROM facts WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                int deleted = pstmt.executeUpdate();

                if (deleted == 0) {
                    sendJson(exchange, 404, new ErrorBody("no fact with id " + id));
                } else {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                }
            }
        }

        /** POST /facts */
        void createFact(HttpExchange exchange) throws IOException, SQLException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            NewFact input;
            try {
                input = gson.fromJson(body, NewFact.class);
            } catch (JsonParseException e) {
                sendJson(exchange, 400, new ErrorBody("body must be JSON like {\"fact\": \"...\"}"));
                return;
            }
            if (input == null || input.fact() == null || input.fact().isBlank()) {
                sendJson(exchange, 400, new ErrorBody("\"fact\" is required and cannot be blank"));
                return;
            }
            String text = input.fact().trim();
            

            CatFact created;
            String sql = "INSERT INTO facts (fact) VALUES (?) RETURNING id";

            try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, text);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("INSERT did not return an id");
                    }
                    created = new CatFact(rs.getInt("id"), text);
                }
            }

            exchange.getResponseHeaders().set("Location", "/facts/" + created.id());
            sendJson(exchange, 201, created);
        }

        // ---- Helper ------------------------------------------------------

        /** Converts any object to JSON with Gson and sends it with the given status. */
        void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
            byte[] bytes = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }
}
