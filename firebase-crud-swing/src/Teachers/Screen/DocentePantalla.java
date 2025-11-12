package Teachers.Screen; // Paquete para Docentes

import Admin.UserManagement.FirebaseClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class DocentePantalla extends JFrame { // Clase DocentePantalla

    // Etiquetas para las tarjetas superiores
    private JLabel lblEstudiantesAlerta;
    private JLabel lblCalificacionesPendientes;
    private final FirebaseClient fb;

    public DocentePantalla(String nombre) throws IOException {
        this.fb = new FirebaseClient("conexion.txt");

        setTitle("Panel del Profesor/a");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240, 250, 240));

        // Color principal del diseño (Azul/Cian fuerte)
        Color colorPrincipal = new Color(0, 150, 175);
        Color colorBarraSuperior = new Color(0, 100, 125);
        Color colorRollover = new Color(20, 170, 195);

        // ====== BARRA SUPERIOR ======
        JPanel barraSuperior = new JPanel();
        barraSuperior.setBackground(colorBarraSuperior);
        barraSuperior.setBounds(0, 0, getWidth(), 90);
        barraSuperior.setLayout(null);
        add(barraSuperior);

        JLabel lblTitulo = new JLabel("¡Hola, " + nombre + "!");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 15, 400, 40);
        barraSuperior.add(lblTitulo);

        JLabel lblSub = new JLabel("Profesor/a");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblSub.setForeground(Color.WHITE);
        lblSub.setBounds(30, 50, 300, 30);
        barraSuperior.add(lblSub);

        // Botón de Seleccionar Clase
        JButton btnSeleccionar = new JButton("Seleccionar Clase");
        btnSeleccionar.setBounds(30, 120, 180, 40);
        btnSeleccionar.setBackground(colorPrincipal);
        btnSeleccionar.setForeground(Color.WHITE);
        btnSeleccionar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSeleccionar.addActionListener(e -> accionBoton("Seleccionar Clase"));
        add(btnSeleccionar);

        int yCard = 120;
        int xCard1 = 360;
        int xCard2 = 700;

        // ====== TARJETAS SUPERIORES ======

        // Tarjeta 1: Estudiantes en Alerta
        JPanel panelAlerta = crearPanelInfo("\uD83D\uDD14", "Estudiantes en Alerta", colorPrincipal);
        panelAlerta.setBounds(xCard1, yCard, 300, 110);
        lblEstudiantesAlerta = crearLabelContador("5", Color.WHITE);
        panelAlerta.add(lblEstudiantesAlerta, BorderLayout.CENTER);
        add(panelAlerta);

        // Tarjeta 2: Calificaciones Pendientes
        JPanel panelCalificaciones = crearPanelInfo("\u2B50", "Calificaciones Pendientes", colorPrincipal);
        panelCalificaciones.setBounds(xCard2, yCard, 300, 110);
        lblCalificacionesPendientes = crearLabelContador("1", Color.WHITE);
        panelCalificaciones.add(lblCalificacionesPendientes, BorderLayout.CENTER);
        add(panelCalificaciones);

        // ====== TÍTULOS DE SECCIONES ======
        JLabel lblIzquierda = new JLabel("Seguimiento de calificaciones");
        lblIzquierda.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIzquierda.setBounds(100, 260, 300, 30);
        add(lblIzquierda);

        JLabel lblDerecha = new JLabel("Faltas académicas y Administración de Clases \u24D8");
        lblDerecha.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDerecha.setBounds(710, 260, 450, 30);
        add(lblDerecha);

        // ====== BOTONES ======
        int x = 100, y = 310, w = 250, h = 120, esp = 40;
        int x2 = 710;

        // Fila 1 - Columna Izquierda (Seguimiento de calificaciones)
        add(crearBotonConColor("📅", "Calendario Académico", "Eventos y Fechas", x, y, colorPrincipal, colorRollover, () -> accionBoton("Calendario Académico")));
        add(crearBotonConColor("📘", "Administrar Tareas", "5 Tareas creadas", x + w + esp, y, colorPrincipal, colorRollover, () -> accionBoton("Administrar Tareas")));
        add(crearBotonConColor("⭐", "Actualizar Notas", "5 materias con notas pendientes", x, y + h + esp, colorPrincipal, colorRollover, () -> accionBoton("Actualizar Notas")));
        add(crearBotonConColor("🧑‍🏫", "Contactar Acudientes y Directivos", "Comunicación Directa", x + w + esp, y + h + esp, colorPrincipal, colorRollover, () -> accionBoton("Contactar Acudientes y Directivos")));

        // Fila 1 - Columna Derecha (Faltas y Administración)
        add(crearBotonConColor("⚠️", "Tomar Asistencia de Estudiantes", "", x2, y, colorPrincipal, colorRollover, () -> accionBoton("Tomar Asistencia")));
        add(crearBotonConColor("📝", "Hacer Anotacion a Estudiante", "", x2 + w + esp, y, colorPrincipal, colorRollover, () -> accionBoton("Hacer Anotación")));

        // Fila 2 - Columna Derecha (Botón largo)
        JButton botonLargo = crearBotonConColor("🛠️", "Ver clases asignadas y administrarlas", "Cambiar clases, horarios, estudiantes y notas", x2, y + h + esp, colorPrincipal, colorRollover, () -> accionBoton("Administrar Clases"));
        botonLargo.setBounds(x2, y + h + esp, 2 * w + esp, h); // Se duplica el ancho y el espacio
        add(botonLargo);

        // No se llama cargarTotalesFirebase ya que los datos de la imagen son estáticos.
    }

    // Método modificado para usar el color principal
    private JPanel crearPanelInfo(String emoji, String texto, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        JLabel lblTexto = new JLabel(texto, SwingConstants.CENTER);
        lblTexto.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTexto.setForeground(Color.WHITE);
        panel.add(lblTexto, BorderLayout.NORTH);

        return panel;
    }

    // Método modificado para el contador
    private JLabel crearLabelContador(String texto, Color textColor) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        lbl.setForeground(textColor);
        return lbl;
    }

    // Método para crear botón con colores específicos
    private JButton crearBotonConColor(String emoji, String titulo, String subtitulo, int x, int y, Color baseColor, Color rolloverColor, Runnable accion) {
        JButton boton = new JButton("<html><center><span style='font-size:24px'>" + emoji +
                "</span><br><b>" + titulo + "</b><br><span style='font-size:13px'>" + subtitulo + "</span></center></html>") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? rolloverColor : baseColor);
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
        JOptionPane.showMessageDialog(this, "Presionaste: " + nombreBoton);
    }

    // Este método se mantiene pero no se llama en el constructor, ya que los valores son fijos.
    private void cargarTotalesFirebase() {
        try {
            Map<String, Object> tareas = fb.listAll("Tareas");
            Map<String, Object> materias = fb.listAll("Materias");
            lblEstudiantesAlerta.setText(String.valueOf(tareas.size()));
            lblCalificacionesPendientes.setText(String.valueOf(materias.size()));
        } catch (Exception e) {
            lblEstudiantesAlerta.setText("Err");
            lblCalificacionesPendientes.setText("Err");
        }
    }

    // Método principal para ejecutar la aplicación de escritorio
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DocentePantalla frame = new DocentePantalla("prueba");
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