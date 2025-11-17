package Admin.Calendar;

import Admin.ClassManagement.FirebaseConexion;
import com.google.gson.Gson;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;

public class Horario extends JFrame {

    private JComboBox<String> cursosComboBox;
    private JComboBox<String> docentesComboBox;

    private DefaultTableModel tableModel;
    private JTable horarioTable;

    private FirebaseConexion firebase;

    private Map<String, String> mapaAsignaturas = new HashMap<>();
    private Map<String, String> mapaDocentesID = new HashMap<>();
    private Map<String, Color> coloresClases = new HashMap<>();

    private static final Map<String, Integer> DIA_TO_COL = new HashMap<>();
    private static final Map<String, Integer> HORA_TO_ROW = new HashMap<>();

    public Horario() {
        super("Horario de Clases");

        DIA_TO_COL.put("Lunes", 1);
        DIA_TO_COL.put("Martes", 2);
        DIA_TO_COL.put("Miércoles", 3);
        DIA_TO_COL.put("Jueves", 4);
        DIA_TO_COL.put("Viernes", 5);

        int row = 0;
        for (int h = 7; h <= 15; h++) {
            HORA_TO_ROW.put(String.format("%02d:00", h), row++);
        }

        try { firebase = new FirebaseConexion("conexion.txt"); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Error cargando conexion.txt"); }

        configurarUI();
        cargarAsignaturas();
        cargarCursos();
        cargarDocentes();
    }

    private void configurarUI() {
        String[] headers = {"Hora", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};
        tableModel = new DefaultTableModel(headers, 9) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        int index = 0;
        for (int h = 7; h <= 15; h++) {
            tableModel.setValueAt(String.format("%02d:00 - %02d:00", h, h+1), index++, 0);
        }

        horarioTable = new JTable(tableModel);
        horarioTable.setRowHeight(60);
        horarioTable.setDefaultRenderer(Object.class, new ColorRenderer(coloresClases));

        cursosComboBox = new JComboBox<>();
        cursosComboBox.addItem("Selecciona un curso...");
        cursosComboBox.addActionListener(e -> cargarHorario());

        docentesComboBox = new JComboBox<>();
        docentesComboBox.addItem("Selecciona un docente...");
        docentesComboBox.addActionListener(e -> cargarHorario());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Curso: "));
        topPanel.add(cursosComboBox);
        topPanel.add(new JLabel("Docente: "));
        topPanel.add(docentesComboBox);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(horarioTable), BorderLayout.CENTER);

        setSize(1100, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void cargarAsignaturas() {
        try {
            Map<String, Map<String, Object>> data = firebase.listAll("Asignaturas");
            for (var entry : data.entrySet()) {
                Map<String, Object> a = entry.getValue();
                String codigo = (String) a.get("codigo");
                String nombre = (String) a.get("nombre");
                if (codigo != null && nombre != null) mapaAsignaturas.put(codigo, nombre);
            }
        } catch (Exception ignored) {}
    }

    private void cargarCursos() {
        try {
            Map<String, Map<String, Object>> data = firebase.listAll("Cursos");
            cursosComboBox.removeAllItems();
            cursosComboBox.addItem("Selecciona un curso...");
            for (var entry : data.entrySet()) {
                Map<String, Object> curso = entry.getValue();
                String nombre = (String) curso.get("nombre");
                if (nombre != null) cursosComboBox.addItem(nombre);
            }
        } catch (Exception ignored) {}
    }

    private void cargarDocentes() {
        try {
            Map<String, Map<String, Object>> data = firebase.listAll("Docentes");
            docentesComboBox.removeAllItems();
            docentesComboBox.addItem("Selecciona un docente...");
            for (var entry : data.entrySet()) {
                Map<String, Object> d = entry.getValue();
                String nombre = (String) d.get("nombre");
                if (nombre != null) {
                    docentesComboBox.addItem(nombre);
                    mapaDocentesID.put(nombre, entry.getKey());
                }
            }
        } catch (Exception ignored) {}
    }

    private void cargarHorario() {
        limpiarTabla();
        coloresClases.clear();

        String cursoSel = (String) cursosComboBox.getSelectedItem();
        String docenteSel = (String) docentesComboBox.getSelectedItem();

        boolean filtrarDocente = docenteSel != null && !docenteSel.equals("Selecciona un docente...");
        boolean filtrarCurso = cursoSel != null && !cursoSel.equals("Selecciona un curso...");

        try {
            Map<String, Map<String, Object>> data = firebase.listAll("Clases");

            for (var entry : data.entrySet()) {
                Clase c = new Gson().fromJson(new Gson().toJson(entry.getValue()), Clase.class);

                if (c == null) continue;

                if (filtrarDocente) {
                    String idDocente = mapaDocentesID.get(docenteSel);
                    if (idDocente == null || !idDocente.equals(c.getDocenteId())) continue;
                } else if (filtrarCurso) {
                    if (c.getCursos() == null || !c.getCursos().contains(cursoSel)) continue;
                } else {
                    continue;
                }

                c.setNombreAsignatura(mapaAsignaturas.get(c.getAsignaturaCodigo()));
                proyectarClase(c);
            }

        } catch (Exception ignored) {}
    }

    private void proyectarClase(Clase clase) {
        if (!coloresClases.containsKey(clase.getCodigo()))
            coloresClases.put(clase.getCodigo(), Color.getHSBColor((float)Math.random(), 0.4f, 1f));

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Horario::new);
    }
}
