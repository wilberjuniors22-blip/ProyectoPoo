package Teachers.Evaluar;

import Admin.UserManagement.FirebaseClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VistaProfesorAsistencias extends JFrame {

    private final FirebaseClient fb;
    private final String docenteNombre;

    private JComboBox<String> comboClases;
    private JComboBox<String> comboFechas;
    private JTable tabla;
    private DefaultTableModel modelo;

    private Map<String,Object> clasesMap;
    private String claseSeleccionada;


    public VistaProfesorAsistencias(String docenteNombre, FirebaseClient fb){
        this.fb = fb;
        this.docenteNombre = docenteNombre;

        setTitle("Tomar Asistencia");
        setSize(900,600);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240,240,240));

        JLabel lbl1 = new JLabel("Clase:");
        lbl1.setBounds(20,20,100,30);
        add(lbl1);

        comboClases = new JComboBox<>();
        comboClases.setBounds(80,20,250,30);
        add(comboClases);

        JLabel lbl2 = new JLabel("Fecha:");
        lbl2.setBounds(360,20,100,30);
        add(lbl2);

        comboFechas = new JComboBox<>();
        comboFechas.setBounds(420,20,200,30);
        add(comboFechas);

        JButton btnCargar = new JButton("Cargar");
        btnCargar.setBounds(640,20,120,30);
        add(btnCargar);

        modelo = new DefaultTableModel(new String[]{"ID","Nombre","Asistencia (P/F)"},0);
        tabla = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBounds(20,70,840,420);
        add(sp);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(350,500,200,40);
        add(btnGuardar);

        cargarClases();

        btnCargar.addActionListener(e -> {
            try { cargarAsistencia(); } catch(Exception ex) { ex.printStackTrace(); }
        });

        btnGuardar.addActionListener(e -> {
            try { guardarAsistencia(); } catch(Exception ex) { ex.printStackTrace(); }
        });
    }

    private void cargarClases() {
        try {
            clasesMap = fb.listAll("Clases");
            comboClases.removeAllItems();
            for (String id : clasesMap.keySet()){
                Map c = (Map) clasesMap.get(id);
                if (c==null) continue;
                Object doc = c.get("docentenombre");
                if (doc!=null && doc.toString().equalsIgnoreCase(docenteNombre)){
                    comboClases.addItem(id);
                }
            }
        } catch(Exception e){ e.printStackTrace(); }
    }
    private void cargarAsistencia() throws Exception {
        if (comboClases.getSelectedItem()==null) return;
        claseSeleccionada = comboClases.getSelectedItem().toString();

        comboFechas.removeAllItems();
        Map<String,Object> asistencias = fb.listAll("Asistencias/"+claseSeleccionada);
        if (asistencias!=null){
            if (!asistencias.isEmpty()) {
                Map.Entry<String, Object> primerEstudiante = asistencias.entrySet().iterator().next();
                Map<String, Object> fechasEstudiante = (Map<String, Object>) primerEstudiante.getValue();
                for (String f : fechasEstudiante.keySet()) {
                    comboFechas.addItem(f);
                }
            }
        }

        Map clase = (Map) clasesMap.get(claseSeleccionada);

        java.util.List<?> cursos = (java.util.List<?>) clase.get("cursos");

        if (cursos == null || cursos.isEmpty()) {
            return;
        }
        String curso = cursos.get(0).toString();

        modelo.setRowCount(0);
        Map<String,Object> usuarios = fb.listAll("Usuarios");
        for (String idUser : usuarios.keySet()){
            Map u = (Map) usuarios.get(idUser);
            if (u==null) continue;
            if ("estudiante".equals(u.get("tipo")) && curso.equals(u.get("curso"))) {
                modelo.addRow(new Object[]{idUser, u.get("nombre"), ""});
            }
        }
    }

    private void guardarAsistencia() throws Exception {
        if (comboFechas.getSelectedItem()==null) {
            JOptionPane.showMessageDialog(this,"Selecciona una fecha");
            return;
        }

        String fecha = comboFechas.getSelectedItem().toString();
        claseSeleccionada = comboClases.getSelectedItem().toString();

        String rutaBase = "Asistencias/" + claseSeleccionada;

        for (int i=0;i<modelo.getRowCount();i++){
            String idEst = modelo.getValueAt(i,0).toString();
            String valor = modelo.getValueAt(i,2).toString(); // P o F

            Map<String,Object> fechaValor = new HashMap<>();
            fechaValor.put(fecha, valor);

            Map<String,Object> asistenciasEstudiante = fb.get(rutaBase + "/" + idEst);
            asistenciasEstudiante.putAll(fechaValor);
            fb.updateGenericNode(rutaBase + "/" + idEst, asistenciasEstudiante);
        }

        JOptionPane.showMessageDialog(this,"Asistencia guardada");
    }
}
