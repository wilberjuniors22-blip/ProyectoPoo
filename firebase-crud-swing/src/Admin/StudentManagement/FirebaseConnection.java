package Admin.StudentManagement;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class FirebaseConnection {
    private static final String BASE_URL = "https://schoolappsnet-poo-default-rtdb.firebaseio.com/";
    private static final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final Gson gson = new Gson();

    private static String url(String path) {
        return BASE_URL + path + ".json";
    }

    public static String post(String path, Object data) throws IOException, InterruptedException {
        String body = gson.toJson(data);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(path)))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Error POST: " + res.body());
        Map<?, ?> m = gson.fromJson(res.body(), Map.class);
        return (String) m.get("name");
    }

    public static void patch(String path, Object data) throws IOException, InterruptedException {
        String body = gson.toJson(data);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(path)))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Error PATCH: " + res.body());
    }

    public static String get(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(path)))
                .GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Error GET: " + res.body());
        return res.body();
    }
}
