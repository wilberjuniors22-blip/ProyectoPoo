package Student.Calendar;

import Admin.UserManagement.FirebaseClient;
import com.google.gson.Gson;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import Admin.Calendar.Clase;
import Admin.Calendar.HorarioClase;

public class HorarioEstudiante extends JFrame {

    private final String nombreEstudiante;
    private String gradoEstudiante;

    private DefaultTableModel tableModel;
    private JTable horarioTable;
    private FirebaseClient firebase;

    private Map<String, String> mapaAsignaturas = new HashMap<>();
    private Map<String, Color> coloresClases = new HashMap<>();

    private static final Map<String, Integer> DIA_TO_COL = new HashMap<>();
    private static final Map<String, Integer> HORA_TO_ROW = new HashMap<>();

    public HorarioEstudiante(String nombreEstudiante) {
        super("Mi Horario - " + nombreEstudiante);
        this.nombreEstudiante = nombreEstudiante;

        DIA_TO_COL.put("Lunes", 1);
        DIA_TO_COL.put("Martes", 2);
        DIA_TO_COL.put("Miércoles", 3);
        DIA_TO_COL.put("Jueves", 4);
        DIA_TO_COL.put("Viernes", 5);

        int row = 0;
        for (int h = 7; h <= 15; h++) {
            HORA_TO_ROW.put(String.format("%02d:00", h), row++);
        }

        try {
            firebase = new FirebaseClient("conexion.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando conexion.txt");
        }

        configurarUI();
        cargarAsignaturas();
        buscarGradoEstudiante();
        cargarHorario();
    }

    private void configurarUI() {
        String[] headers = {"Hora", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};
        tableModel = new DefaultTableModel(headers, 9) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        int index = 0;
        for (int h = 7; h <= 15; h++) {
            tableModel.setValueAt(String.format("%02d:00 - %02d:00", h, h + 1), index++, 0);
        }

        horarioTable = new JTable(tableModel);
        horarioTable.setRowHeight(60);
        horarioTable.setDefaultRenderer(Object.class, new ColorRenderer(coloresClases));

        JLabel lblTitulo = new JLabel("Horario de: " + this.nombreEstudiante, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setLayout(new BorderLayout());
        add(lblTitulo, BorderLayout.NORTH);
        add(new JScrollPane(horarioTable), BorderLayout.CENTER);

        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void cargarAsignaturas() {
        try {
            Map<String, Object> data = firebase.listAll("Asignaturas");
            for (var entry : data.entrySet()) {
                Map<String, Object> a = (Map<String, Object>) entry.getValue();
                String codigo = (String) a.get("codigo");
                String nombre = (String) a.get("nombre");
                if (codigo != null && nombre != null) mapaAsignaturas.put(codigo, nombre);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cargando asignaturas: " + e.getMessage());
        }
    }

    private void buscarGradoEstudiante() {
        try {
            Map<String, Object> data = firebase.listAll("Usuarios/Estudiantes");
            for (var entry : data.entrySet()) {
                Map<String, Object> estudiante = (Map<String, Object>) entry.getValue();
                String nombre = (String) estudiante.get("nombre");

                if (nombre != null && nombre.equals(this.nombreEstudiante)) {
                    this.gradoEstudiante = (String) estudiante.get("curso");
                    if (this.gradoEstudiante != null) {
                        return;
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "No se pudo encontrar el grado para: " + nombreEstudiante);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error buscando grado del estudiante.");
        }
    }

    private void cargarHorario() {
        limpiarTabla();
        coloresClases.clear();

        if (this.gradoEstudiante == null) {
            JOptionPane.showMessageDialog(this, "No se puede cargar el horario, grado no encontrado.");
            return;
        }

        try {
            Map<String, Object> data = firebase.listAll("Clases");

            for (var entry : data.entrySet()) {
                Clase c = new Gson().fromJson(new Gson().toJson(entry.getValue()), Clase.class);
                if (c == null) continue;

                // --- ESTA ES LA NUEVA LÓGICA DE FILTRADO ---
                // Revisa si la lista de 'cursos' de la clase contiene el 'grado' del estudiante.
                if (c.getCursos() != null && c.getCursos().contains(this.gradoEstudiante)) {
                    c.setNombreAsignatura(mapaAsignaturas.get(c.getAsignaturaCodigo()));
                    proyectarClase(c);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el horario: " + e.getMessage());
        }
    }

    private void proyectarClase(Clase clase) {
        if (!coloresClases.containsKey(clase.getCodigo()))
            coloresClases.put(clase.getCodigo(), Color.getHSBColor((float) Math.random(), 0.4f, 1f));

        String info =
                "<html><b>" + clase.getNombreAsignatura() + " (" +
                        clase.getAsignaturaCodigo() + ")</b><br>" +
                        clase.getDocenteNombre() + "<br><i>" +
                        clase.getSalon() + "</i><span id='" +
                        clase.getCodigo() + "'></span></html>";

        if (clase.getHorarios() == null) return;

        for (HorarioClase h : clase.getHorarios()) {
            Integer col = DIA_TO_COL.get(h.getDia());
            Integer row = HORA_TO_ROW.get(h.getHoraInicio());
            if (col == null || row == null) continue;

            int ini = Integer.parseInt(h.getHoraInicio().substring(0, 2));
            int fin = Integer.parseInt(h.getHoraFin().substring(0, 2));
            int dur = Math.max(1, fin - ini);

            for (int i = 0; i < dur; i++) {
                if (row + i < tableModel.getRowCount())
                    tableModel.setValueAt(info, row + i, col);
            }
        }
    }

    private void limpiarTabla() {
        for (int r = 0; r < tableModel.getRowCount(); r++)
            for (int c = 1; c < tableModel.getColumnCount(); c++)
                tableModel.setValueAt(null, r, c);
    }

    // --- Clase interna para renderizar colores (sin cambios) ---
    private static class ColorRenderer extends JLabel implements TableCellRenderer {
        private final Map<String, Color> colores;
        public ColorRenderer(Map<String, Color> colores) {
            this.colores = colores;
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean sel, boolean focus,
                                                       int row, int col) {
            setText(value == null ? "" : value.toString());
            setBackground(Color.WHITE);
            if (value != null) {
                String s = value.toString();
                int idStart = s.indexOf("id='");
                if (idStart != -1) {
                    int idEnd = s.indexOf("'", idStart + 4);
                    String id = s.substring(idStart + 4, idEnd);
                    Color c = colores.get(id);
                    if (c != null) setBackground(c);
                }
            }
            return this;
        }
    }
}