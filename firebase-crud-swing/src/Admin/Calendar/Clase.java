package Admin.Calendar;

import java.util.List;
public class Clase {
    private String asignaturaCodigo;
    private String codigo;
    private List<String> cursos;
    private String docenteId;
    private String docenteNombre;
    private List<HorarioClase> horarios;
    private String salon;

    public Clase() {
        // Constructor vacío requerido por Firebase
    }

    // Getters y Setters
    public String getAsignaturaCodigo() { return asignaturaCodigo; }
    public void setAsignaturaCodigo(String asignaturaCodigo) { this.asignaturaCodigo = asignaturaCodigo; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public List<String> getCursos() { return cursos; }
    public void setCursos(List<String> cursos) { this.cursos = cursos; }

    public String getDocenteId() { return docenteId; }
    public void setDocenteId(String docenteId) { this.docenteId = docenteId; }

    public String getDocenteNombre() { return docenteNombre; }
    public void setDocenteNombre(String docenteNombre) { this.docenteNombre = docenteNombre; }

    public List<HorarioClase> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioClase> horarios) { this.horarios = horarios; }

    public String getSalon() { return salon; }
    public void setSalon(String salon) { this.salon = salon; }
}
