package Login.Functionality;

import Teachers.Screen.DocentePantalla;
import Student.Screen.EstudiantePantalla;
import Parents.Screen.AcudientePantalla;
import Admin.Screen.AdministradorPantalla; // Asegúrate de importar esta si existe
import javax.swing.*;
import java.awt.*;
import java.io.IOException; // Necesario para manejar la excepción

public class Redirecttopage {

    // El método debe estar en un bloque try-catch dentro del Runnable
    public static void redirigirPorRol(String rol, String nombre) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = null;

            try { // Bloque try para capturar la IOException
                switch (rol.toLowerCase()) {
                    case "administradore":
                    case "administrador":
                        // Asegúrate de que AdministradorPantalla(String nombre) lance IOException
                        frame = new Admin.Screen.AdministradorPantalla(nombre);
                        break;
                    case "docente":
                        // Asegúrate de que DocentePantalla(String nombre) lance IOException
                        frame = new Teachers.Screen.DocentePantalla(nombre);
                        break;
                    case "estudiante":
                        // Asegúrate de que EstudiantePantalla(String nombre) lance IOException
                        frame = new Student.Screen.EstudiantePantalla(nombre);
                        break;
                    case "acudiente":
                        // Asegúrate de que AcudientePantalla(String nombre) lance IOException
                        frame = new Parents.Screen.AcudientePantalla(nombre);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Rol no reconocido: " + rol, "Error de Redirección", JOptionPane.WARNING_MESSAGE);
                        return;
                }

                if (frame != null) {
                    frame.setVisible(true);
                }
            } catch (IOException e) {
                // Manejo de error si FirebaseClient falla
                JOptionPane.showMessageDialog(null,
                        "Error de conexión con la base de datos para el rol " + rol + ": " + e.getMessage(),
                        "Error de Inicialización",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}