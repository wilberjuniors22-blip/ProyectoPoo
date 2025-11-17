package Student.Screen;

import Admin.UserManagement.FirebaseClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VistaNotasAsistencias extends JDialog {

    private FirebaseClient fb;
    private String estudianteId;
    private String curso;

    private JList<String> listaClases;
    private DefaultListModel<String> modeloClases;

    private JTextArea areaNotas;
    private JTextArea areaAsistencias;

    private Map<String, String> mapaClaseIdPorNombre = new HashMap<>();

    public VistaNotasAsistencias(Frame owner, FirebaseClient fb, String estudianteId) {
        super(owner, "Mis notas y asistencias", true);
        this.fb = fb;
        this.estudianteId = estudianteId;

        setSize(900, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel(new GridLayout(1, 3, 10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        modeloClases = new DefaultListModel<>();
        listaClases = new JList<>(modeloClases);
        listaClases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaClases.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarNotasYAsistencias();
        });

        JScrollPane scrollClases = new JScrollPane(listaClases);
        scrollClases.setBorder(BorderFactory.createTitledBorder("Clases"));
        panelPrincipal.add(scrollClases);

        areaNotas = new JTextArea();
        areaNotas.setEditable(false);
        JScrollPane scrollNotas = new JScrollPane(areaNotas);
        scrollNotas.setBorder(BorderFactory.createTitledBorder("Notas"));
        panelPrincipal.add(scrollNotas);

        areaAsistencias = new JTextArea();
        areaAsistencias.setEditable(false);
        JScrollPane scrollAsis = new JScrollPane(areaAsistencias);
        scrollAsis.setBorder(BorderFactory.createTitledBorder("Asistencias"));
        panelPrincipal.add(scrollAsis);

        add(panelPrincipal, BorderLayout.CENTER);

        cargarClases();
    }

    private void cargarClases() {
        new Thread(() -> {
            try {
                Map<String, Object> est = fb.get("Usuarios/Estudiantes/" + estudianteId);
                curso = (String) est.get("curso");

                Map<String, Object> asignaturas = fb.get("Asignaturas");

                modeloClases.addElement("GENERAL");

                for (String idAsig : asignaturas.keySet()) {
                    Map<String, Object> dataAsig = (Map<String, Object>) asignaturas.get(idAsig);
                    Map<String, Object> clasesIds = (Map<String, Object>) dataAsig.get("clasesIds");

                    if (clasesIds == null) continue;

                    for (Object idClaseObj : clasesIds.values()) {
                        String idClase = String.valueOf(idClaseObj);

                        Map<String, Object> dataClase = fb.get("Clases/" + idClase);

                        List<Object> cursos = (List<Object>) dataClase.get("cursos");
                        if (cursos == null) continue;

                        if (cursos.contains(curso)) {
                            String nombre = (String) dataAsig.get("nombre");
                            modeloClases.addElement(nombre);
                            mapaClaseIdPorNombre.put(nombre, idClase);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error cargando clases.");
            }
        }).start();
    }

    private void cargarNotasYAsistencias() {
        new Thread(() -> {
            try {
                String seleccion = listaClases.getSelectedValue();
                if (seleccion == null) return;

                if (seleccion.equals("GENERAL")) {
                    cargarResumenGeneral();
                    return;
                }

                String idClase = mapaClaseIdPorNombre.get(seleccion);

                // NOTAS
                Map<String, Object> notas = fb.get("Notas/" + idClase + "/" + estudianteId);

                StringBuilder sbNotas = new StringBuilder();

                for (String idNota : notas.keySet()) {
                    Map<String, Object> n = (Map<String, Object>) notas.get(idNota);

                    sbNotas.append(n.get("nombre"))
                            .append(" (")
                            .append(n.get("porcentaje"))
                            .append("%) : ")
                            .append(n.get("valor"))
                            .append("\n");
                }

                // ASISTENCIAS
                Map<String, Object> asis = fb.get("Asistencias/" + idClase + "/" + estudianteId);

                StringBuilder sbAsis = new StringBuilder();

                for (String fecha : asis.keySet()) {
                    sbAsis.append(fecha)
                            .append(" : ")
                            .append(asis.get(fecha))
                            .append("\n");
                }

                SwingUtilities.invokeLater(() -> {
                    areaNotas.setText(sbNotas.toString());
                    areaAsistencias.setText(sbAsis.toString());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarResumenGeneral() {
        new Thread(() -> {
            try {

                StringBuilder sbNotas = new StringBuilder();
                StringBuilder sbAsis = new StringBuilder();

                for (String nombreClase : mapaClaseIdPorNombre.keySet()) {
                    String idClase = mapaClaseIdPorNombre.get(nombreClase);

                    Map<String, Object> notas = fb.get("Notas/" + idClase + "/" + estudianteId);
                    Map<String, Object> asis = fb.get("Asistencias/" + idClase + "/" + estudianteId);

                    // promedio
                    double acum = 0;
                    for (String idNota : notas.keySet()) {
                        Map<String, Object> n = (Map<String, Object>) notas.get(idNota);

                        double val = Double.parseDouble(n.get("valor").toString());
                        double porc = Double.parseDouble(n.get("porcentaje").toString());

                        acum += val * (porc / 100.0);
                    }
                    sbNotas.append(nombreClase).append(" → ").append(acum).append("\n");

                    // fallas
                    int fallas = 0;
                    for (String fecha : asis.keySet()) {
                        if ("F".equals(asis.get(fecha))) fallas++;
                    }
                    sbAsis.append(nombreClase).append(" → Fallas: ").append(fallas).append("\n");
                }

                SwingUtilities.invokeLater(() -> {
                    areaNotas.setText(sbNotas.toString());
                    areaAsistencias.setText(sbAsis.toString());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
