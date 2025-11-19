package Parents.NotasAsistencias;

import Admin.UserManagement.FirebaseClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

public class VerAsistencias extends JDialog {

    private final FirebaseClient fb;
    private final String estudianteId;
    private final String claseId;
    private final String nombreClase;

    private JTextArea areaAsistencias;

    public VerAsistencias(Frame owner, FirebaseClient fb, String estudianteId, String claseId, String nombreClase) {
        super(owner, "Asistencias de " + nombreClase, true);
        this.fb = fb;
        this.estudianteId = estudianteId;
        this.claseId = claseId;
        this.nombreClase = nombreClase;

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        areaAsistencias = new JTextArea();
        areaAsistencias.setEditable(false);
        areaAsistencias.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollAsis = new JScrollPane(areaAsistencias);
        scrollAsis.setBorder(BorderFactory.createTitledBorder("Historial de Asistencia en " + nombreClase));

        add(scrollAsis, BorderLayout.CENTER);

        cargarDetalles();
    }

    private void cargarDetalles() {
        new Thread(() -> {
            try {
                Map<String, Object> dataClase = fb.get("Clases/" + claseId);

                StringBuilder sbAsis = new StringBuilder();
                sbAsis.append("FECHA/SESIÓN       | ESTADO\n");
                sbAsis.append("---------------------------\n");

                Map<String, Object> asistenciasNode = (Map<String, Object>) dataClase.get("asistencias");
                int totalFallas = 0;
                int totalAsistencias = 0;

                if (asistenciasNode != null) {
                    TreeMap<String, Object> asistenciasOrdenadas = new TreeMap<>(asistenciasNode);

                    for (String fecha : asistenciasOrdenadas.keySet()) {
                        Map<String, Object> asisEstudiantes = (Map<String, Object>) asistenciasOrdenadas.get(fecha);

                        if (asisEstudiantes != null && asisEstudiantes.containsKey(estudianteId)) {
                            String estado = asisEstudiantes.get(estudianteId).toString();

                            // Usamos "A" para Asistió (Presente)
                            String estadoTexto = estado.equalsIgnoreCase("A") ? "Asistió" : "FALLA";
                            String fechaLimpia = fecha.replace("_", ".").replace("-", "/");

                            sbAsis.append(String.format("%-18s : %s\n", fechaLimpia, estadoTexto));

                            if (estado.equalsIgnoreCase("F")) totalFallas++;
                            else totalAsistencias++;
                        }
                    }
                }

                sbAsis.append("\n---------------------------\n");
                sbAsis.append("Total Asistencias: ").append(totalAsistencias).append("\n");
                sbAsis.append("Total Fallas:      ").append(totalFallas);

                SwingUtilities.invokeLater(() -> {
                    areaAsistencias.setText(sbAsis.toString());
                    areaAsistencias.setCaretPosition(0);
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> areaAsistencias.setText("Error al cargar asistencias: " + e.getMessage()));
            }
        }).start();
    }
}