package Admin.UserManagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CrudFrame extends JFrame {
    private final FirebaseClient fb;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","Código","Documento","Nombre","Correo","Edad","Rol"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable table = new JTable(model);

    private final JTextField txtId = new JTextField();
    private final JTextField txtDocumento = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JSpinner spEdad = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));
    private final JComboBox<String> cbRol = new JComboBox<>(new String[]{
            "Estudiante","Acudiente","Docente","Administrador"
    });

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnStats = new JButton("Estadísticas");

    public CrudFrame(FirebaseClient fb) {
        super("Administrador/Editar Usuarios");
        this.fb = fb;

        // ** CAMBIO CLAVE AQUÍ **
        // Usamos DISPOSE_ON_CLOSE para que solo se cierre esta ventana
        // y se liberen sus recursos, manteniendo abierto el programa principal.
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(1000, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        wireEvents();
        cargarTabla();
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        c.gridx=0; c.gridy=row; p.add(new JLabel("ID"), c);
        c.gridx=1; c.gridy=row; txtId.setEditable(false); p.add(txtId, c);

        row++;
        c.gridx=0; c.gridy=row; p.add(new JLabel("Documento"), c);
        c.gridx=1; c.gridy=row; p.add(txtDocumento, c);

        row++;
        c.gridx=0; c.gridy=row; p.add(new JLabel("Nombre"), c);
        c.gridx=1; c.gridy=row; p.add(txtNombre, c);

        row++;
        c.gridx=0; c.gridy=row; p.add(new JLabel("Correo"), c);
        c.gridx=1; c.gridy=row; p.add(txtCorreo, c);

        row++;
        c.gridx=0; c.gridy=row; p.add(new JLabel("Edad"), c);
        c.gridx=1; c.gridy=row; p.add(spEdad, c);

        row++;
        c.gridx=0; c.gridy=row; p.add(new JLabel("Rol"), c);
        c.gridx=1; c.gridy=row; p.add(cbRol, c);

        return p;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(btnNuevo);
        p.add(btnGuardar);
        p.add(btnActualizar);
        p.add(btnEliminar);
        p.add(btnRefrescar);
        p.add(btnStats);
        return p;
    }

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                txtId.setText(String.valueOf(model.getValueAt(r,0)));
                txtDocumento.setText(String.valueOf(model.getValueAt(r,2)));
                txtNombre.setText(String.valueOf(model.getValueAt(r,3)));
                txtCorreo.setText(String.valueOf(model.getValueAt(r,4)));
                spEdad.setValue(Integer.parseInt(String.valueOf(model.getValueAt(r,5))));
                cbRol.setSelectedItem(String.valueOf(model.getValueAt(r,6)));
            }
        });

        btnNuevo.addActionListener(e -> limpiarForm());

        btnGuardar.addActionListener(e -> {
            try {
                if (!validarForm()) return;
                Persona p = new Persona(
                        txtNombre.getText().trim(),
                        txtCorreo.getText().trim(),
                        (int) spEdad.getValue(),
                        txtDocumento.getText().trim(),
                        String.valueOf(cbRol.getSelectedItem()),
                        generarCodigoUnico() // <<--- NUEVO
                );
                String rol = p.rol.toLowerCase();

                String nodo = switch (rol) {
                    case "administrador" -> "Administradores";
                    case "estudiante" -> "Estudiantes";
                    case "docente" -> "Docentes";
                    case "acudiente" -> "Acudientes";
                    default -> "SinRol";
                };

                String id = fb.createInNode(nodo, p);
                JOptionPane.showMessageDialog(this,
                        "Usuario creado.\nID: " + id + "\nCódigo de acceso: " + p.codigoAcceso);
                cargarTabla();
                seleccionarPorId(id);
            } catch (Exception ex) {
                showError("Error al guardar", ex);
            }
        });

        btnActualizar.addActionListener(e -> {
            String id = txtId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Selecciona un registro primero.");
                return;
            }
            try {
                if (!validarForm()) return;

                // Recuperar el actual para no perder su código
                String codigoActual = fb.listAll().get(id).codigoAcceso;

                Persona p = new Persona(
                        txtNombre.getText().trim(),
                        txtCorreo.getText().trim(),
                        (int) spEdad.getValue(),
                        txtDocumento.getText().trim(),
                        String.valueOf(cbRol.getSelectedItem()),
                        codigoActual // <<--- Mantener el mismo código
                );

                String rol = p.rol.toLowerCase();
                String nodo = switch (rol) {
                    case "administrador" -> "Administradores";
                    case "estudiante" -> "Estudiantes";
                    case "docente" -> "Docentes";
                    case "acudiente" -> "Acudientes";
                    default -> "SinRol";
                };

                fb.updateInNode(nodo, id, p);
                JOptionPane.showMessageDialog(this, "Actualizado: " + id);
                cargarTabla();
                seleccionarPorId(id);
            } catch (Exception ex) {
                showError("Error al actualizar", ex);
            }
        });


        btnEliminar.addActionListener(e -> {
            String id = txtId.getText().trim();
            String rol = cbRol.getSelectedItem().toString();

            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Selecciona un registro primero.");
                return;
            }

            int op = JOptionPane.showConfirmDialog(this, "¿Eliminar " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                try {
                    String nodo = nodoPorRol(rol);
                    fb.deleteInNode(nodo, id);
                    JOptionPane.showMessageDialog(this, "Eliminado correctamente");
                    cargarTabla();
                } catch (Exception s) {
                    JOptionPane.showMessageDialog(this, "Error: " + s.getMessage());
                }
            }
        });

        btnRefrescar.addActionListener(e -> cargarTabla());
        btnStats.addActionListener(e -> mostrarEstadisticas());
    }

    private void cargarTabla() {
        try {
            Map<String, Persona> map = fb.listAll();
            List<Persona> list = map.values().stream()
                    .sorted(Comparator.comparing(p -> p.nombre == null ? "" : p.nombre.toLowerCase()))
                    .collect(Collectors.toList());
            model.setRowCount(0);
            for (Persona p : list) {
                model.addRow(new Object[]{
                        p.id,
                        nvl(p.codigoAcceso),  // <<--- NUEVA COLUMNA
                        nvl(p.documento),
                        nvl(p.nombre),
                        nvl(p.correo),
                        p.edad,
                        nvl(p.rol)
                });
            }
        } catch (Exception ex) {
            showError("No se pudo cargar la tabla", ex);
        }
    }

    private boolean validarForm() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.");
            return false;
        }
        if (txtDocumento.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El documento es obligatorio.");
            return false;
        }
        return true;
    }

    private void limpiarForm() {
        txtId.setText("");
        txtDocumento.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        spEdad.setValue(18);
        cbRol.setSelectedIndex(0);
        table.clearSelection();
    }

    private void seleccionarPorId(String id) {
        for (int r = 0; r < model.getRowCount(); r++) {
            if (Objects.equals(model.getValueAt(r,0), id)) {
                table.setRowSelectionInterval(r, r);
                table.scrollRectToVisible(table.getCellRect(r, 0, true));
                return;
            }
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void showError(String title, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, title + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarEstadisticas() {
        try {
            Map<String, Persona> map = fb.listAll();
            Collection<Persona> data = map.values();

            int total = data.size();
            double promedioEdad = data.stream().mapToInt(p -> p.edad).average().orElse(0);
            int minEdad = data.stream().mapToInt(p -> p.edad).min().orElse(0);
            int maxEdad = data.stream().mapToInt(p -> p.edad).max().orElse(0);

            Map<String, Long> porRol = new TreeMap<>();
            data.stream()
                    .map(p -> (p.rol == null || p.rol.isBlank()) ? "(sin rol)" : p.rol.toLowerCase())
                    .forEach(r -> porRol.put(r, porRol.getOrDefault(r, 0L) + 1));

            StringBuilder roles = new StringBuilder();
            porRol.forEach((r, n) -> roles.append(" - ").append(r).append(": ").append(n).append("\n"));

            String msg = String.format(
                    "Estadísticas\n------------\nTotal registros: %d\nEdad promedio: %.2f\nEdad mínima: %d\nEdad máxima: %d\n\nConteo por rol:\n%s",
                    total, promedioEdad, minEdad, maxEdad, roles.toString()
            );

            JOptionPane.showMessageDialog(this, msg, "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("No se pudieron calcular las estadísticas", ex);
        }
    }
    private String generarCodigoUnico() throws Exception {
        Map<String, Persona> map = fb.listAll();
        Random r = new Random();
        String code;

        boolean existe;
        do {
            code = String.format("%04d", r.nextInt(10000)); // Código de 4 dígitos

            existe = false;
            for (Persona p : map.values()) {
                if (p.codigoAcceso != null && p.codigoAcceso.equals(code)) {
                    existe = true;
                    break;
                }
            }
        } while (existe);

        return code;
    }
    private String nodoPorRol(String rol) {
        return switch (rol.toLowerCase()) {
            case "administrador" -> "Administradores";
            case "docente" -> "Docentes";
            case "estudiante" -> "Estudiantes";
            case "acudiente" -> "Acudientes";
            default -> "Usuarios"; // Por si acaso
        };
    }
}