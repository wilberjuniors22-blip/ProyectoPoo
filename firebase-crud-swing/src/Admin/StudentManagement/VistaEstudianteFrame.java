package Admin.StudentManagement; 

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Muestra las notas y asistencias de un estudiante específico.
 */
public class VistaEstudianteFrame extends JFrame {

    private final String estudianteId;
    private final Gson gson = new Gson();

    private JLabel lblNombre;
    private JLabel lblCurso;
    private JTextArea areaNotas;
    private JTextArea areaAsistencias;

    public VistaEstudianteFrame(String estudianteId) {
        this.estudianteId = estudianteId;

        setTitle("Mi Portal Académico");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        cargarDatosEstudiante();
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(panelPrincipal);

        // Panel de información superior
        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        lblNombre = new JLabel("Cargando nombre...");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 18));
        lblCurso = new JLabel("Curso: Cargando...");
        lblCurso.setFont(new Font("Arial", Font.PLAIN, 14));
        panelInfo.add(lblNombre);
        panelInfo.add(lblCurso);
        panelPrincipal.add(panelInfo, BorderLayout.NORTH);

        // Panel central con división
        areaNotas = new JTextArea("Cargando notas...");
        areaNotas.setEditable(false);
        JScrollPane scrollNotas = new JScrollPane(areaNotas);
        scrollNotas.setBorder(BorderFactory.createTitledBorder("Mis Notas"));

        areaAsistencias = new JTextArea("Cargando asistencias...");
        areaAsistencias.setEditable(false);
        JScrollPane scrollAsistencias = new JScrollPane(areaAsistencias);
        scrollAsistencias.setBorder(BorderFactory.createTitledBorder("Mis Asistencias"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollNotas, scrollAsistencias);
        splitPane.setResizeWeight(0.5); // División 50/50
        panelPrincipal.add(splitPane, BorderLayout.CENTER);
    }

    private void cargarDatosEstudiante() {
        // Hilo para no bloquear la UI
        new Thread(() -> {
            try {
                // Obtenemos solo la data del estudiante logueado
                String json = FirebaseConnection.get("Usuarios/Estudiantes/" + this.estudianteId);
                if (json == null || json.equals("null")) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "No se encontró información para este estudiante."));
                    return;
                }

                Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> data = gson.fromJson(json, mapType);

                // Volver al hilo principal para actualizar Swing
                SwingUtilities.invokeLater(() -> {
                    // Cargar datos básicos
                    String nombre = (String) data.getOrDefault("nombre", "Estudiante Sin Nombre");
                    String curso = (String) data.getOrDefault("curso", "Curso no asignado");
                    lblNombre.setText("Bienvenido(a), " + nombre);
                    lblCurso.setText("Curso: " + curso);

                    // Cargar Notas
                    if (data.containsKey("notas") && data.get("notas") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> notas = (Map<String, Object>) data.get("notas");
                        StringBuilder sbNotas = new StringBuilder();
                        for (Map.Entry<String, Object> entry : notas.entrySet()) {
                            sbNotas.append(entry.getKey()).append(": \t").append(entry.getValue()).append("\n");
                        }
                        areaNotas.setText(sbNotas.toString());
                    } else {
                        areaNotas.setText("No hay notas registradas.");
                    }

                    // Cargar Asistencias
                    if (data.containsKey("asistencias") && data.get("asistencias") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> asistencias = (Map<String, Object>) data.get("asistencias");
                        StringBuilder sbAsis = new StringBuilder();
                        for (Map.Entry<String, Object> entry : asistencias.entrySet()) {
                            sbAsis.append(entry.getKey()).append(": \t").append(entry.getValue()).append("\n");
                        }
                        areaAsistencias.setText(sbAsis.toString());
                    } else {
                        areaAsistencias.setText("No hay asistencias registradas.");
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage())
                );
            }
        }).start();
    }
}
