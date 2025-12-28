package ltd.opens.mg.mc.client.gui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ltd.opens.mg.mc.MaingraphforMC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Executors;

public class BlueprintWebServer {
    private static HttpServer server;
    private static int port = 8666;
    private static Path dataFile = Paths.get("blueprint_data.json");
    private static String lastSavedJson = "{\"raw\":{\"nodes\":[],\"connections\":[]}}";
    private static String playerName = "Unknown";
    private static String playerUuid = "";

    public static void setPlayerInfo(String name, String uuid) {
        playerName = name;
        playerUuid = uuid;
        MaingraphforMC.LOGGER.info("Web Server Player Info updated: {} ({})", name, uuid);
    }

    public static void setSavePath(Path path) {
        dataFile = path.resolve("blueprint_data.json");
        MaingraphforMC.LOGGER.info("Blueprint save path set to: {}", dataFile.toAbsolutePath());
        if (Files.exists(dataFile)) {
            try {
                lastSavedJson = Files.readString(dataFile);
                MaingraphforMC.LOGGER.info("Loaded blueprint data from world save");
            } catch (IOException e) {
                MaingraphforMC.LOGGER.error("Failed to load blueprint data from {}", dataFile, e);
            }
        } else {
            lastSavedJson = "{\"raw\":{\"nodes\":[],\"connections\":[]}}";
        }
    }

    public static void start() {
        if (server != null) return;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Centralized Router
            server.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) path = "/blueprint.html";
                
                MaingraphforMC.LOGGER.info("Web Server Request: {}", path);
                
                if (path.startsWith("/api/")) {
                    handleApi(exchange, path);
                } else {
                    handleStatic(exchange, path);
                }
            });

            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            MaingraphforMC.LOGGER.info("Blueprint Web Server started on http://localhost:" + port);
        } catch (IOException e) {
            MaingraphforMC.LOGGER.error("Failed to start Blueprint Web Server", e);
        }
    }

    private static void handleApi(HttpExchange exchange, String path) throws IOException {
        MaingraphforMC.LOGGER.info("API Request: {} {}", exchange.getRequestMethod(), path);
        if (path.equals("/api/save") && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            byte[] body = exchange.getRequestBody().readAllBytes();
            lastSavedJson = new String(body);
            MaingraphforMC.LOGGER.info("Saving blueprint data, length: {}", lastSavedJson.length());
            try {
                Files.writeString(dataFile, lastSavedJson);
                MaingraphforMC.LOGGER.info("Successfully saved to {}", dataFile.toAbsolutePath());
                sendJsonResponse(exchange, 200, "{\"status\":\"ok\",\"message\":\"Saved successfully\"}");
            } catch (IOException e) {
                MaingraphforMC.LOGGER.error("Failed to write save file", e);
                sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        } else if (path.equals("/api/load") && "GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            MaingraphforMC.LOGGER.info("Loading blueprint data");
            
            // Cleanly inject metadata into the saved JSON
            String metadataJson = String.format(
                "\"metadata\":{\"playerName\":\"%s\",\"playerUuid\":\"%s\",\"savePath\":\"%s\"}",
                playerName,
                playerUuid,
                dataFile.toAbsolutePath().toString().replace("\\", "/")
            );

            String responseJson;
            if (lastSavedJson.trim().startsWith("{")) {
                // If it's already an object, insert metadata at the start
                responseJson = "{" + metadataJson + "," + lastSavedJson.substring(1);
            } else {
                // Fallback for empty/invalid file
                responseJson = "{" + metadataJson + "}";
            }
            
            sendJsonResponse(exchange, 200, responseJson);
        } else {
            MaingraphforMC.LOGGER.warn("Unhandled API request: {} {}", exchange.getRequestMethod(), path);
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private static void handleStatic(HttpExchange exchange, String path) throws IOException {
        MaingraphforMC.LOGGER.info("Static Request: {}", path);
        String resourcePath = "/web" + path;
        InputStream is = BlueprintWebServer.class.getResourceAsStream(resourcePath);
        byte[] content = null;

        if (is != null) {
            content = is.readAllBytes();
            is.close();
        } else {
            Path filePath = Paths.get(".").resolve(path.substring(1));
            if (Files.exists(filePath)) {
                content = Files.readAllBytes(filePath);
            }
        }

        if (content != null) {
            String contentType = URLConnection.guessContentTypeFromName(path);
            if (contentType == null) {
                if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".css")) contentType = "text/css";
                else contentType = "text/html";
            }
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes();
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static String getUrl() {
        return "http://localhost:" + port + "/blueprint.html";
    }

    public static void executeBlueprint(String eventType, String name, String[] args) {
        if (lastSavedJson != null && !lastSavedJson.isEmpty()) {
            BlueprintEngine.execute(lastSavedJson, eventType, name, args);
        }
    }
}
