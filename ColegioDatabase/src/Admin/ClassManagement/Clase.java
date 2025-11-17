package Admin.ClassManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Clase {
    public String id;
    public String codigo;
    public String asignaturaCodigo;
    public String docenteId;
    public String docenteNombre;
    public String salon;
    public int capacidad;
    public List<Horario> horarios;
    public List<String> estudiantesIds;


    public Clase() {
        this.horarios = new ArrayList<>();
        this.estudiantesIds = new ArrayList<>();
    }

    public Clase(String codigo, String asignaturaCodigo, String docenteId, String docenteNombre, String salon, int capacidad, List<Horario> horarios) {
        this.codigo = codigo;
        this.asignaturaCodigo = asignaturaCodigo;
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
        this.salon = salon;
        this.capacidad = capacidad;
        this.horarios = horarios;
        this.estudiantesIds = new ArrayList<>();
    }


    public String getCodigo() { return codigo; }
    public String getAsignaturaCodigo() { return asignaturaCodigo; }
    public String getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
    public String getSalon() { return salon; }
    public int getCapacidad() { return capacidad; }
    public List<Horario> getHorarios() { return horarios; }
    public List<String> getEstudiantesIds() { return estudiantesIds; }


    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setAsignaturaCodigo(String asignaturaCodigo) { this.asignaturaCodigo = asignaturaCodigo; }
    public void setDocenteId(String docenteId) { this.docenteId = docenteId; }
    public void setDocenteNombre(String docenteNombre) { this.docenteNombre = docenteNombre; }
    public void setSalon(String salon) { this.salon = salon; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public void setHorarios(List<Horario> horarios) { this.horarios = horarios; }
    public void setEstudiantesIds(List<String> estudiantesIds) { this.estudiantesIds = estudiantesIds; }


    public static class Horario {
        public String dia; // Lunes, Martes, etc.
        public String horaInicio; // e.g., "1:00 PM"
        public String horaFin; // e.g., "3:00 PM"


        public Horario() {}

        public Horario(String dia, String horaInicio, String horaFin) {
            this.dia = dia;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }

        @Override
        public String toString() {
            return dia + ": " + horaInicio + " - " + horaFin;
        }


        public String getDia() { return dia; }
        public String getHoraInicio() { return horaInicio; }
        public String getHoraFin() { return horaFin; }


        public void setDia(String dia) { this.dia = dia; }
        public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
        public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
    }
}