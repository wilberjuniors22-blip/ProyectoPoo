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
    private final String USUARIOS_NODE = "Usuarios"; // Usamos Usuarios/Docentes

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

    // Controles para Clases
    private final JComboBox<String> cbAsignaturaClase = new JComboBox<>();
    private final JComboBox<String> cbDocente = new JComboBox<>();
    private final JComboBox<String> cbSalon = new JComboBox<>();
    private final JTextField txtClaseCodigo = new JTextField(10);
    private final JSpinner spNumClases = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1)); // Clases por semana (hasta 10)
    private final JTextArea taHorarios = new JTextArea(3, 20); // Para mostrar horarios
    private final JButton btnEditarHorarios = new JButton("Definir Horarios");
    private final JButton btnGuardarClase = new JButton("Guardar Clase");
    private final JButton btnEliminarClase = new JButton("Eliminar Clase");
    // Reemplazamos el botón Gestion Estudiantes por la lista de cursos
    private final DefaultListModel<String> cursosListModel = new DefaultListModel<>();
    private final JList<String> cursosList = new JList<>(cursosListModel);

    // Datos en memoria
    private Map<String, Asignatura> asignaturasMap = new HashMap<>();
    private Map<String, Clase> clasesMap = new HashMap<>();
    private Map<String, String> docentesMap = new HashMap<>(); // ID -> Nombre
    private List<Clase.Horario> currentHorarios = new ArrayList<>();
    private List<String> currentCursos = new ArrayList<>();

    // Lista de Salones (Capacidad: 20, 35, 50)
    private final Map<String, Integer> salonesMap = Map.ofEntries(
            Map.entry("Aula 101 (20)", 20),
            Map.entry("Aula 102 (20)", 20),
            Map.entry("Aula 103 (20)", 20),
            Map.entry("Aula 104 (20)", 20),
            Map.entry("Aula 105 (20)", 20),

            Map.entry("Sala B1 (35)", 35),
            Map.entry("Sala B2 (35)", 35),
            Map.entry("Sala B3 (35)", 35),
            Map.entry("Sala B4 (35)", 35),
            Map.entry("Sala B5 (35)", 35),

            Map.entry("Auditorio C1 (50)", 50),
            Map.entry("Auditorio C2 (50)", 50),
            Map.entry("Auditorio C3 (50)", 50),
            Map.entry("Auditorio C4 (50)", 50),
            Map.entry("Auditorio C5 (50)", 50)
    );

    public CrearClaseFrame(FirebaseConexion fb) {
        super("Gestión de Asignaturas y Clases");
        this.fb = fb;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cierra esta ventana
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Estilo
        getContentPane().setBackground(new Color(245, 240, 247));
        setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Layout principal: Dos paneles divididos
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildRightPanel());
        splitPane.setResizeWeight(0.5); // División 50/50
        splitPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(splitPane);

        loadInitialData();
        wireEvents();
        updateClaseForm(null); // Inicializa el formulario de clase en modo nuevo
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

// Lista de Asignaturas
        asignaturasList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollAsignaturas = new JScrollPane(asignaturasList);
        scrollAsignaturas.setBorder(BorderFactory.createTitledBorder("Asignaturas"));

// Lista de Clases (sub-lista)
        clasesList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollClases = new JScrollPane(clasesList);
        scrollClases.setBorder(BorderFactory.createTitledBorder("Clases de la Asignatura Seleccionada"));

// División del panel derecho
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

// Fila 1: Nombre
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Nombre:"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1; form.add(txtAsignaturaNombre, c);

// Fila 2: Código
        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Código:"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1; txtAsignaturaCodigo.setEditable(false); form.add(txtAsignaturaCodigo, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
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

// Fila 1: Asignatura y Código de Clase
        c.gridx = 0; c.gridy = 0; c.weightx = 0; form.add(new JLabel("Asignatura:"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1; form.add(cbAsignaturaClase, c);
        c.gridx = 2; c.gridy = 0; c.weightx = 0; form.add(new JLabel("Código:"), c);
        c.gridx = 3; c.gridy = 0; c.weightx = 1; txtClaseCodigo.setEditable(false); form.add(txtClaseCodigo, c);

// Fila 2: Docente y Salón
        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Docente:"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1; form.add(cbDocente, c);
        c.gridx = 2; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Salón:"), c);
        c.gridx = 3; c.gridy = 1; c.weightx = 1; form.add(cbSalon, c);

// Fila 3: Horarios y Definir
        c.gridx = 0; c.gridy = 2; c.weightx = 0; form.add(new JLabel("Horarios:"), c);
        c.gridx = 1; c.gridy = 2; c.weightx = 1; form.add(new JScrollPane(taHorarios), c);
        taHorarios.setEditable(false); taHorarios.setLineWrap(true); taHorarios.setWrapStyleWord(true);
        c.gridx = 2; c.gridy = 2; c.weightx = 0; c.gridwidth = 2; form.add(btnEditarHorarios, c);

// Fila 4: Cursos list (reemplaza gestión estudiantes)
        c.gridx = 0; c.gridy = 3; c.weightx = 0; c.gridwidth = 1; form.add(new JLabel("Cursos:"), c);
        c.gridx = 1; c.gridy = 3; c.weightx = 1;
        JScrollPane scrollCursos = new JScrollPane(cursosList);
        scrollCursos.setPreferredSize(new Dimension(200, 120));
        cursosList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        form.add(scrollCursos, c);

// Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.add(btnGuardarClase);
        btnPanel.add(btnEliminarClase);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ===============================================
    // CARGA DE DATOS Y LÓGICA
    // ===============================================

    private void loadInitialData() {
        try {
            // 1. Cargar Docentes para el ComboBox desde Usuarios/Docentes
            Map<String, Map<String, Object>> usuarios = fb.listAll(USUARIOS_NODE);
            cbDocente.removeAllItems();
            docentesMap.clear();
            cbDocente.addItem("Selecciona Docente");

            // usuarios map structure: usuarios -> subnodes (Acudientes, Administradores, Docentes)
            Map<String, Map<String, Object>> docentesData = new HashMap<>();
            // buscar subnode "Docentes"
            if (usuarios != null) {
                Object maybeDocentes = usuarios.get("Docentes");
                // si usuarios fueron devueltos en la raíz con subnodos
                if (maybeDocentes instanceof Map) {
                    // ya es el caso que fb.listAll devolvió Usuarios con subnodos (cuando usaste la ruta Usuarios)
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> sub = (Map<String, Map<String, Object>>) maybeDocentes;
                    docentesData.putAll(sub);
                } else {
                    // Si fb.listAll(USUARIOS_NODE) devolvió cada subnodo como hijo (id->map), debemos iterar
                    // Intentamos leer directamente "Docentes" nodo por separado
                    docentesData = fb.listAll(USUARIOS_NODE + "/Docentes");
                }
            } else {
                docentesData = fb.listAll(USUARIOS_NODE + "/Docentes");
            }

            // llenar combo con docentes
            docentesData.forEach((id, data) -> {
                String nombre = (String) data.get("nombre");
                if (nombre == null) {
                    // Si la estructura es distinta, intentar nombre completo en campos alternativos
                    nombre = (String) data.getOrDefault("nombreCompleto", data.getOrDefault("nombre", ""));
                }
                if (nombre != null && !nombre.isBlank()) {
                    docentesMap.put(id, nombre);
                    cbDocente.addItem(nombre + " (ID: " + id + ")");
                }
            });

            // 2. Llenar ComboBox de Salones
            cbSalon.removeAllItems();
            salonesMap.keySet().forEach(cbSalon::addItem);

            // 3. Inicializar lista de cursos (1A..11B)
            cursosListModel.clear();
            String[] grados = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" };
            String[] sufijos = { "A", "B" };
            for (String g : grados) {
                for (String s : sufijos) {
                    cursosListModel.addElement(g + s);
                }
            }

            // 4. Cargar Asignaturas y Clases
            cargarAsignaturasYClases();

        } catch (Exception e) {
            showError("Error al cargar datos iniciales: " + e.getMessage(), e);
        }
    }

    private void cargarAsignaturasYClases() {
        try {
            // Cargar Asignaturas
            Map<String, Map<String, Object>> asignaturasData = fb.listAll(ASIGNATURAS_NODE);
            asignaturasMap.clear();
            asignaturasListModel.clear();
            cbAsignaturaClase.removeAllItems();
            cbAsignaturaClase.addItem("Selecciona Asignatura");

            // Mapear y llenar lista de asignaturas
            if (asignaturasData != null) {
                asignaturasData.forEach((id, data) -> {
                    Asignatura a = mapToAsignatura(data);
                    a.id = id;
                    asignaturasMap.put(a.codigo, a);
                    asignaturasListModel.addElement(a.nombre + " (" + a.codigo + ")");
                    cbAsignaturaClase.addItem(a.nombre + " (" + a.codigo + ")");
                });
            }

            // Cargar Clases
            Map<String, Map<String, Object>> clasesData = fb.listAll(CLASES_NODE);
            clasesMap.clear();
            if (clasesData != null) {
                clasesData.forEach((id, data) -> {
                    Clase c = mapToClase(data);
                    c.id = id;
                    clasesMap.put(c.codigo, c);
                });
            }

            // Refrescar la sublista de clases si hay una asignatura seleccionada
            int selectedIndex = asignaturasList.getSelectedIndex();
            if (selectedIndex != -1) {
                asignaturasList.setSelectedIndex(selectedIndex); // Dispara el evento de selección
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
        @SuppressWarnings("unchecked")
        List<String> clasesIds = (List<String>) data.get("clasesIds");
        if (clasesIds != null) {
            a.clasesIds = clasesIds;
        } else {
            a.clasesIds = new ArrayList<>();
        }
        return a;
    }

    private Clase mapToClase(Map<String, Object> data) {
        Clase c = new Clase();
        c.codigo = (String) data.get("codigo");
        c.asignaturaCodigo = (String) data.get("asignaturaCodigo");
        c.docenteId = (String) data.get("docenteId");
        c.docenteNombre = (String) data.get("docenteNombre");
        c.salon = (String) data.get("salon");
        // capacidad puede venir como Double/Long
        Object capObj = data.getOrDefault("capacidad", 0);
        try {
            c.capacidad = ((Number) capObj).intValue();
        } catch (Exception ignored) {
            c.capacidad = 0;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> horariosData = (List<Map<String, String>>) data.get("horarios");
        if (horariosData != null) {
            c.horarios = horariosData.stream()
                    .map(h -> new Clase.Horario(h.get("dia"), h.get("horaInicio"), h.get("horaFin")))
                    .collect(Collectors.toList());
        } else {
            c.horarios = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        List<String> estudiantesIds = (List<String>) data.get("estudiantesIds");
        if (estudiantesIds != null) {
            c.estudiantesIds = estudiantesIds;
        } else {
            c.estudiantesIds = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        List<String> cursos = (List<String>) data.get("cursos");
        if (cursos != null) {
            c.cursos = cursos;
        } else {
            c.cursos = new ArrayList<>();
        }

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
    // LÓGICA DE EVENTOS
    // ===============================================

    private void wireEvents() {
        // Evento: Seleccionar Asignatura (Panel Derecho)
        asignaturasList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && asignaturasList.getSelectedIndex() != -1) {
                String selectedItem = asignaturasListModel.getElementAt(asignaturasList.getSelectedIndex());
                String codigo = selectedItem.substring(selectedItem.indexOf('(') + 1, selectedItem.length() - 1);
                Asignatura selectedAsignatura = asignaturasMap.get(codigo);

                // Cargar clases en la lista secundaria
                clasesListModel.clear();
                clasesMap.values().stream()
                        .filter(c -> c.asignaturaCodigo != null && c.asignaturaCodigo.equals(codigo))
                        .sorted(Comparator.comparing(c -> c.codigo))
                        .forEach(c -> clasesListModel.addElement(c.codigo + " - Prof. " + c.docenteNombre));

                // Cargar datos en el formulario de Asignatura para edición
                txtAsignaturaNombre.setText(selectedAsignatura.nombre);
                txtAsignaturaCodigo.setText(selectedAsignatura.codigo);

                // Deseleccionar clase y reiniciar form de clase
                clasesList.clearSelection();
                updateClaseForm(null);
            }
        });

        // Evento: Seleccionar Clase (Panel Derecho)
        clasesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && clasesList.getSelectedIndex() != -1) {
                String selectedItem = clasesListModel.getElementAt(clasesList.getSelectedIndex());
                String codigo = selectedItem.substring(0, selectedItem.indexOf(' '));
                Clase selectedClase = clasesMap.get(codigo);
                updateClaseForm(selectedClase);
                // Seleccionar cursos en la lista de cursos según selectedClase
                if (selectedClase != null && selectedClase.cursos != null) {
                    cursosList.clearSelection();
                    // seleccionar índices
                    List<Integer> indices = new ArrayList<>();
                    for (int i = 0; i < cursosListModel.size(); i++) {
                        if (selectedClase.cursos.contains(cursosListModel.get(i))) {
                            indices.add(i);
                        }
                    }
                    int[] idx = indices.stream().mapToInt(Integer::intValue).toArray();
                    cursosList.setSelectedIndices(idx);
                }
            }
        });

        // Evento: Botones de Asignatura
        btnGuardarAsignatura.addActionListener(e -> guardarAsignatura());
        btnEliminarAsignatura.addActionListener(e -> eliminarAsignatura());

        // Evento: Botones de Clase
        btnGuardarClase.addActionListener(e -> guardarClase());
        btnEliminarClase.addActionListener(e -> eliminarClase());
        btnEditarHorarios.addActionListener(e -> definirHorarios());
    }

    private void updateClaseForm(Clase clase) {
        if (clase == null) {
            // Modo Nuevo
            txtClaseCodigo.setText("Automático al guardar");
            cbAsignaturaClase.setSelectedIndex(0);
            cbDocente.setSelectedIndex(0);
            cbSalon.setSelectedIndex(0);
            currentHorarios.clear();
            currentCursos.clear();
            cursosList.clearSelection();
            btnEliminarClase.setEnabled(false);
        } else {
            // Modo Editar
            txtClaseCodigo.setText(clase.codigo);
            // Seleccionar Asignatura en el ComboBox
            for (int i = 1; i < cbAsignaturaClase.getItemCount(); i++) {
                if (cbAsignaturaClase.getItemAt(i).contains("(" + clase.asignaturaCodigo + ")")) {
                    cbAsignaturaClase.setSelectedIndex(i);
                    break;
                }
            }

            // Seleccionar Docente
            String docenteDisplay = clase.docenteNombre + " (ID: " + clase.docenteId + ")";
            cbDocente.setSelectedItem(docenteDisplay);

            // Seleccionar Salón
            cbSalon.setSelectedItem(clase.salon);

            currentHorarios = new ArrayList<>(clase.horarios);
            currentCursos = new ArrayList<>(clase.cursos);
            updateHorariosTextArea();
            // seleccionar cursos en UI
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
    // LÓGICA DE ASIGNATURAS
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
                // Crear nueva asignatura
                String codigo = generarCodigoAsignatura(nombre);
                Asignatura nueva = new Asignatura(codigo, nombre);
                String id = fb.createInNode(ASIGNATURAS_NODE, nueva);
                JOptionPane.showMessageDialog(this, "Asignatura creada: " + nombre + " con código " + codigo);
                txtAsignaturaCodigo.setText(codigo);
            } else {
                // Actualizar asignatura existente
                Asignatura actual = asignaturasMap.values().stream()
                        .filter(a -> a.codigo.equals(codigoActual))
                        .findFirst().orElse(null);

                if (actual != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nombre", nombre);
                    fb.updateInNode(ASIGNATURAS_NODE, actual.id, updates);
                    JOptionPane.showMessageDialog(this, "Asignatura actualizada.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Asignatura no encontrada para actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            cargarAsignaturasYClases();
        } catch (Exception ex) {
            showError("Error al guardar la asignatura.", ex);
        }
    }

    private void eliminarAsignatura() {
        String codigo = txtAsignaturaCodigo.getText().trim();
        if (codigo.isEmpty() || codigo.equals("Automático al guardar")) {
            JOptionPane.showMessageDialog(this, "Selecciona una asignatura para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Asignatura a = asignaturasMap.values().stream()
                .filter(asig -> asig.codigo.equals(codigo))
                .findFirst().orElse(null);

        if (a == null) {
            JOptionPane.showMessageDialog(this, "Asignatura no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!clasesMap.values().stream().filter(c -> c.asignaturaCodigo.equals(codigo)).collect(Collectors.toList()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "No puedes eliminar una asignatura que tiene clases asociadas. Elimina las clases primero.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar la asignatura: " + a.nombre + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                fb.deleteInNode(ASIGNATURAS_NODE, a.id);
                JOptionPane.showMessageDialog(this, "Asignatura eliminada.");
                limpiarFormAsignatura();
                cargarAsignaturasYClases();
            } catch (Exception ex) {
                showError("Error al eliminar la asignatura.", ex);
            }
        }
    }

    private String generarCodigoAsignatura(String nombre) {
        String base = nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : nombre.toUpperCase() + "X";

        AtomicInteger count = new AtomicInteger(0);
        asignaturasMap.keySet().stream()
                .filter(c -> c.startsWith(base))
                .forEach(c -> {
                    try {
                        int num = Integer.parseInt(c.substring(c.length() - 4));
                        if (num >= count.get()) {
                            count.set(num + 1);
                        }
                    } catch (NumberFormatException ignored) {}
                });

        return base + String.format("%04d", count.get());
    }

    private void limpiarFormAsignatura() {
        txtAsignaturaNombre.setText("");
        txtAsignaturaCodigo.setText("Automático al guardar");
    }

    // ===============================================
    // LÓGICA DE CLASES
    // ===============================================

    private void guardarClase() {
        String codigoClase = txtClaseCodigo.getText().trim();
        String asignaturaDisplay = (String) cbAsignaturaClase.getSelectedItem();
        String docenteDisplay = (String) cbDocente.getSelectedItem();
        String salon = (String) cbSalon.getSelectedItem();

        if (asignaturaDisplay == null || !asignaturaDisplay.contains("(")) {
            JOptionPane.showMessageDialog(this, "Selecciona una asignatura válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (docenteDisplay == null || !docenteDisplay.contains("(")) {
            JOptionPane.showMessageDialog(this, "Selecciona un docente válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentHorarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes definir al menos un horario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener cursos seleccionados
        List<String> cursosSeleccionados = cursosList.getSelectedValuesList();
        if (cursosSeleccionados == null || cursosSeleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un curso para asignar la clase.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String asignaturaCodigo = asignaturaDisplay.substring(asignaturaDisplay.indexOf('(') + 1, asignaturaDisplay.length() - 1);
        String docenteId = docenteDisplay.substring(docenteDisplay.indexOf("ID: ") + 4, docenteDisplay.length() - 1);
        String docenteNombre = docenteDisplay.substring(0, docenteDisplay.indexOf('(') - 1);
        int capacidad = salonesMap.getOrDefault(salon, 0);

        try {
            // VALIDACIONES por curso
            for (String curso : cursosSeleccionados) {
                // 1) Si ya existe una clase de la misma asignatura en este curso -> bloquear
                boolean existeMismaAsignatura = clasesMap.values().stream()
                        .filter(c -> c.cursos != null && c.cursos.contains(curso))
                        .anyMatch(c -> c.asignaturaCodigo != null && c.asignaturaCodigo.equals(asignaturaCodigo) &&
                                !(codigoClase.equals(c.codigo))); // permitir editar misma clase
                if (existeMismaAsignatura) {
                    JOptionPane.showMessageDialog(this,
                            "El curso " + curso + " ya tiene una clase de esta asignatura.", "Conflicto", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2) Si ya existe otra clase (cualquier asignatura) que solape horarios en este curso -> bloquear
                for (Clase existente : clasesMap.values()) {
                    if (existente.cursos != null && existente.cursos.contains(curso)) {
                        // Si estamos editando, ignorar la propia clase
                        if (codigoClase != null && !codigoClase.equals("Automático al guardar") && codigoClase.equals(existente.codigo)) {
                            continue;
                        }
                        // comprobar solapamiento entre currentHorarios y existente.horarios
                        for (Clase.Horario hNew : currentHorarios) {
                            for (Clase.Horario hExist : existente.horarios) {
                                if (horariosSolapan(hNew, hExist)) {
                                    JOptionPane.showMessageDialog(this,
                                            "Conflicto de horario en curso " + curso + " con la clase " + existente.codigo + ".",
                                            "Conflicto de Horario", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            if (codigoClase.equals("Automático al guardar")) {
                // Crear nueva clase (usamos Map para enviar a Firebase)
                String nuevoCodigo = generarCodigoClase(asignaturaCodigo);
                Map<String, Object> nuevaClaseMap = new HashMap<>();
                nuevaClaseMap.put("codigo", nuevoCodigo);
                nuevaClaseMap.put("asignaturaCodigo", asignaturaCodigo);
                nuevaClaseMap.put("docenteId", docenteId);
                nuevaClaseMap.put("docenteNombre", docenteNombre);
                nuevaClaseMap.put("salon", salon);
                nuevaClaseMap.put("capacidad", capacidad);
                // horarios -> lista de maps
                List<Map<String, String>> horariosToSave = currentHorarios.stream().map(h -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("dia", h.dia);
                    m.put("horaInicio", h.horaInicio);
                    m.put("horaFin", h.horaFin);
                    return m;
                }).collect(Collectors.toList());
                nuevaClaseMap.put("horarios", horariosToSave);
                nuevaClaseMap.put("cursos", cursosSeleccionados);
                String id = fb.createInNode(CLASES_NODE, nuevaClaseMap);

                // Actualizar lista de clases en la asignatura
                Asignatura asig = asignaturasMap.get(asignaturaCodigo);
                if (asig != null) {
                    if (asig.clasesIds == null) asig.clasesIds = new ArrayList<>();
                    asig.clasesIds.add(id);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("clasesIds", asig.clasesIds);
                    fb.updateInNode(ASIGNATURAS_NODE, asig.id, updates);
                }

                JOptionPane.showMessageDialog(this, "Clase creada: " + nuevoCodigo);

            } else {
                // Actualizar clase existente
                Clase claseExistente = clasesMap.get(codigoClase);
                if (claseExistente != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("docenteId", docenteId);
                    updates.put("docenteNombre", docenteNombre);
                    updates.put("salon", salon);
                    updates.put("capacidad", capacidad);

                    List<Map<String, String>> horariosToSave = currentHorarios.stream().map(h -> {
                        Map<String, String> m = new HashMap<>();
                        m.put("dia", h.dia);
                        m.put("horaInicio", h.horaInicio);
                        m.put("horaFin", h.horaFin);
                        return m;
                    }).collect(Collectors.toList());
                    updates.put("horarios", horariosToSave);
                    updates.put("cursos", cursosSeleccionados);

                    fb.updateInNode(CLASES_NODE, claseExistente.id, updates);
                    JOptionPane.showMessageDialog(this, "Clase actualizada: " + codigoClase);
                }
            }
            limpiarFormAsignatura();
            cargarAsignaturasYClases();
            updateClaseForm(null);
        } catch (Exception ex) {
            showError("Error al guardar la clase.", ex);
        }
    }

    private void eliminarClase() {
        String codigo = txtClaseCodigo.getText().trim();
        if (codigo.isEmpty() || codigo.equals("Automático al guardar")) {
            JOptionPane.showMessageDialog(this, "Selecciona una clase para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Clase clase = clasesMap.get(codigo);
        if (clase == null) {
            JOptionPane.showMessageDialog(this, "Clase no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar la clase: " + codigo + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                // 1. Eliminar la clase del nodo CLASES
                fb.deleteInNode(CLASES_NODE, clase.id);

                // 2. Eliminar la referencia de la clase en la Asignatura
                Asignatura asig = asignaturasMap.values().stream()
                        .filter(a -> a.codigo.equals(clase.asignaturaCodigo))
                        .findFirst().orElse(null);

                if (asig != null && asig.clasesIds != null) {
                    asig.clasesIds.remove(clase.id);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("clasesIds", asig.clasesIds);
                    fb.updateInNode(ASIGNATURAS_NODE, asig.id, updates);
                }

                JOptionPane.showMessageDialog(this, "Clase eliminada.");
                cargarAsignaturasYClases();
                updateClaseForm(null);
            } catch (Exception ex) {
                showError("Error al eliminar la clase.", ex);
            }
        }
    }

    private String generarCodigoClase(String asignaturaCodigo) {
        String base = asignaturaCodigo.substring(0, Math.min(2, asignaturaCodigo.length()));

        long nextIndex = clasesMap.values().stream()
                .filter(c -> c.asignaturaCodigo != null && c.asignaturaCodigo.equals(asignaturaCodigo))
                .count() + 1; // El siguiente índice será el número total de clases + 1

        return base + String.format("%04d", nextIndex);
    }

    // Comprueba si dos horarios se solapan (mismo día y rangos horarios con intersección)
    private boolean horariosSolapan(Clase.Horario a, Clase.Horario b) {
        if (a == null || b == null) return false;
        if (!a.dia.equalsIgnoreCase(b.dia)) return false;
        try {
            int aStart = Integer.parseInt(a.horaInicio.split(":")[0]);
            int aEnd = Integer.parseInt(a.horaFin.split(":")[0]);
            int bStart = Integer.parseInt(b.horaInicio.split(":")[0]);
            int bEnd = Integer.parseInt(b.horaFin.split(":")[0]);
            // solapan si aStart < bEnd && bStart < aEnd
            return (aStart < bEnd) && (bStart < aEnd);
        } catch (Exception e) {
            return false;
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

    // Dialogo para definir horarios
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
                    JSpinner spInicio = new JSpinner(new SpinnerNumberModel(7, 6, 20, 1));
                    rc.gridx = 3; rowsPanel.add(spInicio, rc);

                    rc.gridx = 4; rowsPanel.add(new JLabel("Fin (hora):"), rc);
                    JSpinner spFin = new JSpinner(new SpinnerNumberModel(8, 7, 22, 1));
                    rc.gridx = 5; rowsPanel.add(spFin, rc);

                    HorarioRow hr = new HorarioRow(cbDia, spInicio, spFin);
                    rows.add(hr);
                }
                rowsPanel.revalidate();
                rowsPanel.repaint();
            });

            // Si había horarios previos, precargar
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
                // default create 1 row
                spNum.setValue(1);
                btnApply.doClick();
            }

            JButton btnSave = new JButton("Guardar");
            JButton btnCancel = new JButton("Cancelar");
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.add(btnSave);
            footer.add(btnCancel);

            btnSave.addActionListener(e -> {
                // Validaciones: cada fila inicio < fin
                List<Clase.Horario> tmp = new ArrayList<>();
                for (HorariosDialog.HorarioRow rr : rows) {
                    String dia = (String) rr.cbDia.getSelectedItem();
                    int inicio = (int) rr.spInicio.getValue();
                    int fin = (int) rr.spFin.getValue();
                    if (fin <= inicio) {
                        JOptionPane.showMessageDialog(this, "La hora fin debe ser mayor que la hora inicio.", "Error", JOptionPane.ERROR_MESSAGE);
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

        public boolean isSaved() {
            return saved;
        }

        public List<Clase.Horario> getHorarios() {
            return horarios;
        }

        private static class HorarioRow {
            JComboBox<String> cbDia;
            JSpinner spInicio;
            JSpinner spFin;
            public HorarioRow(JComboBox<String> cbDia, JSpinner spInicio, JSpinner spFin) {
                this.cbDia = cbDia; this.spInicio = spInicio; this.spFin = spFin;
            }
        }
    }

    private void gestionarEstudiantes() {
        // Ahora usamos cursosList para gestionar por curso; si quieres dialogo, se puede implementar
        JOptionPane.showMessageDialog(this, "La gestión de estudiantes ahora se hace por curso (selecciona cursos en la lista).");
    }

    private void showError(String title, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, title + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Método main de prueba
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Usamos la nueva clase de conexión (archivo conexion.txt o el que uses)
                FirebaseConexion fb = new FirebaseConexion("segundaconexion.txt");
                CrearClaseFrame frame = new CrearClaseFrame(fb);
                frame.setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error al inicializar la conexión con Firebase: " + e.getMessage(),
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

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
        public Clase(String codigo, String asignaturaCodigo, String docenteId, String docenteNombre,
                     String salon, int capacidad, List<Horario> horarios) {
            this.codigo = codigo;
            this.asignaturaCodigo = asignaturaCodigo;
            this.docenteId = docenteId;
            this.docenteNombre = docenteNombre;
            this.salon = salon;
            this.capacidad = capacidad;
            this.horarios = horarios != null ? horarios : new ArrayList<>();
        }

        public static class Horario {
            public String dia;
            public String horaInicio;
            public String horaFin;
            public Horario() {}
            public Horario(String dia, String horaInicio, String horaFin) {
                this.dia = dia; this.horaInicio = horaInicio; this.horaFin = horaFin;
            }
            @Override
            public String toString() {
                return dia + " " + horaInicio + "-" + horaFin;
            }
        }
    }
}
