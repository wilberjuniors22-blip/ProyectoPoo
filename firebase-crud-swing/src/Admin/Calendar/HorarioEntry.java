package Admin.Calendar;

/**
 * Representa un horario dentro de una clase (día, horaInicio, horaFin).
 */
public class HorarioEntry {
    private String dia;
    private String horaFin;
    private String horaInicio;

    public HorarioEntry() {} // Requerido por Firebase

    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    @Override
    public String toString() {
        return dia + " " + horaInicio + "-" + horaFin;
    }
}
