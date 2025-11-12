package Admin.Screen;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

import Admin.ClassManagement.FirebaseConexion;
import Admin.StudentManagement.AsignarCursosyPadresFrame;
import Admin.ClassManagement.CrearClaseFrame;
import Admin.UserManagement.CrudFrame;
import Admin.UserManagement.FirebaseClient;
import Admin.Calendar.Horario; // ✅ IMPORTANTE: importa la clase Horario

public class AdministradorPantalla extends JFrame {

    private JLabel lblUsuariosActivos;
    private JLabel lblCursosActivos;
    private String nombre;
    private final FirebaseClient fb;

    public AdministradorPantalla(String nombre) throws IOException {
        this.nombre = nombre;
        this.fb = new FirebaseClient("conexion.txt");

        setTitle("Administrador del Sistema");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 240, 247));

        // ====== BARRA SUPERIOR ======
        JPanel barraSuperior = new JPanel();
        barraSuperior.setBackground(new Color(107, 92, 123));
        barraSuperior.setBounds(0, 0, getWidth(), 90);
        barraSuperior.setLayout(null);
        add(barraSuperior);

        JLabel lblTitulo = new JLabel("¡Hola, " + nombre + "!");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 15, 400, 40);
        barraSuperior.add(lblTitulo);

        JLabel lblSub = new JLabel("Administrador del Sistema");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblSub.setForeground(Color.WHITE);
        lblSub.setBounds(30, 50, 300, 30);
        barraSuperior.add(lblSub);

        // ====== TARJETAS SUPERIORES ======
        JPanel panelUsuarios = crearPanelInfo("\uD83D\uDC64", "Total de Usuarios Activos");
        panelUsuarios.setBounds(360, 120, 300, 110);
        lblUsuariosActivos = crearLabelContador("...");
        panelUsuarios.add(lblUsuariosActivos, BorderLayout.SOUTH);
        add(panelUsuarios);

        JPanel panelCursos = crearPanelInfo("\uD83D\uDCD6", "Cursos Activos");
        panelCursos.setBounds(700, 120, 300, 110);
        lblCursosActivos = crearLabelContador("...");
        panelCursos.add(lblCursosActivos, BorderLayout.SOUTH);
        add(panelCursos);

        // ====== TÍTULOS DE SECCIONES ======
        JLabel lblUsuarios = new JLabel("Gestión de Usuarios ⚙");
        lblUsuarios.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblUsuarios.setBounds(150, 260, 300, 30);
        add(lblUsuarios);

        JLabel lblAcademica = new JLabel("Configuración Académica ⚙");
        lblAcademica.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAcademica.setBounds(780, 260, 300, 30);
        add(lblAcademica);

        // ====== BOTONES ======
        int x = 100, y = 310, w = 250, h = 120, esp = 40;

        add(crearBoton("👤", "Crear Usuarios", "Crear usuario y asignar roles", x, y, () -> accionBoton("Gestionar Usuarios")));
        add(crearBoton("👥", "Gestionar Usuarios", "Modificar atributos de los usuarios", x + w + esp, y, () -> accionBoton("Gestionar Roles")));
        add(crearBoton("📚", "Asignar Cursos y Acudientes", "Padres de familia y cursos", x, y + h + esp, () -> accionBoton("Gestionar Cursos")));
        add(crearBoton("📖", "Gestionar Clase/Asignatura", "Crear y Asignar Asignaturas y Clases", x + w + esp, y + h + esp, () -> accionBoton("Gestionar Asignaturas")));

        int x2 = 710;
        add(crearBoton("📅", "Calendario", "Materias y docentes", x2, y, () -> accionBoton("Calendario"))); // ✅ Cambiado a "Calendario"
        add(crearBoton("    ⚖\uFE0F", "Escalas de Calificación", "Cambiar notas/Porcentajes", x2 + w + esp, y, () -> accionBoton("Escalas de Calificación")));
        add(crearBoton("💬", "Publicar Circulares", "Avisos institucionales", x2, y + h + esp, () -> accionBoton("Publicar Circulares")));
        add(crearBoton("📊", "Generar Reportes", "Análisis y estadísticas", x2 + w + esp, y + h + esp, () -> accionBoton("Generar Reportes")));

        cargarTotalesFirebase();
    }

    private JPanel crearPanelInfo(String emoji, String texto) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setOpaque(false);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(124, 105, 144));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
                "</span><br><b>" + titulo + "</b><br><span style='font-size:13px'>" + subtitulo +
                "</span></center></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(143, 124, 160)
                        : new Color(124, 105, 144));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
                g2.dispose();
            }
        };

        boton.setBounds(x, y, 250, 120);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBorderPainted(false);

        boton.addActionListener(e -> {
            if (accion != null) accion.run();
        });

        return boton;
    }

    private void accionBoton(String nombreBoton) {
        switch (nombreBoton) {
            case "Gestionar Usuarios":
                SwingUtilities.invokeLater(() -> {
                    try {
                        CrudFrame ui = new CrudFrame(this.fb);
                        ui.setVisible(true);
                    } catch (Exception e) {
                        mostrarError("Error al cargar la pantalla de gestión de usuarios", e);
                    }
                });
                break;

            case "Gestionar Cursos":
                SwingUtilities.invokeLater(() -> {
                    try {
                        AsignarCursosyPadresFrame ui = new AsignarCursosyPadresFrame();
                        ui.setVisible(true);
                    } catch (Exception e) {
                        mostrarError("Error al cargar la pantalla de asignación de cursos y padres", e);
                    }
                });
                break;

            case "Gestionar Asignaturas":
                SwingUtilities.invokeLater(() -> {
                    try {
                        FirebaseConexion conexion = new FirebaseConexion("segundaconexion.txt");
                        CrearClaseFrame ui = new CrearClaseFrame(conexion);
                        ui.setVisible(true);
                    } catch (Exception e) {
                        mostrarError("Error al cargar la pantalla de gestión de asignaturas", e);
                    }
                });
                break;

            case "Calendario":
                SwingUtilities.invokeLater(() -> {
                    try {
                        Horario horarioFrame = new Horario();
                        horarioFrame.setVisible(true);
                    } catch (Exception e) {
                        mostrarError("Error al abrir el horario de clases", e);
                    }
                });
                break;

            default:
                JOptionPane.showMessageDialog(this, "Presionaste: " + nombreBoton);
        }
    }

    private void mostrarError(String mensaje, Exception e) {
        JOptionPane.showMessageDialog(this, mensaje + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    private void cargarTotalesFirebase() {
        try {
            Map<String, Object> usuarios = fb.listAll("Usuarios");
            Map<String, Object> cursos = fb.listAll("Cursos");
            lblUsuariosActivos.setText(String.valueOf(usuarios.size()));
            lblCursosActivos.setText(String.valueOf(cursos.size()));
        } catch (Exception e) {
            lblUsuariosActivos.setText("Err");
            lblCursosActivos.setText("Err");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AdministradorPantalla frame = new AdministradorPantalla("Maria");
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
