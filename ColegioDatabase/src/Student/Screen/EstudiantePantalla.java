package Student.Screen;

import Admin.UserManagement.FirebaseClient;
import Student.Calendar.HorarioEstudiante;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class EstudiantePantalla extends JFrame {

    private JLabel lblTareasPendientes;
    private JLabel lblMateriasRegistradas;
    private String nombre;
    private final FirebaseClient fb;

    public EstudiantePantalla(String nombre) throws IOException {
        this.fb = new FirebaseClient("conexion.txt");
        this.nombre = nombre;
        setTitle("Panel del Estudiante");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 250, 240));

        // ====== BARRA SUPERIOR ======
        JPanel barraSuperior = new JPanel();
        barraSuperior.setBackground(new Color(60, 128, 80));
        barraSuperior.setBounds(0, 0, getWidth(), 90);
        barraSuperior.setLayout(null);
        add(barraSuperior);

        JLabel lblTitulo = new JLabel("¡Hola, " + nombre + "!");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 15, 400, 40);
        barraSuperior.add(lblTitulo);

        JLabel lblSub = new JLabel("Panel del Estudiante");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblSub.setForeground(Color.WHITE);
        lblSub.setBounds(30, 50, 300, 30);
        barraSuperior.add(lblSub);

        // ====== TARJETAS SUPERIORES ======
        JPanel panelTareas = crearPanelInfo("\uD83D\uDCDD", "Tareas Pendientes");
        panelTareas.setBounds(360, 120, 300, 110);
        lblTareasPendientes = crearLabelContador("...");
        panelTareas.add(lblTareasPendientes, BorderLayout.SOUTH);
        add(panelTareas);

        JPanel panelMaterias = crearPanelInfo("\uD83D\uDCD6", "Materias Registradas");
        panelMaterias.setBounds(700, 120, 300, 110);
        lblMateriasRegistradas = crearLabelContador("...");
        panelMaterias.add(lblMateriasRegistradas, BorderLayout.SOUTH);
        add(panelMaterias);

        // ====== TÍTULOS DE SECCIONES ======
        JLabel lblIzquierda = new JLabel("Gestión Académica 📚");
        lblIzquierda.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIzquierda.setBounds(150, 260, 300, 30);
        add(lblIzquierda);

        JLabel lblDerecha = new JLabel("Comunicación y Soporte 💬");
        lblDerecha.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDerecha.setBounds(780, 260, 300, 30);
        add(lblDerecha);

        // ====== BOTONES ======
        int x = 100, y = 310, w = 250, h = 120, esp = 40;
        int x2 = 710;

        add(crearBoton("📅", "Calendario Académico", "Fechas importantes", x, y, () -> accionBoton("Calendario Académico")));
        add(crearBoton("📘", "Tareas", "Ver y entregar tareas", x + w + esp, y, () -> accionBoton("Tareas")));
        add(crearBoton("📊", "Ver mis Notas y Asistencia", "Consultar calificaciones y asistencia", x, y + h + esp, () -> accionBoton("Ver mis Notas")));
        add(crearBoton("👨‍🏫", "Contactar Director Docente", "Enviar mensaje", x + w + esp, y + h + esp, () -> accionBoton("Contactar Director Docente")));

        add(crearBoton("💬", "Pedir Asesoría Académica", "Hablar con un docente", x2, y, () -> accionBoton("Pedir Asesoría")));
        add(crearBoton("⚠️", "Enviar Reporte Convivencial", "Reportar incidentes", x2 + w + esp, y, () -> accionBoton("Reporte Convivencial")));
        add(crearBoton("🛠️", "Contactar Soporte Técnico", "Problemas del sistema", x2, y + h + esp, () -> accionBoton("Soporte Técnico")));

        cargarTotalesFirebase();
    }

    private JPanel crearPanelInfo(String emoji, String texto) {
        JPanel panel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(70, 150, 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        JLabel lblTexto = new JLabel(texto, SwingConstants.CENTER);
        lblTexto.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTexto.setForeground(Color.WHITE);
        panel.add(lblTexto, BorderLayout.NORTH);

        JLabel lblEmoji = new JLabel(emoji, SwingConstants.CENTER);
        lblEmoji.setFont(new Font("Arial", Font.PLAIN, 40));
        lblEmoji.setForeground(Color.WHITE);
        panel.add(lblEmoji, BorderLayout.CENTER);
        return panel;
    }

    private JLabel crearLabelContador(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JButton crearBoton(String emoji, String titulo, String subtitulo, int x, int y, Runnable accion) {
        JButton boton = new JButton("<html><center><span style='font-size:24px'>" + emoji +
                "</span><br><b>" + titulo + "</b><br><span style='font-size:13px'>" + subtitulo + "</span></center></html>") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(90, 170, 110) : new Color(70, 150, 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        boton.setBounds(x, y, 250, 120);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBorderPainted(false);
        boton.addActionListener(e -> accion.run());
        return boton;
    }


    private void accionBoton(String nombreBoton) {

        if (nombreBoton.equals("Ver mis Notas")) {
            try {
                VistaNotasAsistencias vista = new VistaNotasAsistencias(this, this.fb, this.nombre);
                vista.setVisible(true); // Esto abre la nueva ventana

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al abrir la vista de notas: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        } if (nombreBoton.equals("Calendario Académico")) {

            new HorarioEstudiante(this.nombre).setVisible(true);
            return;
        } else {
        JOptionPane.showMessageDialog(this, "Presionaste: " + nombreBoton);
        }
    }

    private void cargarTotalesFirebase() {
        try {
            Map<String, Object> tareas = fb.listAll("Tareas");
            Map<String, Object> materias = fb.listAll("Materias");
            lblTareasPendientes.setText(String.valueOf(tareas.size()));
            lblMateriasRegistradas.setText(String.valueOf(materias.size()));
        } catch (Exception e) {
            lblTareasPendientes.setText("Err");
            lblMateriasRegistradas.setText("Err");
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                EstudiantePantalla frame = new EstudiantePantalla("prueba");
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
}

