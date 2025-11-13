package Student.Screen; // El mismo paquete que EstudiantePantalla

import Admin.UserManagement.FirebaseClient; // Importamos tu FirebaseClient
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * Esta es una NUEVA CLASE.
 * Es una ventana (JDialog) que se abre para mostrar las notas y asistencias.
 * Utiliza la conexión FirebaseClient que ya tienes.
 */
public class VistaNotasAsistencias extends JDialog {

    private JTextArea areaNotas;
    private JTextArea areaAsistencias;
    private FirebaseClient fb;
    private String estudianteId;

    /**
     * Constructor de la ventana.
     * @param owner El JFrame principal (tu EstudiantePantalla)
     * @param fb La instancia de FirebaseClient ya conectada
     * @param estudianteId El ID del estudiante (que en tu caso es el 'nombre')
     */
    public VistaNotasAsistencias(Frame owner, FirebaseClient fb, String estudianteId) {
        // 'super' crea el JDialog.
        // 'true' significa que es modal (bloquea la ventana de atrás)
        super(owner, "Mis Notas y Asistencias", true);

        this.fb = fb;
        this.estudianteId = estudianteId;

        // Configuración de la ventana
        setSize(650, 450);
        setLocationRelativeTo(owner); // Se centra sobre la ventana principal
        setLayout(new BorderLayout());

        // ----- Creación de los Componentes -----
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Área para notas
        areaNotas = new JTextArea("Cargando notas...");
        areaNotas.setEditable(false);
        areaNotas.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollNotas = new JScrollPane(areaNotas);
        scrollNotas.setBorder(BorderFactory.createTitledBorder("Mis Notas"));

        // Área para asistencias
        areaAsistencias = new JTextArea("Cargando asistencias...");
        areaAsistencias.setEditable(false);
        areaAsistencias.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollAsistencias = new JScrollPane(areaAsistencias);
        scrollAsistencias.setBorder(BorderFactory.createTitledBorder("Mis Asistencias"));

        // Panel dividido para ponerlas una al lado de la otra
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollNotas, scrollAsistencias);
        splitPane.setResizeWeight(0.5); // 50% de espacio para cada una
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        add(panelPrincipal);

        // --- Cargar los datos de Firebase ---
        cargarDatos();
    }

    /**
     * Se conecta a Firebase en un hilo separado para no congelar la app.
     */
    private void cargarDatos() {

        new Thread(() -> {
            try {
                // --- ¡IMPORTANTE! ASUNCIÓN #1 ---
                // Asumo que tus estudiantes están guardados en la ruta:
                // "Usuarios/Estudiantes/" + el_id_del_estudiante
                String path = "Usuarios/Estudiantes/" + this.estudianteId;

                // --- ¡IMPORTANTE! ASUNCIÓN #2 ---
                // Asumo que tu clase 'FirebaseClient' tiene un método 'get(path)'
                // que devuelve un Map con los datos de ese estudiante.
                Map<String, Object> data = fb.get(path);

                if (data == null) {
                    SwingUtilities.invokeLater(() -> {
                        areaNotas.setText("Error: No se encontró información para el ID: " + this.estudianteId);
                        areaAsistencias.setText("Error: No se encontró información.");
                    });
                    return;
                }

                // Si todo sale bien, actualizamos la interfaz
                SwingUtilities.invokeLater(() -> {

                    // --- Cargar Notas (ASUNCIÓN #3) ---
                    // Asumo que los datos en Firebase tienen un campo "notas"
                    if (data.containsKey("notas") && data.get("notas") instanceof Map) {

                        @SuppressWarnings("unchecked")
                        Map<String, Object> notas = (Map<String, Object>) data.get("notas");

                        StringBuilder sbNotas = new StringBuilder();
                        for (Map.Entry<String, Object> entry : notas.entrySet()) {
                            // Formato: "Materia: Nota"
                            sbNotas.append(String.format("%-20s: %s\n", entry.getKey(), entry.getValue()));
                        }
                        areaNotas.setText(sbNotas.toString());
                    } else {
                        areaNotas.setText("No hay notas registradas.");
                    }

                    // --- Cargar Asistencias (ASUNCIÓN #4) ---
                    // Asumo que los datos en Firebase tienen un campo "asistencias"
                    if (data.containsKey("asistencias") && data.get("asistencias") instanceof Map) {

                        @SuppressWarnings("unchecked")
                        Map<String, Object> asistencias = (Map<String, Object>) data.get("asistencias");

                        StringBuilder sbAsis = new StringBuilder();
                        for (Map.Entry<String, Object> entry : asistencias.entrySet()) {
                            // Formato: "Fecha: Estado"
                            sbAsis.append(String.format("%-20s: %s\n", entry.getKey(), entry.getValue()));
                        }
                        areaAsistencias.setText(sbAsis.toString());
                    } else {
                        areaAsistencias.setText("No hay asistencias registradas.");
                    }
                });

            } catch (Exception e) {
                // Manejo de errores (por si el método 'get' no existe o falla)
                SwingUtilities.invokeLater(() -> {
                    String errorMsg = "Error al cargar datos: " + e.getMessage() + "\n\n" +
                            "Verifica la ASUNCIÓN #2: ¿Tu clase 'FirebaseClient' tiene un método 'get(String path)'?";
                    JOptionPane.showMessageDialog(this, errorMsg, "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                    areaNotas.setText(errorMsg);
                    areaAsistencias.setText(errorMsg);
                    e.printStackTrace();
                });
            }
        }).start();
    }
}