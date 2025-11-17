package Admin.UserManagement;

public class Persona {
    public String id;
    public String nombre;
    public String correo;
    public int edad;
    public String documento;
    public String rol;
    public String codigoAcceso; // <<--- NUEVO

    public Persona(String nombre, String correo, int edad, String documento, String rol, String codigoAcceso) {
        this.nombre = nombre;
        this.correo = correo;
        this.edad = edad;
        this.documento = documento;
        this.rol = rol;
        this.codigoAcceso = codigoAcceso;
    }
}