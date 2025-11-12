package Admin.StudentManagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cursos {
    private static final Gson gson = new Gson();
    private static final String NODE = "Cursos";

    public static void inicializarCursos() throws IOException, InterruptedException {
        Map<String, Object> existentes = listarCursos();
        if (existentes != null && !existentes.isEmpty()) return; // ya están creados

        String[] niveles = {"1","2","3","4","5","6","7","8","9","10","11"};
        String[] sub = {"A","B"};

        for (String n : niveles) {
            for (String s : sub) {
                String nombre = n + s;
                FirebaseConnection.post(NODE, Map.of("nombre", nombre));
            }
        }
        System.out.println("✅ Cursos inicializados en Firebase.");
    }

    public static Map<String, Object> listarCursos() throws IOException, InterruptedException {
        String res = FirebaseConnection.get(NODE);
        if (res == null || res.equals("null")) return Map.of();
        Type t = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(res, t);
    }
}
