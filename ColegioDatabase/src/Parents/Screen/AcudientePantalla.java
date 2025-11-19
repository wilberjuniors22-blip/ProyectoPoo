package Parents.Screen;

import Admin.UserManagement.FirebaseClient;
import Parents.Calendar.HorarioAcudiente;
import Parents.NotasAsistencias.VerAsistencias;
import Parents.NotasAsistencias.VerNotas;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AcudientePantalla extends JFrame {

    private JLabel lblTareasPendientes;
    private JLabel lblPromedioEstudiante;
    private final FirebaseClient fb;
    private String acudienteId;
    private JButton btnSeleccionar;
    private String estudianteSeleccionadoId;

    // Mapas de ayuda para la selección de asignaturas
    private Map<String, String> mapaNombresAsignaturas = new HashMap<>();
    private Map<String, String> mapaClaseDisplayToId = new HashMap<>();
    private String cursoEstudianteSeleccionado;

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
        add(crearBoton("⭐", "Ver Notas Generales", "Notas de Materias", x, y + h + esp, botonColor, botonRolloverColor, () -> seleccionarClaseYVer("Notas")));
        add(crearBoton("🧑‍🏫", "Ver Asistencias", "Asistencias del Estudiante", x + w + esp, y + h + esp, botonColor, botonRolloverColor, () -> seleccionarClaseYVer("Asistencias")));

        add(crearBoton("⚠️", "Historial de Reportes", "", x2, y, botonColor, botonRolloverColor, () -> accionBoton("Historial de Reportes")));
        add(crearBoton("✍️", "Crear Reclamo", "", x2 + w + esp, y, botonColor, botonRolloverColor, () -> accionBoton("Crear Reclamo")));

        JButton botonLargo = crearBoton("📞", "Cambiar Información de Contacto", "Actualizar Teléfono o Email", x2, y + h + esp, botonColor, botonRolloverColor, () -> accionBoton("Cambiar Información de Contacto"));
        botonLargo.setBounds(x2, y + h + esp, 2 * w + esp, h);
        add(botonLargo);

        cargarNombresAsignaturas();
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

    // --- LÓGICA DE DATOS ---

    private void cargarNombresAsignaturas() {
        new Thread(() -> {
            try {
                Map<String, Object> asignaturas = fb.listAll("Asignaturas");
                if (asignaturas != null) {
                    for (Object obj : asignaturas.values()) {
                        if (obj instanceof Map) {
                            Map<String, Object> a = (Map<String, Object>) obj;
                            String codigo = String.valueOf(a.get("codigo"));
                            String nombre = String.valueOf(a.get("nombre"));
                            mapaNombresAsignaturas.put(codigo, nombre);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

            Map<String, String> mapaNombres = new HashMap<>();
            Map<String, String> mapaIdToCurso = new HashMap<>();

            for (String id : idsHijos) {
                try {
                    Map<String, Object> est = fb.get("Usuarios/Estudiantes/" + id);
                    String nombre = est.getOrDefault("nombre", "Sin nombre").toString();
                    String curso = est.getOrDefault("curso", "N/A").toString();
                    mapaNombres.put(id, nombre);
                    mapaIdToCurso.put(id, curso);
                } catch (Exception e) {
                    mapaNombres.put(id, "Error: " + id);
                }
            }

            String[] nombres = mapaNombres.values().toArray(new String[0]);
            String elegido = (String) JOptionPane.showInputDialog(
                    this, "Selecciona un estudiante:", "Hijos",
                    JOptionPane.PLAIN_MESSAGE, null, nombres, null
            );

            if (elegido != null) {
                String idEncontrado = mapaNombres.entrySet().stream()
                        .filter(e -> e.getValue().equals(elegido))
                        .findFirst().get().getKey();

                estudianteSeleccionadoId = idEncontrado;
                cursoEstudianteSeleccionado = mapaIdToCurso.get(idEncontrado);
                btnSeleccionar.setText(elegido);
                actualizarDatosDelEstudiante(idEncontrado);
            }


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando hijos: " + e.getMessage());
        }
    }

    private void actualizarDatosDelEstudiante(String id) {
        new Thread(() -> {
            try {
                Map<String, Object> info = fb.get("Usuarios/Estudiantes/" + id);
                String tareas = info.getOrDefault("tareasPendientes", "0").toString();
                String promedio = info.getOrDefault("promedio", "—").toString();

                SwingUtilities.invokeLater(() -> {
                    lblTareasPendientes.setText(tareas);
                    lblPromedioEstudiante.setText(promedio);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblTareasPendientes.setText("—");
                    lblPromedioEstudiante.setText("—");
                });
            }
        }).start();
    }

    private void seleccionarClaseYVer(String tipo) {
        if (estudianteSeleccionadoId == null || estudianteSeleccionadoId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Por favor, primero selecciona un hijo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                mapaClaseDisplayToId.clear();

                Map<String, Object> todasLasClases = fb.listAll("Clases");
                if (todasLasClases == null) return;

                for (String idClase : todasLasClases.keySet()) {
                    Map<String, Object> dataClase = (Map<String, Object>) todasLasClases.get(idClase);
                    if (dataClase == null) continue;

                    java.util.List<?> cursosClase = (java.util.List<?>) dataClase.get("cursos");
                    if (cursosClase != null && cursosClase.contains(cursoEstudianteSeleccionado)) {

                        String codigoAsignatura = (String) dataClase.get("asignaturaCodigo");
                        String nombreReal = mapaNombresAsignaturas.getOrDefault(codigoAsignatura, "Materia Desconocida");

                        String nombreMostrar = nombreReal + " (" + codigoAsignatura + ")";
                        mapaClaseDisplayToId.put(nombreMostrar, idClase);
                    }
                }

                String[] nombresClases = mapaClaseDisplayToId.keySet().toArray(new String[0]);

                if (nombresClases.length == 0) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "No se encontraron clases para el curso: " + cursoEstudianteSeleccionado, "Error", JOptionPane.ERROR_MESSAGE));
                    return;
                }

                String claseElegida = (String) JOptionPane.showInputDialog(
                        this, "Selecciona la materia para ver las " + tipo.toLowerCase() + ":",
                        tipo + " del Estudiante", JOptionPane.PLAIN_MESSAGE, null, nombresClases, nombresClases[0]
                );

                if (claseElegida != null) {
                    String idClase = mapaClaseDisplayToId.get(claseElegida);

                    SwingUtilities.invokeLater(() -> {
                        if (tipo.equals("Notas")) {
                            new VerNotas(this, fb, estudianteSeleccionadoId, idClase, claseElegida).setVisible(true);
                        } else if (tipo.equals("Asistencias")) {
                            new VerAsistencias(this, fb, estudianteSeleccionadoId, idClase, claseElegida).setVisible(true);
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error al cargar clases: " + e.getMessage()));
            }
        }).start();
    }


    private void accionBoton(String nombre) {
        if (estudianteSeleccionadoId == null || estudianteSeleccionadoId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Por favor, primero selecciona un hijo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nombre.equals("Calendario Académico")) {
            new HorarioAcudiente(estudianteSeleccionadoId).setVisible(true);
            return;
        }

        JOptionPane.showMessageDialog(this, "Presionaste: " + nombre + " para el estudiante con ID: " + estudianteSeleccionadoId);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AcudientePantalla f = new AcudientePantalla("Stephanie Guerrero", "-OeNy6ZWjGSmWDs7MAGt");
                f.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}