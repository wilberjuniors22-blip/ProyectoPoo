package Admin.ClassManagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class FirebaseConexion {

    private final String baseUrl;
    private final String auth;  // Token de autenticación (si aplica)
    private final HttpClient http;
    private final Gson gson = new Gson();

    /**
     * Constructor: carga la configuración desde un archivo "conexion.txt"
     * con claves DATABASE_URL y AUTH.
     */
    public FirebaseConexion(String conexionTxtPath) throws IOException {
        Properties p = new Properties();
        try (var r = Files.newBufferedReader(Path.of(conexionTxtPath), StandardCharsets.UTF_8)) {
            p.load(r);
        }

        this.baseUrl = trimEndSlash(Objects.requireNonNull(p.getProperty("DATABASE_URL"),
                "DATABASE_URL requerido en conexion.txt"));
        this.auth = p.getProperty("AUTH");
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    // =========================================================
    // MÉTODOS CRUD
    // =========================================================

    /**
     * Lista todos los objetos de un nodo (por ejemplo, "Clases" o "Asignaturas").
     */
    public Map<String, Map<String, Object>> listAll(String node) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(node)))
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new IOException("Error al obtener " + node + ": HTTP " + res.statusCode());
        }

        if (res.body() == null || res.body().equals("null") || res.body().isBlank()) {
            return Map.of();
        }

        if (res.body().contains("\"error\"")) {
            throw new IOException("Firebase devolvió un error: " + res.body());
        }

        Type t = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
        Map<String, Map<String, Object>> data = gson.fromJson(res.body(), t);
        return data != null ? data : Map.of();
    }

    public String createInNode(String node, Object data) throws IOException, InterruptedException {
        String body = gson.toJson(data);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(node)))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 200) {
            Map<?, ?> resp = gson.fromJson(res.body(), Map.class);
            return (String) resp.get("name");
        }

        throw new IOException("Error al crear en " + node + ": HTTP " + res.statusCode() + " -> " + res.body());
    }

    public void updateInNode(String node, String id, Map<String, Object> updates) throws IOException, InterruptedException {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id requerido");
        String body = gson.toJson(updates);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(node + "/" + id)))
                // PATCH para que Firebase haga un "update" (merge) y no reemplace todo el objeto
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("Error al actualizar: HTTP " + res.statusCode() + " -> " + res.body());
        }
    }

    public void deleteInNode(String node, String id) throws IOException, InterruptedException {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id requerido");

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(node + "/" + id)))
                .DELETE()
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("Error al eliminar: HTTP " + res.statusCode() + " -> " + res.body());
        }
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private String trimEndSlash(String s) {
        return (s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
    }

    private String url(String path) {
        String u = baseUrl + "/" + path + ".json";
        if (auth != null && !auth.isBlank()) u += "?auth=" + auth;
        return u;
    }
}
