package Parents.Screen;

import Admin.UserManagement.FirebaseClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AcudientePantalla extends JFrame {

    private JLabel lblTareasPendientes;
    private JLabel lblPromedioEstudiante;
    private final FirebaseClient fb;
    private String acudienteId;
    private JButton btnSeleccionar;
    private String estudianteSeleccionado;

    public AcudientePantalla(String nombre, String acudienteId) throws IOException {
        this.fb = new FirebaseClient("conexion.txt");
        this.acudienteId = acudienteId;

        setTitle("Panel del Acudiente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 250, 240));

        JPanel barraSuperior = new JPanel();
        barraSuperior.setBackground(new Color(204, 153, 51));
        barraSuperior.setBounds(0, 0, getWidth(), 90);
        barraSuperior.setLayout(null);
        add(barraSuperior);

        JLabel lblTitulo = new JLabel("¡Hola, " + nombre + "!");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 15, 400, 40);
        barraSuperior.add(lblTitulo);

        JLabel lblSub = new JLabel("Acudiente");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblSub.setForeground(Color.WHITE);
        lblSub.setBounds(30, 50, 300, 30);
        barraSuperior.add(lblSub);

        btnSeleccionar = new JButton("Seleccionar Hijo");
        btnSeleccionar.setBounds(30, 120, 180, 40);
        btnSeleccionar.setBackground(new Color(204, 153, 51));
        btnSeleccionar.setForeground(Color.BLACK);
        btnSeleccionar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSeleccionar.addActionListener(e -> seleccionarEstudiante());
        add(btnSeleccionar);


        int yCard = 120;
        int xCard1 = 360;
        int xCard2 = 700;

        JPanel panelTareas = crearPanelInfo("🔔", "Tareas Pendiente", new Color(204, 153, 51));
        panelTareas.setBounds(xCard1, yCard, 300, 110);
        lblTareasPendientes = crearLabelContador("—", Color.WHITE);
        panelTareas.add(lblTareasPendientes, BorderLayout.CENTER);
        add(panelTareas);

        JPanel panelPromedio = crearPanelInfo("⭐", "Promedio del Estudiante", new Color(204, 153, 51));
        panelPromedio.setBounds(xCard2, yCard, 300, 110);
        lblPromedioEstudiante = crearLabelContador("—", Color.WHITE);
        panelPromedio.add(lblPromedioEstudiante, BorderLayout.CENTER);
        add(panelPromedio);

        JLabel lblIzquierda = new JLabel("Seguimiento Académico");
        lblIzquierda.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIzquierda.setBounds(100, 260, 300, 30);
        add(lblIzquierda);

        JLabel lblDerecha = new JLabel("Configuración Académica ⓘ");
        lblDerecha.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDerecha.setBounds(710, 260, 300, 30);
        add(lblDerecha);

        Color botonColor = new Color(204, 153, 51);
        Color botonRolloverColor = new Color(214, 163, 61);

        int x = 100, y = 310, w = 250, h = 120, esp = 40;
        int x2 = 710;

        add(crearBoton("📅", "Calendario Académico", "Eventos y Fechas", x, y, botonColor, botonRolloverColor, () -> accionBoton("Calendario Académico")));
        add(crearBoton("📘", "Consultar Tareas", "1 Tarea Pendiente", x + w + esp, y, botonColor, botonRolloverColor, () -> accionBoton("Consultar Tareas")));
        add(crearBoton("⭐", "Ver Notas Generales", "Notas de Materias", x, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Ver Notas Generales")));
        add(crearBoton("🧑‍🏫", "Ver Asistencias", "Asistencias del Estudiante", x + w + esp, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Contactar Profesores")));

        add(crearBoton("⚠️", "Historial de Reportes", "", x2, y, botonColor, botonRolloverColor, () -> accionBoton("Historial de Reportes")));
        add(crearBoton("✍️", "Crear Reclamo", "", x2 + w + esp, y, botonColor, botonRolloverColor, () -> accionBoton("Crear Reclamo")));

        JButton botonLargo = crearBoton("📞", "Cambiar Información de Contacto", "Actualizar Teléfono o Email", x2, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Cambiar Información de Contacto"));
        botonLargo.setBounds(x2, y + h + esp, 2 * w + esp, h);
        add(botonLargo);
    }

    private JPanel crearPanelInfo(String emoji, String texto, Color bg) {
        JPanel panel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }

    private JLabel crearLabelContador(String txt, Color c) {
        JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        lbl.setForeground(c);
        return lbl;
    }

    private JButton crearBoton(String emoji, String titulo, String subtitulo, int x, int y, Color base, Color hover, Runnable accion) {
        JButton btn = new JButton("<html><center><span style='font-size:24px'>" + emoji +
                "</span><br><b>" + titulo + "</b><br><span style='font-size:13px'>" + subtitulo + "</span></center></html>") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(getModel().isRollover() ? hover : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };
        btn.setBounds(x, y, 250, 120);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    private void seleccionarEstudiante() {
        try {
            Map<String, Object> acudienteData = fb.get("Usuarios/Acudientes/" + acudienteId);
            if (acudienteData == null || acudienteData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontró información del acudiente.");
                return;
            }

            Object hijosObj = acudienteData.get("hijos");
            if (!(hijosObj instanceof List<?> hijosList)) {
                JOptionPane.showMessageDialog(this, "Este acudiente no tiene hijos registrados.");
                return;
            }

            List<String> idsHijos = hijosList.stream().map(Object::toString).collect(Collectors.toList());
            if (idsHijos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay hijos asociados.");
                return;
            }

            Map<String, String> mapaNombres = idsHijos.stream().collect(Collectors.toMap(
                    id -> id,
                    id -> {
                        try {
                            Map<String, Object> est = fb.get("Usuarios/Estudiantes/" + id);
                            return est.getOrDefault("nombre", "Sin nombre").toString();
                        } catch (Exception e) {
                            return "Error";
                        }
                    }
            ));

            String[] nombres = mapaNombres.values().toArray(new String[0]);
            String elegido = (String) JOptionPane.showInputDialog(
                    this, "Selecciona un estudiante:", "Hijos",
                    JOptionPane.PLAIN_MESSAGE, null, nombres, null
            );

            if (elegido != null) {
                String idEncontrado = mapaNombres.entrySet().stream()
                        .filter(e -> e.getValue().equals(elegido))
                        .findFirst().get().getKey();

                estudianteSeleccionado = idEncontrado;
                btnSeleccionar.setText(elegido); // ← CAMBIA EL TEXTO DEL BOTÓN
                actualizarDatosDelEstudiante(idEncontrado);
            }


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando hijos: " + e.getMessage());
        }
    }

    private void actualizarDatosDelEstudiante(String id) {
        try {
            Map<String, Object> info = fb.get("Usuarios/Estudiantes/" + id);
            if (info == null || info.isEmpty()) {
                lblTareasPendientes.setText("—");
                lblPromedioEstudiante.setText("—");
                return;
            }

            lblTareasPendientes.setText(info.getOrDefault("tareasPendientes", "0").toString());
            lblPromedioEstudiante.setText(info.getOrDefault("promedio", "—").toString());

        } catch (Exception e) {
            lblTareasPendientes.setText("—");
            lblPromedioEstudiante.setText("—");
        }
    }

    private void accionBoton(String nombre) {
        JOptionPane.showMessageDialog(this, "Presionaste: " + nombre);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AcudientePantalla f = new AcudientePantalla("Acudiente", "ID_ACUDIENTE_AQUI");
                f.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
