package Admin.Calendar;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Horario extends JFrame {

    private JComboBox<String> cursosComboBox;
    private DefaultTableModel tableModel;
    private JTable horarioTable;
    private DatabaseReference databaseReference;

    private static final Map<String, Integer> DIA_TO_COL = new HashMap<>();
    private static final Map<String, Integer> HORA_TO_ROW = new HashMap<>();

    public Horario() {
        super("Horario de Clases");

        // Mapeo de días a columnas
        DIA_TO_COL.put("Lunes", 1);
        DIA_TO_COL.put("Martes", 2);
        DIA_TO_COL.put("Miércoles", 3);
        DIA_TO_COL.put("Jueves", 4);
        DIA_TO_COL.put("Viernes", 5);

        // Mapeo de horas a filas (07:00 → fila 0)
        for (int h = 7; h <= 16; h++) {
            HORA_TO_ROW.put(String.format("%02d:00", h), h - 7);
        }

        if (!initializeFirebase()) {
            JOptionPane.showMessageDialog(this, "Error al inicializar Firebase. Revisa la ruta del key.json", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        configurarUI();
        cargarCursosDesdeFirebase();
    }

    private boolean initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(
                        "C:/Users/Juan Guio/IdeaProjects/firebase-crud-swing/key.json"
                );
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://schoolappsnet-poo-default-rtdb.firebaseio.com/")
                        .build();
                FirebaseApp.initializeApp(options);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void configurarUI() {
        String[] headers = {"Hora", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};
        tableModel = new DefaultTableModel(headers, 10) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Rellenar la columna de horas
        for (int h = 7; h <= 16; h++) {
            tableModel.setValueAt(String.format("%02d:00H", h), h - 7, 0);
        }

        horarioTable = new JTable(tableModel);
        horarioTable.setRowHeight(60);
        horarioTable.setFont(new Font("Arial", Font.PLAIN, 14));
        horarioTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        cursosComboBox = new JComboBox<>();
        cursosComboBox.addItem("Selecciona un curso...");
        cursosComboBox.addActionListener(e -> cargarHorarioParaCursoSeleccionado());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Curso: "));
        topPanel.add(cursosComboBox);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(horarioTable), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void cargarCursosDesdeFirebase() {
        DatabaseReference cursosRef = databaseReference.child("Cursos");

        cursosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> listaCursos = new ArrayList<>();

                for (DataSnapshot cursoSnapshot : snapshot.getChildren()) {
                    String nombre = cursoSnapshot.child("nombre").getValue(String.class);
                    if (nombre != null && !nombre.isEmpty()) {
                        listaCursos.add(nombre);
                    }
                }

                Collections.sort(listaCursos);

                SwingUtilities.invokeLater(() -> {
                    cursosComboBox.removeAllItems();
                    cursosComboBox.addItem("Selecciona un curso...");
                    for (String curso : listaCursos) {
                        cursosComboBox.addItem(curso);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error al cargar cursos: " + error.getMessage());
            }
        });
    }

    private void cargarHorarioParaCursoSeleccionado() {
        String cursoSeleccionado = (String) cursosComboBox.getSelectedItem();
        limpiarHorarioTabla();

        if (cursoSeleccionado == null || cursoSeleccionado.equals("Selecciona un curso...")) return;

        DatabaseReference clasesRef = databaseReference.child("Clases");

        clasesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot claseSnapshot : snapshot.getChildren()) {
                    Clase clase = claseSnapshot.getValue(Clase.class);

                    if (clase == null || clase.getCursos() == null) continue;

                    if (clase.getCursos().contains(cursoSeleccionado)) {
                        proyectarClaseEnTabla(clase);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error al cargar horario: " + error.getMessage());
            }
        });
    }

    private void proyectarClaseEnTabla(Clase clase) {
        String infoClase = String.format(
                "<html><b>%s</b><br>%s<br><i>%s</i></html>",
                clase.getAsignaturaCodigo(),
                clase.getDocenteNombre(),
                clase.getSalon()
        );

        List<HorarioClase> horarios = clase.getHorarios();
        if (horarios == null) return;

        for (HorarioClase entry : horarios) {
            String dia = entry.getDia();
            String horaInicio = entry.getHoraInicio();
            String horaFin = entry.getHoraFin();

            if (dia == null || horaInicio == null || horaFin == null) continue;
            if (!DIA_TO_COL.containsKey(dia)) continue;
            if (!HORA_TO_ROW.containsKey(horaInicio)) continue;

            int col = DIA_TO_COL.get(dia);
            int startRow = HORA_TO_ROW.get(horaInicio);
            int startHour = Integer.parseInt(horaInicio.substring(0, 2));
            int endHour = Integer.parseInt(horaFin.substring(0, 2));
            int duration = Math.max(1, endHour - startHour);

            for (int i = 0; i < duration; i++) {
                int row = startRow + i;
                if (row < tableModel.getRowCount()) {
                    tableModel.setValueAt(infoClase, row, col);
                }
            }
        }
    }

    private void limpiarHorarioTabla() {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 1; col < tableModel.getColumnCount(); col++) {
                tableModel.setValueAt(null, row, col);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Horario::new);
    }
}

