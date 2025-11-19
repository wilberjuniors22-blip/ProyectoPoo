package Admin.ClassManagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CrearClaseFrame extends JFrame {

    // Nodos de Firebase
    private final String ASIGNATURAS_NODE = "Asignaturas";
    private final String CLASES_NODE = "Clases";
    private final String USUARIOS_NODE = "Usuarios";

    // Conexión
    private final FirebaseConexion fb;

    // Modelos de datos y UI
    private final DefaultListModel<String> asignaturasListModel = new DefaultListModel<>();
    private final JList<String> asignaturasList = new JList<>(asignaturasListModel);
    private final DefaultListModel<String> clasesListModel = new DefaultListModel<>();
    private final JList<String> clasesList = new JList<>(clasesListModel);

    // Controles para Asignaturas
    private final JTextField txtAsignaturaNombre = new JTextField(20);
    private final JTextField txtAsignaturaCodigo = new JTextField(10);
    private final JButton btnGuardarAsignatura = new JButton("Guardar Asignatura");
    private final JButton btnEliminarAsignatura = new JButton("Eliminar Asignatura");
    private final JButton btnNuevaAsignatura = new JButton("Nueva Asignatura"); // BOTÓN NUEVO

    // Controles para Clases
    private final JComboBox<String> cbAsignaturaClase = new JComboBox<>();
    private final JComboBox<String> cbDocente = new JComboBox<>();
    private final JComboBox<String> cbSalon = new JComboBox<>();
    private final JTextField txtClaseCodigo = new JTextField(10);
    private final JTextArea taHorarios = new JTextArea(3, 20);
    private final JButton btnEditarHorarios = new JButton("Definir Horarios");
    private final JButton btnGuardarClase = new JButton("Guardar Clase");
    private final JButton btnEliminarClase = new JButton("Eliminar Clase");
    private final JButton btnNuevaClase = new JButton("Nueva Clase"); // BOTÓN NUEVO

    private final DefaultListModel<String> cursosListModel = new DefaultListModel<>();
    private final JList<String> cursosList = new JList<>(cursosListModel);

    // Datos en memoria
    private Map<String, Asignatura> asignaturasMap = new HashMap<>();
    private Map<String, Clase> clasesMap = new HashMap<>();
    private Map<String, String> docentesMap = new HashMap<>();
    private List<Clase.Horario> currentHorarios = new ArrayList<>();
    private List<String> currentCursos = new ArrayList<>();

    // Lista de Salones
    private final Map<String, Integer> salonesMap = Map.ofEntries(
            Map.entry("Aula 101 (20)", 20), Map.entry("Aula 102 (20)", 20),
            Map.entry("Aula 103 (20)", 20), Map.entry("Aula 104 (20)", 20),
            Map.entry("Aula 105 (20)", 20), Map.entry("Sala B1 (35)", 35),
            Map.entry("Sala B2 (35)", 35), Map.entry("Sala B3 (35)", 35),
            Map.entry("Sala B4 (35)", 35), Map.entry("Sala B5 (35)", 35),
            Map.entry("Auditorio C1 (50)", 50), Map.entry("Auditorio C2 (50)", 50),
            Map.entry("Auditorio C3 (50)", 50), Map.entry("Auditorio C4 (50)", 50),
            Map.entry("Auditorio C5 (50)", 50)
    );

    public CrearClaseFrame(FirebaseConexion fb) {
        super("Gestión de Asignaturas y Clases");
        this.fb = fb;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(245, 240, 247));
        setFont(new Font("SansSerif", Font.PLAIN, 14));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildRightPanel());
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(splitPane);

        loadInitialData();
        wireEvents();

        // Inicializar formularios limpios
        limpiarFormAsignatura();
        updateClaseForm(null);
    }

    // ===============================================
    // PARTES DE LA UI
    // ===============================================

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.add(buildAsignaturaPanel());
        panel.add(buildClasePanel());
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(124, 105, 144), 1),
                "Lista de Asignaturas y Clases", 0, 0, new Font("SansSerif", Font.BOLD, 16), new Color(107, 92, 123)));

        asignaturasList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollAsignaturas = new JScrollPane(asignaturasList);
        scrollAsignaturas.setBorder(BorderFactory.createTitledBorder("Asignaturas"));

        clasesList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollClases = new JScrollPane(clasesList);
        scrollClases.setBorder(BorderFactory.createTitledBorder("Clases de la Asignatura Seleccionada"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollAsignaturas, scrollClases);
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildAsignaturaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(124, 105, 144), 1),
                "Crear/Editar Asignatura", 0, 0, new Font("SansSerif", Font.BOLD, 16), new Color(107, 92, 123)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Nombre:"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1; form.add(txtAsignaturaNombre, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Código:"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1; txtAsignaturaCodigo.setEditable(false); form.add(txtAsignaturaCodigo, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.add(btnNuevaAsignatura); // AGREGADO
        btnPanel.add(btnGuardarAsignatura);
        btnPanel.add(btnEliminarAsignatura);

        panel.add(form, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildClasePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(124, 105, 144), 1),
                "Crear/Editar Clase", 0, 0, new Font("SansSerif", Font.BOLD, 16), new Color(107, 92, 123)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0; c.gridy = 0; c.weightx = 0; form.add(new JLabel("Asignatura:"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1; form.add(cbAsignaturaClase, c);
        c.gridx = 2; c.gridy = 0; c.weightx = 0; form.add(new JLabel("Código:"), c);
        c.gridx = 3; c.gridy = 0; c.weightx = 1; txtClaseCodigo.setEditable(false); form.add(txtClaseCodigo, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Docente:"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1; form.add(cbDocente, c);
        c.gridx = 2; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Salón:"), c);
        c.gridx = 3; c.gridy = 1; c.weightx = 1; form.add(cbSalon, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; form.add(new JLabel("Horarios:"), c);
        c.gridx = 1; c.gridy = 2; c.weightx = 1; form.add(new JScrollPane(taHorarios), c);
        taHorarios.setEditable(false); taHorarios.setLineWrap(true); taHorarios.setWrapStyleWord(true);
        c.gridx = 2; c.gridy = 2; c.weightx = 0; c.gridwidth = 2; form.add(btnEditarHorarios, c);

        c.gridx = 0; c.gridy = 3; c.weightx = 0; c.gridwidth = 1; form.add(new JLabel("Cursos:"), c);
        c.gridx = 1; c.gridy = 3; c.weightx = 1;
        JScrollPane scrollCursos = new JScrollPane(cursosList);
        scrollCursos.setPreferredSize(new Dimension(200, 120));
        cursosList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        form.add(scrollCursos, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.add(btnNuevaClase); // AGREGADO
        btnPanel.add(btnGuardarClase);
        btnPanel.add(btnEliminarClase);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ===============================================
    // CARGA DE DATOS
    // ===============================================

    private void loadInitialData() {
        try {
            Map<String, Map<String, Object>> usuarios = fb.listAll(USUARIOS_NODE);
            cbDocente.removeAllItems();
            docentesMap.clear();
            cbDocente.addItem("Selecciona Docente");

            Map<String, Map<String, Object>> docentesData = new HashMap<>();
            if (usuarios != null) {
                Object maybeDocentes = usuarios.get("Docentes");
                if (maybeDocentes instanceof Map) {
                    docentesData.putAll((Map<String, Map<String, Object>>) maybeDocentes);
                } else {
                    docentesData = fb.listAll(USUARIOS_NODE + "/Docentes");
                }
            } else {
                docentesData = fb.listAll(USUARIOS_NODE + "/Docentes");
            }

            docentesData.forEach((id, data) -> {
                String nombre = (String) data.get("nombre");
                if (nombre == null) nombre = (String) data.getOrDefault("nombreCompleto", "");
                if (nombre != null && !nombre.isBlank()) {
                    docentesMap.put(id, nombre);
                    cbDocente.addItem(nombre + " (ID: " + id + ")");
                }
            });

            cbSalon.removeAllItems();
            salonesMap.keySet().forEach(cbSalon::addItem);

            cursosListModel.clear();
            String[] grados = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" };
            String[] sufijos = { "A", "B" };
            for (String g : grados) {
                for (String s : sufijos) {
                    cursosListModel.addElement(g + s);
                }
            }

            cargarAsignaturasYClases();

        } catch (Exception e) {
            showError("Error al cargar datos iniciales: " + e.getMessage(), e);
        }
    }

    private void cargarAsignaturasYClases() {
        try {
            Map<String, Map<String, Object>> asignaturasData = fb.listAll(ASIGNATURAS_NODE);
            asignaturasMap.clear();
            asignaturasListModel.clear();
            cbAsignaturaClase.removeAllItems();
            cbAsignaturaClase.addItem("Selecciona Asignatura");

            if (asignaturasData != null) {
                asignaturasData.forEach((id, data) -> {
                    Asignatura a = mapToAsignatura(data);
                    a.id = id;
                    asignaturasMap.put(a.codigo, a);
                    asignaturasListModel.addElement(a.nombre + " (" + a.codigo + ")");
                    cbAsignaturaClase.addItem(a.nombre + " (" + a.codigo + ")");
                });
            }

            Map<String, Map<String, Object>> clasesData = fb.listAll(CLASES_NODE);
            clasesMap.clear();
            if (clasesData != null) {
                clasesData.forEach((id, data) -> {
                    Clase c = mapToClase(data);
                    c.id = id;
                    clasesMap.put(c.codigo, c);
                });
            }

            int selectedIndex = asignaturasList.getSelectedIndex();
            if (selectedIndex != -1) {
                asignaturasList.setSelectedIndex(selectedIndex);
            } else {
                clasesListModel.clear();
            }

        } catch (Exception e) {
            showError("Error al recargar datos de Asignaturas/Clases: " + e.getMessage(), e);
        }
    }

    private Asignatura mapToAsignatura(Map<String, Object> data) {
        Asignatura a = new Asignatura();
        a.codigo = (String) data.get("codigo");
        a.nombre = (String) data.get("nombre");
        List<String> clasesIds = (List<String>) data.get("clasesIds");
        a.clasesIds = clasesIds != null ? clasesIds : new ArrayList<>();
        return a;
    }

    private Clase mapToClase(Map<String, Object> data) {
        Clase c = new Clase();
        c.codigo = (String) data.get("codigo");
        c.asignaturaCodigo = (String) data.get("asignaturaCodigo");
        c.docenteId = (String) data.get("docenteId");
        c.docenteNombre = (String) data.get("docenteNombre");
        c.salon = (String) data.get("salon");
        Object capObj = data.getOrDefault("capacidad", 0);
        try { c.capacidad = ((Number) capObj).intValue(); } catch (Exception ignored) { c.capacidad = 0; }

        List<Map<String, String>> horariosData = (List<Map<String, String>>) data.get("horarios");
        if (horariosData != null) {
            c.horarios = horariosData.stream()
                    .map(h -> new Clase.Horario(h.get("dia"), h.get("horaInicio"), h.get("horaFin")))
                    .collect(Collectors.toList());
        }
        List<String> cursos = (List<String>) data.get("cursos");
        c.cursos = cursos != null ? cursos : new ArrayList<>();
        return c;
    }

    private void updateHorariosTextArea() {
        if (currentHorarios.isEmpty()) {
            taHorarios.setText("Sin horarios definidos.");
        } else {
            StringBuilder sb = new StringBuilder();
            currentHorarios.forEach(h -> sb.append(h).append("\n"));
            taHorarios.setText(sb.toString().trim());
        }
    }

    // ===============================================
    // EVENTOS
    // ===============================================

    private void wireEvents() {
        // Lista Asignaturas
        asignaturasList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && asignaturasList.getSelectedIndex() != -1) {
                String selectedItem = asignaturasListModel.getElementAt(asignaturasList.getSelectedIndex());
                String codigo = selectedItem.substring(selectedItem.indexOf('(') + 1, selectedItem.length() - 1);
                Asignatura selectedAsignatura = asignaturasMap.get(codigo);

                clasesListModel.clear();
                clasesMap.values().stream()
                        .filter(c -> c.asignaturaCodigo != null && c.asignaturaCodigo.equals(codigo))
                        .sorted(Comparator.comparing(c -> c.codigo))
                        .forEach(c -> clasesListModel.addElement(c.codigo + " - Prof. " + c.docenteNombre));

                txtAsignaturaNombre.setText(selectedAsignatura.nombre);
                txtAsignaturaCodigo.setText(selectedAsignatura.codigo);

                clasesList.clearSelection();
                updateClaseForm(null);
            }
        });

        // Lista Clases
        clasesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && clasesList.getSelectedIndex() != -1) {
                String selectedItem = clasesListModel.getElementAt(clasesList.getSelectedIndex());
                String codigo = selectedItem.substring(0, selectedItem.indexOf(' '));
                Clase selectedClase = clasesMap.get(codigo);
                updateClaseForm(selectedClase);
            }
        });

        // Botones Asignatura
        btnNuevaAsignatura.addActionListener(e -> {
            asignaturasList.clearSelection();
            limpiarFormAsignatura();
        });
        btnGuardarAsignatura.addActionListener(e -> guardarAsignatura());
        btnEliminarAsignatura.addActionListener(e -> eliminarAsignatura());

        // Botones Clase
        btnNuevaClase.addActionListener(e -> {
            clasesList.clearSelection();
            updateClaseForm(null);
        });
        btnGuardarClase.addActionListener(e -> guardarClase());
        btnEliminarClase.addActionListener(e -> eliminarClase());
        btnEditarHorarios.addActionListener(e -> definirHorarios());
    }

    private void updateClaseForm(Clase clase) {
        if (clase == null) {
            // NUEVO MODO
            txtClaseCodigo.setText("Automático al guardar");
            // Mantener asignatura si hay una seleccionada en la lista izquierda
            if (asignaturasList.isSelectionEmpty()) {
                cbAsignaturaClase.setSelectedIndex(0);
            } else {
                // Asegurar que el combo coincida con la lista
                String item = asignaturasList.getSelectedValue();
                for (int i = 0; i < cbAsignaturaClase.getItemCount(); i++) {
                    if (cbAsignaturaClase.getItemAt(i).equals(item)) {
                        cbAsignaturaClase.setSelectedIndex(i);
                        break;
                    }
                }
            }

            cbDocente.setSelectedIndex(0);
            cbSalon.setSelectedIndex(0);
            currentHorarios.clear();
            currentCursos.clear();
            cursosList.clearSelection();
            btnEliminarClase.setEnabled(false);
        } else {
            // MODO EDICIÓN
            txtClaseCodigo.setText(clase.codigo);
            for (int i = 1; i < cbAsignaturaClase.getItemCount(); i++) {
                if (cbAsignaturaClase.getItemAt(i).contains("(" + clase.asignaturaCodigo + ")")) {
                    cbAsignaturaClase.setSelectedIndex(i);
                    break;
                }
            }
            String docenteDisplay = clase.docenteNombre + " (ID: " + clase.docenteId + ")";
            cbDocente.setSelectedItem(docenteDisplay);
            cbSalon.setSelectedItem(clase.salon);
            currentHorarios = new ArrayList<>(clase.horarios);
            currentCursos = new ArrayList<>(clase.cursos);

            cursosList.clearSelection();
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < cursosListModel.size(); i++) {
                if (currentCursos.contains(cursosListModel.get(i))) indices.add(i);
            }
            int[] idx = indices.stream().mapToInt(Integer::intValue).toArray();
            if (idx.length > 0) cursosList.setSelectedIndices(idx);

            btnEliminarClase.setEnabled(true);
        }
        updateHorariosTextArea();
    }

    // ===============================================
    // LOGICA DE GUARDADO (CORREGIDA)
    // ===============================================

    private void guardarAsignatura() {
        String nombre = txtAsignaturaNombre.getText().trim();
        String codigoActual = txtAsignaturaCodigo.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la asignatura es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (codigoActual.equals("Automático al guardar") || codigoActual.isEmpty()) {
                String codigo = generarCodigoAsignatura(nombre);
                Asignatura nueva = new Asignatura(codigo, nombre);
                fb.createInNode(ASIGNATURAS_NODE, nueva);
                JOptionPane.showMessageDialog(this, "Asignatura creada: " + codigo);
                txtAsignaturaCodigo.setText(codigo); // Se queda en modo edición tras guardar
            } else {
                Asignatura actual = asignaturasMap.get(codigoActual);
                if (actual != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nombre", nombre);
                    fb.updateInNode(ASIGNATURAS_NODE, actual.id, updates);
                    JOptionPane.showMessageDialog(this, "Asignatura actualizada.");
                }
            }
            cargarAsignaturasYClases();
        } catch (Exception ex) {
            showError("Error al guardar asignatura.", ex);
        }
    }

    private void eliminarAsignatura() {
        String codigo = txtAsignaturaCodigo.getText().trim();
        if (codigo.equals("Automático al guardar")) return;

        Asignatura a = asignaturasMap.get(codigo);
        if (a == null) return;

        if (!clasesMap.values().stream().filter(c -> c.asignaturaCodigo.equals(codigo)).collect(Collectors.toList()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Elimina las clases primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(this, "¿Eliminar asignatura " + a.nombre + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                fb.deleteInNode(ASIGNATURAS_NODE, a.id);
                JOptionPane.showMessageDialog(this, "Eliminada.");
                limpiarFormAsignatura();
                cargarAsignaturasYClases();
            } catch (Exception ex) { showError("Error", ex); }
        }
    }

    private String generarCodigoAsignatura(String nombre) {
        String base = nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase() + "X";
        int max = 0;
        for (String key : asignaturasMap.keySet()) {
            if (key.startsWith(base)) {
                try {
                    int num = Integer.parseInt(key.substring(2));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return base + String.format("%04d", max + 1);
    }

    private void limpiarFormAsignatura() {
        txtAsignaturaNombre.setText("");
        txtAsignaturaCodigo.setText("Automático al guardar");
    }

    private void guardarClase() {
        String codigoClase = txtClaseCodigo.getText().trim();
        String asignaturaDisplay = (String) cbAsignaturaClase.getSelectedItem();

        if (asignaturaDisplay == null || !asignaturaDisplay.contains("(")) {
            JOptionPane.showMessageDialog(this, "Selecciona Asignatura.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        String asignaturaCodigo = asignaturaDisplay.substring(asignaturaDisplay.indexOf('(') + 1, asignaturaDisplay.length() - 1);

        String docenteDisplay = (String) cbDocente.getSelectedItem();
        if (docenteDisplay == null || !docenteDisplay.contains("(")) {
            JOptionPane.showMessageDialog(this, "Selecciona Docente.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        String docenteId = docenteDisplay.substring(docenteDisplay.indexOf("ID: ") + 4, docenteDisplay.length() - 1);
        String docenteNombre = docenteDisplay.substring(0, docenteDisplay.indexOf('(') - 1);
        String salon = (String) cbSalon.getSelectedItem();

        List<String> cursosSeleccionados = cursosList.getSelectedValuesList();
        if (cursosSeleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona cursos.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        if (currentHorarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Define horarios.", "Error", JOptionPane.ERROR_MESSAGE); return;
        }

        try {
            if (codigoClase.equals("Automático al guardar")) {
                // CREAR NUEVA
                String nuevoCodigo = generarCodigoClase(asignaturaCodigo);
                Map<String, Object> map = new HashMap<>();
                map.put("codigo", nuevoCodigo);
                map.put("asignaturaCodigo", asignaturaCodigo);
                map.put("docenteId", docenteId);
                map.put("docenteNombre", docenteNombre);
                map.put("salon", salon);
                map.put("capacidad", salonesMap.get(salon));
                map.put("cursos", cursosSeleccionados);

                List<Map<String, String>> horariosMap = new ArrayList<>();
                for(Clase.Horario h : currentHorarios) {
                    Map<String, String> m = new HashMap<>();
                    m.put("dia", h.dia); m.put("horaInicio", h.horaInicio); m.put("horaFin", h.horaFin);
                    horariosMap.add(m);
                }
                map.put("horarios", horariosMap);

                String id = fb.createInNode(CLASES_NODE, map);

                // Actualizar asignatura con ID de clase
                Asignatura asig = asignaturasMap.get(asignaturaCodigo);
                if(asig != null) {
                    if(asig.clasesIds == null) asig.clasesIds = new ArrayList<>();
                    asig.clasesIds.add(id);
                    Map<String, Object> upd = new HashMap<>();
                    upd.put("clasesIds", asig.clasesIds);
                    fb.updateInNode(ASIGNATURAS_NODE, asig.id, upd);
                }
                JOptionPane.showMessageDialog(this, "Clase creada: " + nuevoCodigo);

                // Opcional: Si quieres que tras guardar se quede en la nueva clase:
                // txtClaseCodigo.setText(nuevoCodigo);
                // O si prefieres limpiar para crear otra:
                updateClaseForm(null);

            } else {
                // ACTUALIZAR EXISTENTE
                Clase ex = clasesMap.get(codigoClase);
                if (ex != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("docenteId", docenteId);
                    updates.put("docenteNombre", docenteNombre);
                    updates.put("salon", salon);
                    updates.put("capacidad", salonesMap.get(salon));
                    updates.put("cursos", cursosSeleccionados);

                    List<Map<String, String>> horariosMap = new ArrayList<>();
                    for(Clase.Horario h : currentHorarios) {
                        Map<String, String> m = new HashMap<>();
                        m.put("dia", h.dia); m.put("horaInicio", h.horaInicio); m.put("horaFin", h.horaFin);
                        horariosMap.add(m);
                    }
                    updates.put("horarios", horariosMap);

                    fb.updateInNode(CLASES_NODE, ex.id, updates);
                    JOptionPane.showMessageDialog(this, "Clase actualizada.");
                }
            }
            cargarAsignaturasYClases();
        } catch (Exception ex) { showError("Error guardando clase", ex); }
    }

    // CORREGIDO: Generación de código de Clase
    private String generarCodigoClase(String asignaturaCodigo) {
        // Base es la asignatura (ej MA0000 -> MA)
        // O podríamos usar la asignatura completa MA0000 + _1
        // Tu sistema parece usar las primeras 2 letras + consecutivo global de ese prefijo
        // Si prefieres que sean consecutivas PARA ESA ASIGNATURA:

        // Lógica solicitada: Clases de MA0000 son MA0001, MA0002, etc.
        // Problema: Si el prefijo es igual, colisionan con otras asignaturas (ej MA1111).
        // Asumiremos que el código de clase sigue el patrón: [2 letras asignatura] + [4 digitos]

        String base = asignaturaCodigo.substring(0, Math.min(2, asignaturaCodigo.length()));
        int max = 0;

        // Buscar el máximo en TODAS las clases que empiecen con esas 2 letras
        for (Clase c : clasesMap.values()) {
            if (c.codigo != null && c.codigo.startsWith(base)) {
                try {
                    int num = Integer.parseInt(c.codigo.substring(base.length()));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return base + String.format("%04d", max + 1);
    }

    private void eliminarClase() {
        String codigo = txtClaseCodigo.getText().trim();
        if (codigo.equals("Automático al guardar")) return;
        Clase c = clasesMap.get(codigo);
        if (c == null) return;

        int op = JOptionPane.showConfirmDialog(this, "¿Eliminar clase " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                fb.deleteInNode(CLASES_NODE, c.id);

                Asignatura asig = asignaturasMap.get(c.asignaturaCodigo);
                if (asig != null && asig.clasesIds != null) {
                    asig.clasesIds.remove(c.id);
                    Map<String, Object> upd = new HashMap<>();
                    upd.put("clasesIds", asig.clasesIds);
                    fb.updateInNode(ASIGNATURAS_NODE, asig.id, upd);
                }
                JOptionPane.showMessageDialog(this, "Eliminada.");
                cargarAsignaturasYClases();
                updateClaseForm(null);
            } catch(Exception ex) { showError("Error", ex); }
        }
    }

    private void definirHorarios() {
        HorariosDialog dlg = new HorariosDialog(this, currentHorarios);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            currentHorarios = dlg.getHorarios();
            updateHorariosTextArea();
        }
    }

    // ... (HorariosDialog y Clases Estáticas Asignatura/Clase/Horario permanecen igual) ...
    // Incluir aquí las clases estáticas Asignatura, Clase, Horario y HorariosDialog del código anterior.

    private void showError(String title, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, title + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- COPIA LAS CLASES ESTATICAS (HorariosDialog, Asignatura, Clase) AQUI ABAJO ---
    // (Son las mismas del código previo, no han cambiado, solo la lógica de generación de IDs y los botones nuevos)

    public static class Asignatura {
        public String id;
        public String codigo;
        public String nombre;
        public List<String> clasesIds = new ArrayList<>();
        public Asignatura() {}
        public Asignatura(String codigo, String nombre) { this.codigo = codigo; this.nombre = nombre; }
    }

    public static class Clase {
        public String id;
        public String codigo;
        public String asignaturaCodigo;
        public String docenteId;
        public String docenteNombre;
        public String salon;
        public int capacidad;
        public List<Horario> horarios = new ArrayList<>();
        public List<String> estudiantesIds = new ArrayList<>();
        public List<String> cursos = new ArrayList<>();
        public Clase() {}

        public static class Horario {
            public String dia;
            public String horaInicio;
            public String horaFin;
            public Horario() {}
            public Horario(String dia, String horaInicio, String horaFin) {
                this.dia = dia; this.horaInicio = horaInicio; this.horaFin = horaFin;
            }
            @Override public String toString() { return dia + " " + horaInicio + "-" + horaFin; }
        }
    }

    private static class HorariosDialog extends JDialog {
        private final JPanel container = new JPanel(new GridBagLayout());
        private final JSpinner spNum;
        private final java.util.List<HorarioRow> rows = new ArrayList<>();
        private boolean saved = false;
        private java.util.List<Clase.Horario> horarios = new ArrayList<>();

        public HorariosDialog(Window owner, List<Clase.Horario> current) {
            super(owner, "Definir Horarios", ModalityType.APPLICATION_MODAL);
            setSize(600, 400);
            setLocationRelativeTo(owner);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(6,6,6,6);
            c.fill = GridBagConstraints.HORIZONTAL;

            spNum = new JSpinner(new SpinnerNumberModel(Math.max(1, current.size()), 1, 10, 1));
            c.gridx = 0; c.gridy = 0; c.gridwidth = 1; container.add(new JLabel("Número de clases por semana:"), c);
            c.gridx = 1; c.gridy = 0; container.add(spNum, c);

            JButton btnApply = new JButton("Aplicar");
            c.gridx = 2; c.gridy = 0; container.add(btnApply, c);

            JPanel rowsPanel = new JPanel(new GridBagLayout());
            JScrollPane scroll = new JScrollPane(rowsPanel);
            scroll.setPreferredSize(new Dimension(560, 260));
            c.gridx = 0; c.gridy = 1; c.gridwidth = 3; container.add(scroll, c);

            btnApply.addActionListener(e -> {
                int n = (int) spNum.getValue();
                rowsPanel.removeAll();
                rows.clear();
                GridBagConstraints rc = new GridBagConstraints();
                rc.insets = new Insets(4,4,4,4);
                rc.fill = GridBagConstraints.HORIZONTAL;
                String[] dias = {"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"};
                for (int i = 0; i < n; i++) {
                    rc.gridx = 0; rc.gridy = i;
                    rowsPanel.add(new JLabel("Clase " + (i+1) + " - Día:"), rc);
                    JComboBox<String> cbDia = new JComboBox<>(dias);
                    rc.gridx = 1; rowsPanel.add(cbDia, rc);

                    rc.gridx = 2; rowsPanel.add(new JLabel("Inicio (hora):"), rc);
                    JSpinner spInicio = new JSpinner(new SpinnerNumberModel(7, 7, 16, 1));
                    rc.gridx = 3; rowsPanel.add(spInicio, rc);

                    rc.gridx = 4; rowsPanel.add(new JLabel("Fin (hora):"), rc);
                    JSpinner spFin = new JSpinner(new SpinnerNumberModel(8, 7, 16, 1));
                    rc.gridx = 5; rowsPanel.add(spFin, rc);

                    HorarioRow hr = new HorarioRow(cbDia, spInicio, spFin);
                    rows.add(hr);
                }
                rowsPanel.revalidate();
                rowsPanel.repaint();
            });

            if (current != null && !current.isEmpty()) {
                spNum.setValue(current.size());
                btnApply.doClick();
                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < current.size() && i < rows.size(); i++) {
                        HorarioRow r = rows.get(i);
                        Clase.Horario hh = current.get(i);
                        r.cbDia.setSelectedItem(hh.dia);
                        try {
                            int hi = Integer.parseInt(hh.horaInicio.split(":")[0]);
                            int hf = Integer.parseInt(hh.horaFin.split(":")[0]);
                            r.spInicio.setValue(hi);
                            r.spFin.setValue(hf);
                        } catch (Exception ignored) {}
                    }
                });
            } else {
                spNum.setValue(1);
                btnApply.doClick();
            }

            JButton btnSave = new JButton("Guardar");
            JButton btnCancel = new JButton("Cancelar");
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.add(btnSave);
            footer.add(btnCancel);

            btnSave.addActionListener(e -> {
                List<Clase.Horario> tmp = new ArrayList<>();
                for (HorarioRow rr : rows) {
                    String dia = (String) rr.cbDia.getSelectedItem();
                    int inicio = (int) rr.spInicio.getValue();
                    int fin = (int) rr.spFin.getValue();
                    if (fin <= inicio) {
                        JOptionPane.showMessageDialog(this, "Hora fin debe ser mayor a inicio.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String hi = String.format("%02d:00", inicio);
                    String hf = String.format("%02d:00", fin);
                    tmp.add(new Clase.Horario(dia, hi, hf));
                }
                horarios = tmp;
                saved = true;
                setVisible(false);
            });

            btnCancel.addActionListener(e -> {
                saved = false;
                setVisible(false);
            });

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(container, BorderLayout.CENTER);
            getContentPane().add(footer, BorderLayout.SOUTH);
        }

        public boolean isSaved() { return saved; }
        public List<Clase.Horario> getHorarios() { return horarios; }

        private static class HorarioRow {
            JComboBox<String> cbDia;
            JSpinner spInicio;
            JSpinner spFin;
            public HorarioRow(JComboBox<String> cbDia, JSpinner spInicio, JSpinner spFin) {
                this.cbDia = cbDia; this.spInicio = spInicio; this.spFin = spFin;
            }
        }
    }
}