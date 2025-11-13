package Parents.Screen; // 1. CAMBIO DE PAQUETE

import Admin.UserManagement.FirebaseClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class AcudientePantalla extends JFrame { // 2. CAMBIO DE NOMBRE DE CLASE

    // 3. ACTUALIZACIÓN DE NOMBRES DE ETIQUETAS
    private JLabel lblTareasPendientes;
    private JLabel lblPromedioEstudiante;
    private String nombre;
    private final FirebaseClient fb;
    private String estudianteSeleccionado;

    public AcudientePantalla(String nombre) throws IOException {
        this.fb = new FirebaseClient("conexion.txt");

        setTitle("Panel del Acudiente"); // Título actualizado
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        // Mantenemos el color de fondo para consistencia visual
        getContentPane().setBackground(new Color(240, 250, 240));

        // ====== BARRA SUPERIOR ======
        JPanel barraSuperior = new JPanel();
        // Cambiamos el color de la barra superior para parecerse al de la imagen (marrón/mostaza)
        barraSuperior.setBackground(new Color(204, 153, 51));
        barraSuperior.setBounds(0, 0, getWidth(), 90);
        barraSuperior.setLayout(null);
        add(barraSuperior);

        // 4. TEXTOS DE BARRA SUPERIOR
        JLabel lblTitulo = new JLabel("¡Hola, " + nombre + "!");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(30, 15, 400, 40);
        barraSuperior.add(lblTitulo);

        JLabel lblSub = new JLabel("Acudiente"); // Subtítulo
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblSub.setForeground(Color.WHITE);
        lblSub.setBounds(30, 50, 300, 30);
        barraSuperior.add(lblSub);

        // Botón de Seleccionar Estudiante (elemento nuevo de la imagen)
        JButton btnSeleccionar = new JButton("Seleccionar Estudiante");
        btnSeleccionar.setBounds(30, 120, 180, 40);
        btnSeleccionar.setBackground(new Color(204, 153, 51));
        btnSeleccionar.setForeground(Color.BLACK);
        btnSeleccionar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSeleccionar.addActionListener(e -> seleccionarEstudiante());
        add(btnSeleccionar);

        // Ajustamos la ubicación de las tarjetas superiores para dejar espacio al botón
        int yCard = 120;
        int xCard1 = 360;
        int xCard2 = 700;


        // ====== TARJETAS SUPERIORES (5. TEXTOS ACTUALIZADOS) ======

        // Tarjeta 1: Tareas Pendientes
        JPanel panelTareas = crearPanelInfo("\uD83D\uDD14", "Tareas Pendiente", new Color(204, 153, 51));
        panelTareas.setBounds(xCard1, yCard, 300, 110);
        lblTareasPendientes = crearLabelContador("1", new Color(255, 255, 255)); // Texto estático "1"
        panelTareas.add(lblTareasPendientes, BorderLayout.CENTER);
        add(panelTareas);

        // Tarjeta 2: Promedio del Estudiante
        JPanel panelPromedio = crearPanelInfo("\u2B50", "Promedio del Estudiante", new Color(204, 153, 51));
        panelPromedio.setBounds(xCard2, yCard, 300, 110);
        lblPromedioEstudiante = crearLabelContador("5.0", new Color(255, 255, 255)); // Texto estático "5.0"
        panelPromedio.add(lblPromedioEstudiante, BorderLayout.CENTER);
        add(panelPromedio);

        // ====== TÍTULOS DE SECCIONES (6. TEXTOS ACTUALIZADOS) ======
        JLabel lblIzquierda = new JLabel("Seguimiento Académico");
        lblIzquierda.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIzquierda.setBounds(100, 260, 300, 30);
        add(lblIzquierda);

        JLabel lblDerecha = new JLabel("Configuración Académica \u24D8");
        lblDerecha.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDerecha.setBounds(710, 260, 300, 30);
        add(lblDerecha);

        // Colores para los botones (marrón de la imagen)
        Color botonColor = new Color(204, 153, 51);
        Color botonRolloverColor = new Color(214, 163, 61);

        // ====== BOTONES (7. TEXTOS Y POSICIONES ACTUALIZADAS) ======
        int x = 100, y = 310, w = 250, h = 120, esp = 40;
        int x2 = 710;

        // Fila 1 - Columna Izquierda (Seguimiento Académico)
        add(crearBotonConColor("📅", "Calendario Académico", "Eventos y Fechas", x, y, botonColor, botonRolloverColor, () -> accionBoton("Calendario Académico")));
        add(crearBotonConColor("📘", "Consultar Tareas", "1 Tarea Pendiente", x + w + esp, y, botonColor, botonRolloverColor, () -> accionBoton("Consultar Tareas")));
        add(crearBotonConColor("⭐", "Ver Notas Generales", "0 Materias Perdidas", x, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Ver Notas Generales")));
        add(crearBotonConColor("🧑‍🏫", "Contactar Profesores", "Comunicación Directa", x + w + esp, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Contactar Profesores")));

        // Fila 1 - Columna Derecha (Configuración Académica)
        add(crearBotonConColor("⚠️", "Historial de Reportes", "", x2, y, botonColor, botonRolloverColor, () -> accionBoton("Historial de Reportes")));
        add(crearBotonConColor("✍️", "Crear Reclamo", "", x2 + w + esp, y, botonColor, botonRolloverColor, () -> accionBoton("Crear Reclamo")));

        // Fila 2 - Columna Derecha (Botón largo - 540x120)
        JButton botonLargo = crearBotonConColor("📞", "Cambiar Información de Contacto", "Actualizar Teléfono o Email", x2, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Cambiar Información de Contacto"));
        botonLargo.setBounds(x2, y + h + esp, 2 * w + esp, h); // Se duplica el ancho y el espacio
        add(botonLargo);


        // Se comenta la carga de Firebase ya que los valores de la imagen son estáticos.
        // cargarTotalesFirebase();
    }

    // Método modificado para aceptar color (acorde a la imagen)
    private JPanel crearPanelInfo(String emoji, String texto, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor); // Usa el color pasado
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        JLabel lblTexto = new JLabel(texto, SwingConstants.CENTER);
        lblTexto.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTexto.setForeground(Color.WHITE);
        panel.add(lblTexto, BorderLayout.NORTH); // Título arriba

        // Nota: En la imagen, el contador (5.0, 1) está en el centro, no abajo.
        // El Label del contador se añadirá en el constructor.

        return panel;
    }

    // Método modificado para aceptar color
    private JLabel crearLabelContador(String texto, Color textColor) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 26)); // Letra más grande
        lbl.setForeground(textColor);
        return lbl;
    }

    // Nuevo método para crear botón con colores específicos
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
    private void seleccionarEstudiante() {
    try {
        // 🔹 Paso 1: obtener lista de hijos desde Firebase (simulada aquí)
        // Si ya tienes guardada en Firebase una colección "Estudiantes" relacionada al acudiente,
        // podrías filtrarla por ID del acudiente.
        Map<String, Object> hijos = fb.listAll("Estudiantes");

        if (hijos == null || hijos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron estudiantes asociados.", "Sin Resultados", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔹 Paso 2: Crear arreglo de nombres
        String[] nombres = hijos.keySet().toArray(new String[0]);

        // 🔹 Paso 3: Mostrar diálogo para seleccionar
        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona un estudiante:",
                "Seleccionar Estudiante",
                JOptionPane.PLAIN_MESSAGE,
                null,
                nombres,
                estudianteSeleccionado // valor por defecto
        );

        // 🔹 Paso 4: Actualizar datos
        if (seleccionado != null) {
            estudianteSeleccionado = seleccionado;
            JOptionPane.showMessageDialog(this, "Has seleccionado: " + estudianteSeleccionado);

            // (Ejemplo) Actualizar valores en las tarjetas
            actualizarDatosDelEstudiante(estudianteSeleccionado);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error al cargar estudiantes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    private void actualizarDatosDelEstudiante(String nombreEstudiante) {
    try {
        // 🔹 Aquí puedes obtener la info específica desde Firebase
        // Ejemplo simulado:
        Map<String, Object> info = fb.get("Estudiantes/" + nombreEstudiante);

        // 🔹 Si no existe, mostrar mensaje
        if (info == null) {
            lblTareasPendientes.setText("—");
            lblPromedioEstudiante.setText("—");
            return;
        }

        // 🔹 Actualizar los labels con los valores reales o simulados
        Object tareasPend = info.getOrDefault("tareasPendientes", "0");
        Object promedio = info.getOrDefault("promedio", "—");

        lblTareasPendientes.setText(String.valueOf(tareasPend));
        lblPromedioEstudiante.setText(String.valueOf(promedio));

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error al obtener datos del estudiante: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    // Se mantiene el método de Firebase por si se necesita, aunque no se usa en el constructor
    private void cargarTotalesFirebase() {
        try {
            Map<String, Object> tareas = fb.listAll("Tareas");
            Map<String, Object> materias = fb.listAll("Materias");
            // Se usa el label de promedio para mostrar un total si se desea
            lblTareasPendientes.setText(String.valueOf(tareas.size()));
            lblPromedioEstudiante.setText(String.valueOf(materias.size()));
        } catch (Exception e) {
            lblTareasPendientes.setText("Err");
            lblPromedioEstudiante.setText("Err");
        }
    }

    // Método principal para ejecutar la aplicación de escritorio
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AcudientePantalla frame = new AcudientePantalla("prueba");
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
