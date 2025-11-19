package Admin.Analytics;

import Admin.UserManagement.FirebaseClient;
import java.util.HashMap;
import java.util.Map;

public class QuantityAnalyticsService {

    private final FirebaseClient fb;

    public QuantityAnalyticsService(FirebaseClient fb) {
        this.fb = fb;
    }

    public AnalyticsReport generateReport() throws Exception {
        AnalyticsReport report = new AnalyticsReport();

        Map<String, Object> usuariosPorRol = fb.listAll("Usuarios");
        report.conteoPorRol = new HashMap<>();
        report.totalUsuarios = 0;

        for (String rol : usuariosPorRol.keySet()) {
            Object dataRol = usuariosPorRol.get(rol);
            int cantidad = 0;

            if (dataRol instanceof Map) {
                Map<String, Object> usuariosEnRol = (Map<String, Object>) dataRol;
                cantidad = usuariosEnRol.size();
            } else if (dataRol instanceof java.util.List) {
                java.util.List<?> usuariosEnRol = (java.util.List<?>) dataRol;
                cantidad = usuariosEnRol.size();
            }

            report.conteoPorRol.put(rol, cantidad);
            report.totalUsuarios += cantidad;
        }

        Map<String, Object> cursos = fb.listAll("Cursos");
        report.totalCursos = (cursos != null) ? cursos.size() : 0;

        Map<String, Object> asignaturas = fb.listAll("Asignaturas");
        report.totalAsignaturas = (asignaturas != null) ? asignaturas.size() : 0;

        report.clasesPorAsignatura = new HashMap<>();
        report.clasesPorDocente = new HashMap<>();
        report.totalClases = 0;

        Map<String, Object> clases = fb.listAll("Clases");

        if (clases != null && !clases.isEmpty()) {
            report.totalClases = clases.size();

            Map<String, String> codigoANombreAsignatura = new HashMap<>();
            if (asignaturas != null) {
                for(Object asigObj : asignaturas.values()) {
                    if (asigObj instanceof Map) {
                        Map<String, Object> asig = (Map<String, Object>) asigObj;
                        String codigo = (String) asig.get("codigo");
                        String nombre = (String) asig.get("nombre");
                        if (codigo != null && nombre != null) {
                            codigoANombreAsignatura.put(codigo, nombre);
                        }
                    }
                }
            }

            // 3. Iterar sobre cada clase para hacer los conteos
            for (Object claseObj : clases.values()) {
                if (claseObj == null || !(claseObj instanceof Map)) continue;

                Map<String, Object> claseData = (Map<String, Object>) claseObj;

                String codigoAsignatura = (String) claseData.get("asignaturaCodigo");
                String nombreDocente = (String) claseData.get("docenteNombre");

                if (codigoAsignatura != null) {
                    String nombreAsignatura = codigoANombreAsignatura.getOrDefault(codigoAsignatura, codigoAsignatura);

                    report.clasesPorAsignatura.merge(nombreAsignatura, 1, Integer::sum);
                }

                if (nombreDocente == null || nombreDocente.isEmpty()) {
                    nombreDocente = "Sin Asignar";
                }
                report.clasesPorDocente.merge(nombreDocente, 1, Integer::sum);
            }
        }

        return report;
    }
}