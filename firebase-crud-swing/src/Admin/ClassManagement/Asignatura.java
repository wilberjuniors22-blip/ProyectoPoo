package Admin.ClassManagement;

import java.util.ArrayList;
import java.util.List;

public class Asignatura {
    public String id; // ID asignada por Firebase
    public String codigo; // Codigo generado (e.g., CI0000)
    public String nombre;
    // Lista de IDs de las clases que pertenecen a esta asignatura
    public List<String> clasesIds;

    // Constructor vacío requerido por Firebase
    public Asignatura() {
        this.clasesIds = new ArrayList<>();
    }

    public Asignatura(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.clasesIds = new ArrayList<>();
    }

    // Getters necesarios para Firebase
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public List<String> getClasesIds() { return clasesIds; }

    // Setters (opcionales, pero buenas prácticas)
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setClasesIds(List<String> clasesIds) { this.clasesIds = clasesIds; }
}