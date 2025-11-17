package Admin.UserManagement;

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

public class FirebaseClient {
    private final String baseUrl;
    private final String node;
    private final String auth;
    private final HttpClient http;
    private final Gson gson = new Gson();

    public FirebaseClient(String conexionTxtPath) throws IOException {
        Properties p = new Properties();
        try (var r = Files.newBufferedReader(Path.of(conexionTxtPath), StandardCharsets.UTF_8)) {
            p.load(r);
        }
        this.baseUrl = trimEndSlash(Objects.requireNonNull(p.getProperty("DATABASE_URL"), "DATABASE_URL requerido en conexion.txt"));
        this.node = Objects.requireNonNull(p.getProperty("NODE"), "NODE requerido en conexion.txt");
        this.auth = p.getProperty("AUTH");
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }
    public Map<String, Object> listAll(String coleccion) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(coleccion)))
                .GET().timeout(Duration.ofSeconds(20)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            if (res.body() == null || res.body().equals("null") || res.body().isBlank()) {
                // ✅ Colección vacía, devolver mapa vacío sin lanzar error
                return Map.of();
            }

            // Si Firebase devolvió un objeto de error (por ejemplo {"error": "..."}), evitar el crash
            if (res.body().contains("\"error\"")) {
                throw new IOException("Firebase devolvió un error: " + res.body());
            }

            Type t = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> map = gson.fromJson(res.body(), t);
            return map != null ? map : Map.of();
        } else {
            throw new IOException("Error al obtener colección " + coleccion + ": HTTP " + res.statusCode());
        }
    }
    private String trimEndSlash(String s) {
        return (s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
    }

    private String url(String path) {
        String u = baseUrl + "/" + path + ".json";
        if (auth != null && !auth.isBlank()) u += "?auth=" + auth;
        return u;
    }
    public Map<String, Object> get(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(path)))
                .GET().timeout(Duration.ofSeconds(20)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) {
            if (res.body() == null || res.body().equals("null") || res.body().isBlank()) {
                return Map.of();
            }

            if (res.body().contains("\"error\"")) {
                throw new IOException("Firebase devolvió un error para la ruta " + path + ": " + res.body());
            }

            Type t = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> map = gson.fromJson(res.body(), t);
            return map != null ? map : Map.of();
        } else {
            throw new IOException("Error al obtener datos de la ruta " + path + ": HTTP " + res.statusCode() + " -> " + res.body());
        }
    }

    public Map<String, Persona> listAll() throws IOException, InterruptedException {
        Map<String, Persona> result = new LinkedHashMap<>();

        String[] subNodes = {"Administradores", "Estudiantes", "Docentes", "Acudientes"};

        for (String sub : subNodes) {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url(node + "/" + sub)))
                    .GET().timeout(Duration.ofSeconds(20)).build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200 && res.body() != null && !res.body().equals("null")) {
                Type t = new TypeToken<Map<String, Persona>>() {}.getType();
                Map<String, Persona> map = gson.fromJson(res.body(), t);
                if (map != null) {
                    map.forEach((k, v) -> {
                        if (v != null) {
                            v.id = k;
                            result.put(k, v);
                        }
                    });
                }
            }
        }
        return result;
    }

    public String createInNode(String subNode, Persona persona) throws IOException, InterruptedException {
        String body = gson.toJson(persona);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(node + "/" + subNode)))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20)).build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 200) {
            Map<?, ?> resp = gson.fromJson(res.body(), Map.class);
            return (String) resp.get("name");
        }
        throw new IOException("Error al crear: HTTP " + res.statusCode() + " -> " + res.body());
    }

    public void updateInNode(String subNode, String id, Persona persona) throws IOException, InterruptedException {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id requerido");
        persona.id = null;
        String body = gson.toJson(persona);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(node + "/" + subNode + "/" + id)))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20)).build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("Error al actualizar: HTTP " + res.statusCode() + " -> " + res.body());
        }
    }
    public void updateGenericNode(String fullPath, Map<String, Object> data) throws IOException, InterruptedException {
        String body = gson.toJson(data);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(fullPath)))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20)).build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("Error al actualizar nodo genérico: HTTP " + res.statusCode() + " -> " + res.body());
        }
    }
    public void deleteInNode(String subNode, String id) throws IOException, InterruptedException {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id requerido");

        String fullPath = node + "/" + subNode + "/" + id;

        HttpRequest req = HttpRequest.newBuilder(URI.create(url(fullPath)))
                .DELETE().timeout(Duration.ofSeconds(20)).build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new IOException("Error al eliminar: HTTP " + res.statusCode() + " -> " + res.body());
        }
    }

}