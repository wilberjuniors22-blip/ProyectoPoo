package Login.Functionality;

import Teachers.Screen.DocentePantalla;
import Student.Screen.EstudiantePantalla;
import Parents.Screen.AcudientePantalla;
import Admin.Screen.AdministradorPantalla;

import javax.swing.*;
import java.io.IOException;

public class Redirecttopage {

    public static void redirigirPorRol(String rol, String nombre, String usuarioId) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = null;

            try {
                switch (rol.toLowerCase()) {
                    case "administrador":
                    case "administradore":
                        frame = new AdministradorPantalla(nombre);
                        break;

                    case "docente":
                        frame = new DocentePantalla(nombre);
                        break;

                    case "estudiante":
                        frame = new EstudiantePantalla(nombre);
                        break;

                    case "acudiente":
                        frame = new AcudientePantalla(nombre, usuarioId);
                        break;

                    default:
                        JOptionPane.showMessageDialog(null,
                                "Rol no reconocido: " + rol,
                                "Error de Redirección",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                }

                if (frame != null) frame.setVisible(true);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error al inicializar la pantalla de " + rol + ": " + e.getMessage(),
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
