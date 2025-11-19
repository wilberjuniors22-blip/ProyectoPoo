package Teachers.Evaluar;

import Admin.UserManagement.FirebaseClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VistaProfesorAsistencias extends JFrame {

    private final FirebaseClient fb;
    private final String docenteNombre;

    private JComboBox<String> comboClases;
    private JTextField txtFecha;
    private JTable tabla;
    private DefaultTableModel modelo;

    private Map<String, Object> clasesMap;
    private Map<String, String> claseDisplayMap;
    private Map<String, String> asignaturaNombresMap;
    private List<String> idEstudiantesOcultos; // Lista para almacenar los IDs de los estudiantes

    private String claseSeleccionadaId;

    public VistaProfesorAsistencias(String docenteNombre, FirebaseClient fb) {
        this.fb = fb;
        this.docenteNombre = docenteNombre;

        setTitle("Tomar Asistencia (Checkbox)");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 240, 240));

        JLabel lbl1 = new JLabel("Clase:");
        lbl1.setBounds(20, 20, 60, 30);
        add(lbl1);

        comboClases = new JComboBox<>();
        comboClases.setBounds(80, 20, 300, 30);
        add(comboClases);

        JLabel lbl2 = new JLabel("Fecha / Sesión:");
        lbl2.setBounds(400, 20, 120, 30);
        add(lbl2);

        txtFecha = new JTextField("Sesion1");
        txtFecha.setBounds(520, 20, 150, 30);
        add(txtFecha);

        JButton btnCargar = new JButton("Cargar Lista");
        btnCargar.setBounds(690, 20, 150, 30);
        add(btnCargar);

        // Cambiamos el tipo de la columna 1 a Boolean para el Checkbox
        modelo = new DefaultTableModel(new Object[]{"Nombre del Estudiante", "Asistencia"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return Boolean.class; // Checkbox
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Solo editable el Checkbox
            }
        };

        tabla = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBounds(20, 70, 840, 420);
        add(sp);

        JButton btnGuardar = new JButton("Guardar Asistencia");
        btnGuardar.setBounds(350, 510, 200, 40);
        add(btnGuardar);

        idEstudiantesOcultos = new ArrayList<>();

        cargarNombresAsignaturas();
        cargarClases();

        btnCargar.addActionListener(e -> {
            try {
                cargarEstudiantesYAsistenciaPrevia();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage());
            }
        });

        btnGuardar.addActionListener(e -> {
            try {
                guardarAsistencia();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        });
    }

    private void cargarNombresAsignaturas() {
        asignaturaNombresMap = new HashMap<>();
        try {
            Map<String, Object> asignaturas = fb.listAll("Asignaturas");
            if (asignaturas == null) return;

            for (Object obj : asignaturas.values()) {
                if (obj instanceof Map) {
                    Map<String, Object> a = (Map<String, Object>) obj;
                    String codigo = a.get("codigo").toString();
                    String nombre = a.get("nombre").toString();
                    asignaturaNombresMap.put(codigo, nombre);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarClases() {
        try {
            clasesMap = fb.listAll("Clases");
            claseDisplayMap = new HashMap<>();
            comboClases.removeAllItems();

            if (clasesMap == null) return;

            for (String id : clasesMap.keySet()) {
                Map<String, Object> c = (Map<String, Object>) clasesMap.get(id);
                if (c == null) continue;

                Object doc = c.get("docenteNombre");
                if (doc != null && doc.toString().equalsIgnoreCase(docenteNombre)) {

                    String codigoAsignatura = c.getOrDefault("asignaturaCodigo", "SIN_CODIGO").toString();
                    String nombreAsignatura = asignaturaNombresMap.getOrDefault(codigoAsignatura, "Asignatura Desconocida");

                    String display = nombreAsignatura + " (" + codigoAsignatura + ")";

                    claseDisplayMap.put(display, id);
                    comboClases.addItem(display);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarEstudiantesYAsistenciaPrevia() throws Exception {
        if (comboClases.getSelectedItem() == null) return;

        String displayClase = comboClases.getSelectedItem().toString();
        claseSeleccionadaId = claseDisplayMap.get(displayClase);
        String fecha = txtFecha.getText().trim();

        if (fecha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe una fecha o sesión (ej: 20-Oct o Sesion1)");
            return;
        }

        Map<String, Object> claseData = (Map<String, Object>) clasesMap.get(claseSeleccionadaId);
        List<String> cursosClase = (List<String>) claseData.get("cursos");
        if (cursosClase == null || cursosClase.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta clase no tiene cursos asignados.");
            return;
        }
        String cursoObjetivo = cursosClase.get(0);

        Map<String, Object> datosClaseFull = fb.get("Clases/" + claseSeleccionadaId);
        Map<String, Object> asistenciasNode = (Map<String, Object>) datosClaseFull.get("asistencias");
        Map<String, Object> asistenciaFecha = null;

        if (asistenciasNode != null) {
            String claveFecha = fecha.replace(".", "_").replace("/", "-");
            asistenciaFecha = (Map<String, Object>) asistenciasNode.get(claveFecha);
        }

        modelo.setRowCount(0);
        idEstudiantesOcultos.clear();

        Map<String, Object> estudiantes = fb.listAll("Usuarios/Estudiantes");
        if (estudiantes == null) return;

        for (String idEst : estudiantes.keySet()) {
            Map<String, Object> estData = (Map<String, Object>) estudiantes.get(idEst);
            if (estData == null) continue;

            Object cursoEst = estData.get("curso");
            if (cursoEst != null && cursoEst.toString().equals(cursoObjetivo)) {

                boolean vino = false;
                if (asistenciaFecha != null && asistenciaFecha.containsKey(idEst)) {
                    vino = "A".equalsIgnoreCase(asistenciaFecha.get(idEst).toString());
                }

                modelo.addRow(new Object[]{
                        estData.get("nombre"),
                        vino
                });

                idEstudiantesOcultos.add(idEst);
            }
        }
    }

    private void guardarAsistencia() throws Exception {
        if (comboClases.getSelectedItem() == null) return;
        String fecha = txtFecha.getText().trim();

        if (fecha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La fecha no puede estar vacía.");
            return;
        }

        String claveFecha = fecha.replace(".", "_").replace("/", "-").replace("#", "");

        String displayClase = comboClases.getSelectedItem().toString();
        claseSeleccionadaId = claseDisplayMap.get(displayClase);

        Map<String, Object> mapaAsistenciaSesion = new HashMap<>();

        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (i >= idEstudiantesOcultos.size()) continue; // Seguridad

            String idEst = idEstudiantesOcultos.get(i);
            Boolean vino = (Boolean) modelo.getValueAt(i, 1);

            String valor = vino ? "A" : "F";

            mapaAsistenciaSesion.put(idEst, valor);
        }

        String ruta = "Clases/" + claseSeleccionadaId + "/asistencias/" + claveFecha;
        fb.updateGenericNode(ruta, mapaAsistenciaSesion);

        JOptionPane.showMessageDialog(this, "Asistencia guardada correctamente bajo: " + claveFecha);
    }
}