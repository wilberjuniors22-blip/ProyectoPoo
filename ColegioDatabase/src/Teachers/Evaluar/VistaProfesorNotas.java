package Teachers.Evaluar;

import Admin.UserManagement.FirebaseClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VistaProfesorNotas extends JFrame {
    private final FirebaseClient fb;
    private final String docenteNombre;

    private JComboBox<String> comboClases;
    private JComboBox<String> comboActividades;
    private JTextField txtNuevaActividad;
    private JTable tablaNotas;
    private DefaultTableModel modeloTabla;

    private Map<String, Object> clasesMap;
    private Map<String, String> claseDisplayMap;
    private Map<String, String> asignaturaNombresMap;

    private String claseSeleccionadaId;

    public VistaProfesorNotas(String docenteNombre, FirebaseClient fb) {
        this.fb = fb;
        this.docenteNombre = docenteNombre;

        setTitle("Gestionar Calificaciones");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 240, 240));

        JLabel lbl1 = new JLabel("Clase:");
        lbl1.setBounds(30, 20, 50, 30);
        add(lbl1);

        comboClases = new JComboBox<>();
        comboClases.setBounds(80, 20, 250, 30);
        add(comboClases);

        JLabel lbl2 = new JLabel("Actividad Existente:");
        lbl2.setBounds(350, 20, 120, 30);
        add(lbl2);

        comboActividades = new JComboBox<>();
        comboActividades.setBounds(470, 20, 180, 30);
        add(comboActividades);

        JButton btnCargarActividad = new JButton("Cargar");
        btnCargarActividad.setBounds(660, 20, 80, 30);
        add(btnCargarActividad);

        JLabel lbl3 = new JLabel("O Nueva Actividad:");
        lbl3.setBounds(350, 60, 120, 30);
        add(lbl3);

        txtNuevaActividad = new JTextField();
        txtNuevaActividad.setBounds(470, 60, 180, 30);
        add(txtNuevaActividad);

        JButton btnCrearCargar = new JButton("Crear/Ver");
        btnCrearCargar.setBounds(660, 60, 100, 30);
        add(btnCrearCargar);

        modeloTabla = new DefaultTableModel(new String[]{"Nombre del Estudiante", "Nota (20 - 100)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        tablaNotas = new JTable(modeloTabla);
        JScrollPane sp = new JScrollPane(tablaNotas);
        sp.setBounds(30, 110, 880, 380);
        add(sp);

        JButton btnGuardar = new JButton("Guardar Notas");
        btnGuardar.setBounds(380, 510, 200, 40);
        add(btnGuardar);

        cargarNombresAsignaturas();
        cargarClasesDocente();

        comboClases.addActionListener(e -> actualizarComboActividades());

        btnCargarActividad.addActionListener(e -> {
            if(comboActividades.getSelectedItem() != null) {
                cargarTablaEstudiantes(comboActividades.getSelectedItem().toString());
            }
        });

        btnCrearCargar.addActionListener(e -> {
            String nueva = txtNuevaActividad.getText().trim();
            if (!nueva.isEmpty()) {
                cargarTablaEstudiantes(nueva);
            } else {
                JOptionPane.showMessageDialog(this, "Escribe un nombre para la nueva actividad");
            }
        });

        btnGuardar.addActionListener(e -> {
            try { guardarNotas(); } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    // NUEVO MÉTODO
    private void cargarNombresAsignaturas() {
        asignaturaNombresMap = new HashMap<>();
        try {
            Map<String, Object> asignaturas = fb.listAll("Asignaturas");
            if (asignaturas == null) return;

            for (Object obj : asignaturas.values()) {
                Map<String, Object> a = (Map<String, Object>) obj;
                String codigo = a.get("codigo").toString();
                String nombre = a.get("nombre").toString();
                asignaturaNombresMap.put(codigo, nombre);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar nombres de asignaturas: " + e.getMessage());
        }
    }

    private void cargarClasesDocente() {
        try {
            clasesMap = fb.listAll("Clases");
            claseDisplayMap = new HashMap<>();
            comboClases.removeAllItems();

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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void actualizarComboActividades() {
        comboActividades.removeAllItems();
        if (comboClases.getSelectedItem() == null) return;

        String displayClase = comboClases.getSelectedItem().toString();
        claseSeleccionadaId = claseDisplayMap.get(displayClase);

        try {
            Map<String, Object> claseData = fb.get("Clases/" + claseSeleccionadaId);
            if (claseData != null && claseData.containsKey("calificaciones")) {
                Map<String, Object> califNode = (Map<String, Object>) claseData.get("calificaciones");
                for (String actividad : califNode.keySet()) {
                    comboActividades.addItem(actividad);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarTablaEstudiantes(String nombreActividad) {
        modeloTabla.setRowCount(0);
        if (comboClases.getSelectedItem() == null) return;

        String displayClase = comboClases.getSelectedItem().toString();
        claseSeleccionadaId = claseDisplayMap.get(displayClase);

        try {
            Map<String, Object> claseData = (Map<String, Object>) clasesMap.get(claseSeleccionadaId);
            List<String> cursos = (List<String>) claseData.get("cursos");
            if (cursos == null || cursos.isEmpty()) return;
            String cursoObjetivo = cursos.get(0);

            Map<String, Object> notasExistentes = new HashMap<>();

            Map<String, Object> datosFrescos = fb.get("Clases/" + claseSeleccionadaId);
            if (datosFrescos != null && datosFrescos.containsKey("calificaciones")) {
                Map<String, Object> califMap = (Map<String, Object>) datosFrescos.get("calificaciones");
                if (califMap != null && califMap.containsKey(nombreActividad)) {
                    notasExistentes = (Map<String, Object>) califMap.get(nombreActividad);
                }
            }

            Map<String, Object> estudiantes = fb.listAll("Usuarios/Estudiantes");

            for (String idEst : estudiantes.keySet()) {
                Map<String, Object> u = (Map<String, Object>) estudiantes.get(idEst);
                if (u == null) continue;

                Object cursoEst = u.get("curso");
                if (cursoEst != null && cursoEst.toString().equals(cursoObjetivo)) {

                    String nota = "";
                    if (notasExistentes != null && notasExistentes.containsKey(idEst)) {
                        nota = notasExistentes.get(idEst).toString();
                    }

                    modeloTabla.addRow(new Object[]{u.get("nombre"), nota});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarNotas() throws Exception {
        if (comboClases.getSelectedItem() == null) return;

        String actividad = "";
        if (!txtNuevaActividad.getText().trim().isEmpty()) {
            actividad = txtNuevaActividad.getText().trim();
        } else if (comboActividades.getSelectedItem() != null) {
            actividad = comboActividades.getSelectedItem().toString();
        }

        if (actividad.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona o escribe una actividad");
            return;
        }

        actividad = actividad.replace(".", "_").replace("#", "");

        String displayClase = comboClases.getSelectedItem().toString();
        claseSeleccionadaId = claseDisplayMap.get(displayClase);

        Map<String, Object> notasMap = new HashMap<>();

        Map<String, Object> estudiantes = fb.listAll("Usuarios/Estudiantes");
        Map<String, String> nombresAIds = new HashMap<>();
        for(String id : estudiantes.keySet()) {
            Map<String, Object> estData = (Map<String, Object>) estudiantes.get(id);
            if(estData != null) {
                nombresAIds.put(estData.get("nombre").toString(), id);
            }
        }

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String nombreEst = modeloTabla.getValueAt(i, 0).toString();
            String idEst = nombresAIds.get(nombreEst);

            if (idEst != null) {
                Object val = modeloTabla.getValueAt(i, 1);
                String nota = (val == null) ? "" : val.toString();

                notasMap.put(idEst, nota);
            }
        }

        String ruta = "Clases/" + claseSeleccionadaId + "/calificaciones/" + actividad;
        fb.updateGenericNode(ruta, notasMap);

        JOptionPane.showMessageDialog(this, "Notas guardadas para: " + actividad);
        actualizarComboActividades();
        txtNuevaActividad.setText("");
    }
}