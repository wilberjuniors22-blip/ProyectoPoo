package Admin.Analytics;

import java.util.Map;

public class AnalyticsReport {

    public int totalUsuarios;
    public int totalCursos;
    public int totalAsignaturas;
    public int totalClases;

    public Map<String, Integer> conteoPorRol;
    public Map<String, Integer> clasesPorAsignatura;
    public Map<String, Integer> clasesPorDocente;
}