package Admin.StudentManagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class AsignarCursosyPadresFrame extends JFrame {
    private final Gson gson = new Gson();

    // Componentes UI
    private JComboBox<String> comboEstudiantes;
    private JComboBox<String> comboCurso;
    private JLabel lblCursoActual;

    private JComboBox<String> comboAcudiente;
    private JList<String> listHijos;
    private DefaultListModel<String> modelHijos;

    // Mapas para lógica de negocio
    private Map<String, String> mapNombreAIdEstudiante = new HashMap<>();
    private Map<String, String> mapIdEstudianteACurso = new HashMap<>();

    private Map<String, String> mapNombreAIdAcudiente = new HashMap<>();
    private Map<String, List<String>> mapIdAcudienteAHijos = new HashMap<>();

    public AsignarCursosyPadresFrame() {
        setTitle("Gestión de Asignaciones Académicas");
        setSize(800, 550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        JPanel panelCurso = crearPanelEstilizado("Asignar Curso a Estudiante");
        panelCurso.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        comboEstudiantes = new JComboBox<>();
        comboCurso = new JComboBox<>();
        lblCursoActual = new JLabel("Curso Actual: Cargando...");
        lblCursoActual.setForeground(new Color(100, 100, 100));
        lblCursoActual.setFont(new Font("SansSerif", Font.ITALIC, 12));

        JButton btnGuardarCurso = new JButton("Guardar Asignación de Curso");
        estilizarBoton(btnGuardarCurso);


        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelCurso.add(new JLabel("Seleccionar Estudiante:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        panelCurso.add(comboEstudiantes, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        panelCurso.add(lblCursoActual, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelCurso.add(new JLabel("Asignar Nuevo Curso:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panelCurso.add(comboCurso, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panelCurso.add(btnGuardarCurso, gbc);

        JPanel panelPadres = crearPanelEstilizado("Asignar Hijos a Acudiente");
        panelPadres.setLayout(new BorderLayout(15, 15));

        JPanel topPadres = new JPanel(new BorderLayout(5, 5));
        topPadres.setOpaque(false);
        topPadres.add(new JLabel("Seleccionar Acudiente:"), BorderLayout.NORTH);
        comboAcudiente = new JComboBox<>();
        topPadres.add(comboAcudiente, BorderLayout.CENTER);

        JPanel centerPadres = new JPanel(new BorderLayout(5, 5));
        centerPadres.setOpaque(false);
        centerPadres.add(new JLabel("Seleccionar Hijos (Usa Ctrl para múltiple selección):"), BorderLayout.NORTH);

        modelHijos = new DefaultListModel<>();
        listHijos = new JList<>(modelHijos);
        listHijos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listHijos.setVisibleRowCount(6);
        centerPadres.add(new JScrollPane(listHijos), BorderLayout.CENTER);

        JButton btnGuardarPadre = new JButton("Guardar Relación Familiar");
        estilizarBoton(btnGuardarPadre);

        panelPadres.add(topPadres, BorderLayout.NORTH);
        panelPadres.add(centerPadres, BorderLayout.CENTER);
        panelPadres.add(btnGuardarPadre, BorderLayout.SOUTH);

        mainPanel.add(panelCurso);
        mainPanel.add(Box.createVerticalStrut(20)); // Espacio
        mainPanel.add(panelPadres);

        add(mainPanel, BorderLayout.CENTER);

        comboEstudiantes.addActionListener(e -> actualizarInfoEstudianteSeleccionado());

        comboAcudiente.addActionListener(e -> actualizarSeleccionHijos());

        btnGuardarCurso.addActionListener(e -> guardarCurso());
        btnGuardarPadre.addActionListener(e -> guardarPadres());

        cargarDatos();
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelEstilizado(String titulo) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), titulo,
                        0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(50, 50, 100)
                )
        ));
        return p;
    }

    private void estilizarBoton(JButton btn) {
        btn.setBackground(new Color(70, 130, 180)); // Azul acero
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {

                String estudiantesJson = FirebaseConnection.get("Usuarios/Estudiantes");
                String acudientesJson = FirebaseConnection.get("Usuarios/Acudientes");
                String cursosJson = FirebaseConnection.get("Cursos");

                java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

                Map<String, Object> estudiantesData = estudiantesJson == null || estudiantesJson.equals("null") ? new HashMap<>() : gson.fromJson(estudiantesJson, mapType);
                Map<String, Object> acudientesData = acudientesJson == null || acudientesJson.equals("null") ? new HashMap<>() : gson.fromJson(acudientesJson, mapType);
                Map<String, Object> cursosData = cursosJson == null || cursosJson.equals("null") ? new HashMap<>() : gson.fromJson(cursosJson, mapType);

                SwingUtilities.invokeLater(() -> {
                    comboEstudiantes.removeAllItems();
                    comboAcudiente.removeAllItems();
                    comboCurso.removeAllItems();
                    modelHijos.clear();

                    mapNombreAIdEstudiante.clear();
                    mapIdEstudianteACurso.clear();
                    mapNombreAIdAcudiente.clear();
                    mapIdAcudienteAHijos.clear();

                    // 1. Procesar Estudiantes
                    List<String> nombresEstudiantesOrdenados = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : estudiantesData.entrySet()) {
                        String id = entry.getKey();
                        Map<String, Object> val = (Map<String, Object>) entry.getValue();
                        String nombre = val.getOrDefault("nombre", id).toString();
                        String curso = val.getOrDefault("curso", "Sin curso").toString();

                        mapNombreAIdEstudiante.put(nombre, id);
                        mapIdEstudianteACurso.put(id, curso);
                        nombresEstudiantesOrdenados.add(nombre);
                    }
                    Collections.sort(nombresEstudiantesOrdenados);
                    nombresEstudiantesOrdenados.forEach(n -> {
                        comboEstudiantes.addItem(n);
                        modelHijos.addElement(n);
                    });

                    for (Map.Entry<String, Object> entry : acudientesData.entrySet()) {
                        String id = entry.getKey();
                        Map<String, Object> val = (Map<String, Object>) entry.getValue();
                        String nombre = val.getOrDefault("nombre", id).toString();

                        List<String> hijosList = new ArrayList<>();
                        Object hijosObj = val.get("hijos");
                        if (hijosObj instanceof List) {
                            hijosList = (List<String>) hijosObj;
                        }

                        mapNombreAIdAcudiente.put(nombre, id);
                        mapIdAcudienteAHijos.put(id, hijosList);
                        comboAcudiente.addItem(nombre);
                    }


                    List<String> listaCursos = new ArrayList<>();
                    for (Map.Entry<String, Object> e : cursosData.entrySet()) {
                        Object val = e.getValue();
                        if (val instanceof Map) {
                            Object nombreCurso = ((Map) val).get("nombre");
                            if(nombreCurso != null) listaCursos.add(nombreCurso.toString());
                        } else {
                            listaCursos.add(val.toString());
                        }
                    }
                    Collections.sort(listaCursos);
                    listaCursos.forEach(c -> comboCurso.addItem(c));

                    actualizarInfoEstudianteSeleccionado();
                    actualizarSeleccionHijos();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error cargando datos: " + ex.getMessage()));
            }
        }).start();
    }

    private void actualizarInfoEstudianteSeleccionado() {
        String nombreEst = (String) comboEstudiantes.getSelectedItem();
        if (nombreEst != null) {
            String id = mapNombreAIdEstudiante.get(nombreEst);
            String curso = mapIdEstudianteACurso.getOrDefault(id, "Sin curso asignado");
            lblCursoActual.setText("Curso Actual: " + curso);

            comboCurso.setSelectedItem(curso);
        }
    }

    private void actualizarSeleccionHijos() {
        String nombreAcudiente = (String) comboAcudiente.getSelectedItem();
        listHijos.clearSelection(); // Limpiar selección previa

        if (nombreAcudiente != null) {
            String idAcudiente = mapNombreAIdAcudiente.get(nombreAcudiente);
            List<String> idsHijosActuales = mapIdAcudienteAHijos.get(idAcudiente);

            if (idsHijosActuales != null && !idsHijosActuales.isEmpty()) {
                List<Integer> indicesASeleccionar = new ArrayList<>();

                for (int i = 0; i < modelHijos.size(); i++) {
                    String nombreEnLista = modelHijos.get(i);
                    String idEnLista = mapNombreAIdEstudiante.get(nombreEnLista);

                    if (idsHijosActuales.contains(idEnLista)) {
                        indicesASeleccionar.add(i);
                    }
                }

                int[] indices = indicesASeleccionar.stream().mapToInt(i -> i).toArray();
                listHijos.setSelectedIndices(indices);
            }
        }
    }


    private void guardarCurso() {
        try {
            String nombreEstudiante = (String) comboEstudiantes.getSelectedItem();
            String idEstudiante = mapNombreAIdEstudiante.get(nombreEstudiante);
            String nuevoCurso = (String) comboCurso.getSelectedItem();

            if (idEstudiante == null || nuevoCurso == null) return;

            FirebaseConnection.patch("Usuarios/Estudiantes/" + idEstudiante, Map.of("curso", nuevoCurso));

            mapIdEstudianteACurso.put(idEstudiante, nuevoCurso);
            lblCursoActual.setText("Curso Actual: " + nuevoCurso);

            JOptionPane.showMessageDialog(this, "Curso actualizado para " + nombreEstudiante);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void guardarPadres() {
        try {
            String nombreAcudiente = (String) comboAcudiente.getSelectedItem();
            String idAcudiente = mapNombreAIdAcudiente.get(nombreAcudiente);

            if (idAcudiente == null) return;

            List<String> nombresSeleccionados = listHijos.getSelectedValuesList();
            List<String> idsParaGuardar = new ArrayList<>();

            for (String nombre : nombresSeleccionados) {
                String id = mapNombreAIdEstudiante.get(nombre);
                if (id != null) idsParaGuardar.add(id);
            }

            FirebaseConnection.patch("Usuarios/Acudientes/" + idAcudiente, Map.of("hijos", idsParaGuardar));

            mapIdAcudienteAHijos.put(idAcudiente, idsParaGuardar);

            JOptionPane.showMessageDialog(this, "Familia actualizada para " + nombreAcudiente + "\nTotal hijos: " + idsParaGuardar.size());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AsignarCursosyPadresFrame f = new AsignarCursosyPadresFrame();
            f.setVisible(true);
        });
    }
}