package Admin.StudentManagement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AsignarCursosyPadresFrame extends JFrame {
    private final Gson gson = new Gson();
    private JComboBox<String> comboEstudiantes;
    private JComboBox<String> comboCurso;
    private JComboBox<String> comboAcudiente;
    private JList<String> listHijos;
    private DefaultListModel<String> modelHijos = new DefaultListModel<>();

    // Map para relacionar nombres visibles con IDs reales
    private Map<String, String> estudiantesMap = new HashMap<>();
    private Map<String, String> acudientesMap = new HashMap<>();

    public AsignarCursosyPadresFrame() {
        setTitle("Asignar Cursos y Padres");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(2, 1));

        // Panel 1: Asignar Curso
        JPanel panelCurso = new JPanel(new GridLayout(3, 2, 10, 10));
        comboEstudiantes = new JComboBox<>();
        comboCurso = new JComboBox<>();

        JButton btnGuardarCurso = new JButton("Asignar Curso");
        btnGuardarCurso.addActionListener(e -> guardarCurso());

        panelCurso.setBorder(BorderFactory.createTitledBorder("Asignar Curso a Estudiante"));
        panelCurso.add(new JLabel("Estudiante:"));
        panelCurso.add(comboEstudiantes);
        panelCurso.add(new JLabel("Curso:"));
        panelCurso.add(comboCurso);
        panelCurso.add(new JLabel());
        panelCurso.add(btnGuardarCurso);

        // Panel 2: Asignar Hijos
        JPanel panelPadres = new JPanel(new BorderLayout());
        comboAcudiente = new JComboBox<>();
        listHijos = new JList<>(modelHijos);
        listHijos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton btnGuardarPadre = new JButton("Asignar Hijos al Acudiente");
        btnGuardarPadre.addActionListener(e -> guardarPadres());

        panelPadres.setBorder(BorderFactory.createTitledBorder("Asignar Hijos a Acudiente"));
        panelPadres.add(comboAcudiente, BorderLayout.NORTH);
        panelPadres.add(new JScrollPane(listHijos), BorderLayout.CENTER);
        panelPadres.add(btnGuardarPadre, BorderLayout.SOUTH);

        add(panelCurso);
        add(panelPadres);

        cargarDatos();
    }

    private void cargarDatos() {
        new Thread(() -> {
            try {
                // Inicializa cursos si no existen
                Cursos.inicializarCursos();

                String estudiantesJson = FirebaseConnection.get("Usuarios/Estudiantes");
                String acudientesJson = FirebaseConnection.get("Usuarios/Acudientes");
                String cursosJson = FirebaseConnection.get("Cursos");

                java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

                Map<String, Object> estudiantes = estudiantesJson == null || estudiantesJson.equals("null")
                        ? new HashMap<>()
                        : gson.fromJson(estudiantesJson, mapType);

                Map<String, Object> acudientes = acudientesJson == null || acudientesJson.equals("null")
                        ? new HashMap<>()
                        : gson.fromJson(acudientesJson, mapType);

                Map<String, Object> cursos = cursosJson == null || cursosJson.equals("null")
                        ? new HashMap<>()
                        : gson.fromJson(cursosJson, mapType);

                SwingUtilities.invokeLater(() -> {
                    comboEstudiantes.removeAllItems();
                    comboAcudiente.removeAllItems();
                    comboCurso.removeAllItems();
                    modelHijos.clear();
                    estudiantesMap.clear();
                    acudientesMap.clear();

                    // Poblar estudiantes
                    for (Map.Entry<String, Object> entry : estudiantes.entrySet()) {
                        String id = entry.getKey();
                        String nombre = id; // por si no tiene campo nombre

                        if (entry.getValue() instanceof Map) {
                            Object nom = ((Map<?, ?>) entry.getValue()).get("nombre");
                            if (nom != null) nombre = nom.toString();
                        }

                        estudiantesMap.put(nombre, id);
                        comboEstudiantes.addItem(nombre);
                        modelHijos.addElement(nombre);
                    }

                    // Poblar acudientes
                    for (Map.Entry<String, Object> entry : acudientes.entrySet()) {
                        String id = entry.getKey();
                        String nombre = id;

                        if (entry.getValue() instanceof Map) {
                            Object nom = ((Map<?, ?>) entry.getValue()).get("nombre");
                            if (nom != null) nombre = nom.toString();
                        }

                        acudientesMap.put(nombre, id);
                        comboAcudiente.addItem(nombre);
                    }

                    // Poblar cursos
                    for (Map.Entry<String, Object> e : cursos.entrySet()) {
                        Object val = e.getValue();
                        if (val instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> m = (Map<String, Object>) val;
                            Object nombre = m.get("nombre");
                            if (nombre != null) comboCurso.addItem(String.valueOf(nombre));
                        } else {
                            comboCurso.addItem(String.valueOf(val));
                        }
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

    private void guardarCurso() {
        try {
            String nombreEstudiante = (String) comboEstudiantes.getSelectedItem();
            String estudianteId = estudiantesMap.get(nombreEstudiante);
            String curso = (String) comboCurso.getSelectedItem();

            if (estudianteId == null || curso == null) {
                JOptionPane.showMessageDialog(this, "Selecciona estudiante y curso.");
                return;
            }

            FirebaseConnection.patch("Usuarios/Estudiantes/" + estudianteId, Map.of("curso", curso));
            JOptionPane.showMessageDialog(this, "✅ Curso asignado correctamente a " + nombreEstudiante);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al asignar curso: " + ex.getMessage());
        }
    }

    private void guardarPadres() {
        try {
            String nombreAcudiente = (String) comboAcudiente.getSelectedItem();
            String acudienteId = acudientesMap.get(nombreAcudiente);

            List<String> hijosSeleccionados = listHijos.getSelectedValuesList();
            if (acudienteId == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un acudiente.");
                return;
            }

            // Convertir nombres de hijos a IDs
            List<String> hijosIds = new ArrayList<String>();
            for (Object nombreHijo : hijosSeleccionados) {
                String id = estudiantesMap.get(nombreHijo.toString());
                if (id != null) hijosIds.add(id);
            }

            FirebaseConnection.patch("Usuarios/Acudientes/" + acudienteId, Map.of("hijos", hijosIds));
            JOptionPane.showMessageDialog(this, "✅ Hijos asignados correctamente a " + nombreAcudiente);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al asignar hijos: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AsignarCursosyPadresFrame f = new AsignarCursosyPadresFrame();
            f.setVisible(true);
        });
    }
}