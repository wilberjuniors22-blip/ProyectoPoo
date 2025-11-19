package Parents.NotasAsistencias;

import Admin.UserManagement.FirebaseClient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

public class VerNotas extends JDialog {

    private final FirebaseClient fb;
    private final String estudianteId;
    private final String claseId;
    private final String nombreClase;

    private JTextArea areaNotas;

    public VerNotas(Frame owner, FirebaseClient fb, String estudianteId, String claseId, String nombreClase) {
        super(owner, "Notas de " + nombreClase, true);
        this.fb = fb;
        this.estudianteId = estudianteId;
        this.claseId = claseId;
        this.nombreClase = nombreClase;

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        areaNotas = new JTextArea();
        areaNotas.setEditable(false);
        areaNotas.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollNotas = new JScrollPane(areaNotas);
        scrollNotas.setBorder(BorderFactory.createTitledBorder("Calificaciones en " + nombreClase));

        add(scrollNotas, BorderLayout.CENTER);

        cargarDetalles();
    }

    private void cargarDetalles() {
        new Thread(() -> {
            try {
                Map<String, Object> dataClase = fb.get("Clases/" + claseId);

                StringBuilder sbNotas = new StringBuilder();
                sbNotas.append("ACTIVIDAD          | NOTA\n");
                sbNotas.append("---------------------------\n");

                Map<String, Object> calificacionesNode = (Map<String, Object>) dataClase.get("calificaciones");
                double sumaNotas = 0;
                int contNotas = 0;

                if (calificacionesNode != null) {
                    for (String nombreActividad : calificacionesNode.keySet()) {
                        Map<String, Object> notasEstudiantes = (Map<String, Object>) calificacionesNode.get(nombreActividad);

                        if (notasEstudiantes != null && notasEstudiantes.containsKey(estudianteId)) {
                            String notaStr = notasEstudiantes.get(estudianteId).toString();
                            String nombreActLimpio = nombreActividad.replace("_", " ");

                            sbNotas.append(String.format("%-18s : %s\n", nombreActLimpio, notaStr));

                            try {
                                sumaNotas += Double.parseDouble(notaStr);
                                contNotas++;
                            } catch (NumberFormatException ignored) {}
                        } else {
                            String nombreActLimpio = nombreActividad.replace("_", " ");
                            sbNotas.append(String.format("%-18s : -\n", nombreActLimpio));
                        }
                    }
                }

                if (contNotas > 0) {
                    sbNotas.append("\n---------------------------\n");
                    sbNotas.append(String.format("PROMEDIO SIMPLE    : %.2f", (sumaNotas / contNotas)));
                } else {
                    sbNotas.append("\nSin calificaciones registradas.");
                }

                SwingUtilities.invokeLater(() -> {
                    areaNotas.setText(sbNotas.toString());
                    areaNotas.setCaretPosition(0);
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> areaNotas.setText("Error al cargar notas: " + e.getMessage()));
            }
        }).start();
    }
}