package Student.Screen;

import Admin.UserManagement.FirebaseClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VistaNotasAsistencias extends JDialog {

    private final FirebaseClient fb;
    private final String nombreEstudianteParam;

    private String estudianteIdReal;
    private String cursoEstudiante;

    private JList<String> listaClases;
    private DefaultListModel<String> modeloClases;

    private JTextArea areaNotas;
    private JTextArea areaAsistencias;

    private Map<String, String> mapaClaseIdPorNombre = new HashMap<>();
    private Map<String, String> mapaNombresAsignaturas = new HashMap<>();

    public VistaNotasAsistencias(Frame owner, FirebaseClient fb, String nombreEstudiante) {
        super(owner, "Mis Notas y Asistencias", true);
        this.fb = fb;
        this.nombreEstudianteParam = nombreEstudiante;

        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel(new GridLayout(1, 3, 10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        modeloClases = new DefaultListModel<>();
        listaClases = new JList<>(modeloClases);
        listaClases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaClases.setFont(new Font("SansSerif", Font.PLAIN, 14));
        listaClases.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarNotasYAsistencias();
        });

        JScrollPane scrollClases = new JScrollPane(listaClases);
        scrollClases.setBorder(BorderFactory.createTitledBorder("Mis Clases"));
        panelPrincipal.add(scrollClases);

        areaNotas = new JTextArea();
        areaNotas.setEditable(false);
        areaNotas.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollNotas = new JScrollPane(areaNotas);
        scrollNotas.setBorder(BorderFactory.createTitledBorder("Calificaciones"));
        panelPrincipal.add(scrollNotas);

        areaAsistencias = new JTextArea();
        areaAsistencias.setEditable(false);
        areaAsistencias.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollAsis = new JScrollPane(areaAsistencias);
        scrollAsis.setBorder(BorderFactory.createTitledBorder("Historial de Asistencia"));
        panelPrincipal.add(scrollAsis);

        add(panelPrincipal, BorderLayout.CENTER);

        iniciarCargaDatos();
    }

    private void iniciarCargaDatos() {
        new Thread(() -> {
            try {
                if (!buscarInfoEstudiante()) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "No se encontró al estudiante: " + nombreEstudianteParam));
                    return;
                }

                cargarNombresAsignaturas();

                cargarClasesDelEstudiante();

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error cargando datos: " + e.getMessage()));
            }
        }).start();
    }

    private boolean buscarInfoEstudiante() throws Exception {
        Map<String, Object> estudiantes = fb.listAll("Usuarios/Estudiantes");
        if (estudiantes == null) return false;

        for (String id : estudiantes.keySet()) {
            Map<String, Object> data = (Map<String, Object>) estudiantes.get(id);
            if (data == null) continue;

            String nombre = (String) data.get("nombre");
            if (nombre != null && nombre.equalsIgnoreCase(nombreEstudianteParam)) {
                this.estudianteIdReal = id;
                this.cursoEstudiante = (String) data.get("curso");
                return true;
            }
        }
        return false;
    }

    private void cargarNombresAsignaturas() throws Exception {
        Map<String, Object> asignaturas = fb.listAll("Asignaturas");
        if (asignaturas != null) {
            for (Object obj : asignaturas.values()) {
                if (obj instanceof Map) {
                    Map<String, Object> a = (Map<String, Object>) obj;
                    String codigo = String.valueOf(a.get("codigo"));
                    String nombre = String.valueOf(a.get("nombre"));
                    mapaNombresAsignaturas.put(codigo, nombre);
                }
            }
        }
    }

    private void cargarClasesDelEstudiante() throws Exception {
        Map<String, Object> todasLasClases = fb.listAll("Clases");

        SwingUtilities.invokeLater(() -> {
            modeloClases.clear();
            modeloClases.addElement("--- RESUMEN GENERAL ---");
        });

        if (todasLasClases == null) return;

        for (String idClase : todasLasClases.keySet()) {
            Map<String, Object> dataClase = (Map<String, Object>) todasLasClases.get(idClase);
            if (dataClase == null) continue;

            java.util.List<?> cursosClase = (java.util.List<?>) dataClase.get("cursos");
            if (cursosClase != null && cursosClase.contains(this.cursoEstudiante)) {

                String codigoAsignatura = (String) dataClase.get("asignaturaCodigo");
                String nombreReal = mapaNombresAsignaturas.getOrDefault(codigoAsignatura, "Materia");

                String nombreMostrar = nombreReal + " (" + codigoAsignatura + ")";
                mapaClaseIdPorNombre.put(nombreMostrar, idClase);

                SwingUtilities.invokeLater(() -> modeloClases.addElement(nombreMostrar));
            }
        }
    }

    private void cargarNotasYAsistencias() {
        String seleccion = listaClases.getSelectedValue();
        if (seleccion == null) return;

        new Thread(() -> {
            if (seleccion.equals("--- RESUMEN GENERAL ---")) {
                cargarResumenGeneral();
            } else {
                String idClase = mapaClaseIdPorNombre.get(seleccion);
                cargarDetalleClase(idClase);
            }
        }).start();
    }

    private void cargarDetalleClase(String idClase) {
        try {
            Map<String, Object> dataClase = fb.get("Clases/" + idClase);

            // --- PROCESAR CALIFICACIONES ---
            StringBuilder sbNotas = new StringBuilder();
            sbNotas.append("ACTIVIDAD          | NOTA\n");
            sbNotas.append("---------------------------\n");

            Map<String, Object> calificacionesNode = (Map<String, Object>) dataClase.get("calificaciones");
            double sumaNotas = 0;
            int contNotas = 0;

            if (calificacionesNode != null) {
                for (String nombreActividad : calificacionesNode.keySet()) {
                    Map<String, Object> notasEstudiantes = (Map<String, Object>) calificacionesNode.get(nombreActividad);

                    if (notasEstudiantes != null && notasEstudiantes.containsKey(estudianteIdReal)) {
                        String notaStr = notasEstudiantes.get(estudianteIdReal).toString();
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

            // --- PROCESAR ASISTENCIAS ---
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

                    if (asisEstudiantes != null && asisEstudiantes.containsKey(estudianteIdReal)) {
                        String estado = asisEstudiantes.get(estudianteIdReal).toString();

                        String estadoTexto = estado.equals("A") ? "Asistió" : "FALLA";
                        String fechaLimpia = fecha.replace("_", ".").replace("-", "/");

                        sbAsis.append(String.format("%-18s : %s\n", fechaLimpia, estadoTexto));

                        if (estado.equals("F")) totalFallas++;
                        else totalAsistencias++;
                    }
                }
            }

            sbAsis.append("\n---------------------------\n");
            sbAsis.append("Total Asistencias: ").append(totalAsistencias).append("\n");
            sbAsis.append("Total Fallas:      ").append(totalFallas);

            // Actualizar UI
            SwingUtilities.invokeLater(() -> {
                areaNotas.setText(sbNotas.toString());
                areaAsistencias.setText(sbAsis.toString());
                areaNotas.setCaretPosition(0);
                areaAsistencias.setCaretPosition(0);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarResumenGeneral() {
        StringBuilder sbResumen = new StringBuilder();
        sbResumen.append("RESUMEN DE FALLAS POR MATERIA:\n\n");

        try {
            for (String nombreClase : mapaClaseIdPorNombre.keySet()) {
                String idClase = mapaClaseIdPorNombre.get(nombreClase);
                Map<String, Object> dataClase = fb.get("Clases/" + idClase);

                if (dataClase == null) continue;

                Map<String, Object> asistenciasNode = (Map<String, Object>) dataClase.get("asistencias");
                int fallas = 0;

                if (asistenciasNode != null) {
                    for (Object obj : asistenciasNode.values()) {
                        Map<String, Object> sesion = (Map<String, Object>) obj;
                        if (sesion.containsKey(estudianteIdReal)) {
                            String est = sesion.get(estudianteIdReal).toString();
                            if ("F".equals(est)) fallas++;
                        }
                    }
                }

                sbResumen.append(nombreClase).append(" -> ").append(fallas).append(" Fallas\n");
            }

            SwingUtilities.invokeLater(() -> {
                areaNotas.setText("Selecciona una materia específica\npara ver el detalle de calificaciones.");
                areaAsistencias.setText(sbResumen.toString());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}