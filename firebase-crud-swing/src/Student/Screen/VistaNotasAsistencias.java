package Student.Screen;

import Admin.UserManagement.FirebaseClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class VistaNotasAsistencias extends JDialog {

    private JTextArea areaNotas;
    private JTextArea areaAsistencias;
    private FirebaseClient fb;
    private String estudianteId;
    
    public VistaNotasAsistencias(Frame owner, FirebaseClient fb, String estudianteId) {
    
        super(owner, "Mis Notas y Asistencias", true);

        this.fb = fb;
        this.estudianteId = estudianteId;

        setSize(650, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        areaNotas = new JTextArea("Cargando notas...");
        areaNotas.setEditable(false);
        areaNotas.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollNotas = new JScrollPane(areaNotas);
        scrollNotas.setBorder(BorderFactory.createTitledBorder("Mis Notas"));

        areaAsistencias = new JTextArea("Cargando asistencias...");
        areaAsistencias.setEditable(false);
        areaAsistencias.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollAsistencias = new JScrollPane(areaAsistencias);
        scrollAsistencias.setBorder(BorderFactory.createTitledBorder("Mis Asistencias"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollNotas, scrollAsistencias);
        splitPane.setResizeWeight(0.5); // 50% de espacio para cada una
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        add(panelPrincipal);

        cargarDatos();
    }

    private void cargarDatos() {

        new Thread(() -> {
            try {
                String path = "Usuarios/Estudiantes/" + this.estudianteId;

        
                Map<String, Object> data = fb.get(path);

                if (data == null) {
                    SwingUtilities.invokeLater(() -> {
                        areaNotas.setText("Error: No se encontró información para el ID: " + this.estudianteId);
                        areaAsistencias.setText("Error: No se encontró información.");
                    });
                    return;
                }
                
                SwingUtilities.invokeLater(() -> {

                    
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
