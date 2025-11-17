package Login.Functionality;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class Firebaseconnect {
    private static final String DATABASE_URL = "https://schoolappsnet-poo-default-rtdb.firebaseio.com/Usuarios";
    private static final Gson gson = new Gson();

    public static Map<String, Object> verificarUsuario(String correo, String codigo) {
        try {
            String[] roles = {"Administradores", "Docentes", "Estudiantes", "Acudientes"};
            HttpClient client = HttpClient.newHttpClient();

            for (String rol : roles) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(DATABASE_URL + "/" + rol + ".json"))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body() != null && !response.body().equals("null")) {
                    Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
                    Map<String, Map<String, Object>> usuarios = gson.fromJson(response.body(), mapType);

                    for (Map.Entry<String, Map<String, Object>> entry : usuarios.entrySet()) {
                        Map<String, Object> userData = entry.getValue();

                        if (userData != null &&
                                correo.equals(userData.get("correo")) &&
                                codigo.equals(String.valueOf(userData.get("codigoAcceso")))) {

                            userData.put("rol", rol.substring(0, rol.length() - 1));
                            userData.put("uid", entry.getKey());
                            return userData;
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
