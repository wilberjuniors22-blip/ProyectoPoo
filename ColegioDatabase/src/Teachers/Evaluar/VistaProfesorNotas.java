package Teachers.Evaluar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import Admin.UserManagement.FirebaseClient;

public class VistaProfesorNotas extends JFrame {
    private final FirebaseClient fb;
    private final String docenteNombre;

    private JComboBox<String> comboClases;
    private JComboBox<String> comboActividades;
    private JTable tablaNotas;
    private DefaultTableModel modeloTabla;

    private Map<String, Object> clasesMap;
    private String claseSeleccionadaId;
    private String actividadSeleccionada;

    public VistaProfesorNotas(String docenteNombre, FirebaseClient fb) {
        this.fb = fb;
        this.docenteNombre = docenteNombre;

        setTitle("Actualizar Notas");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(240,240,240));

        JLabel lbl1 = new JLabel("Clase:");
        lbl1.setBounds(30,20,100,30);
        add(lbl1);

        comboClases = new JComboBox<>();
        comboClases.setBounds(100,20,300,30);
        add(comboClases);

        JLabel lbl2 = new JLabel("Actividad:");
        lbl2.setBounds(430,20,100,30);
        add(lbl2);

        comboActividades = new JComboBox<>();
        comboActividades.setBounds(520,20,200,30);
        add(comboActividades);

        JButton btnCargar = new JButton("Cargar");
        btnCargar.setBounds(740,20,120,30);
        add(btnCargar);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Nota"},0);
        tablaNotas = new JTable(modeloTabla);
        JScrollPane sp = new JScrollPane(tablaNotas);
        sp.setBounds(30,70,830,420);
        add(sp);

        JButton btnGuardar = new JButton("Guardar Notas");
        btnGuardar.setBounds(350,500,200,40);
        add(btnGuardar);

        cargarClasesDocente();

        btnCargar.addActionListener(e -> {
            try { cargarEstudiantesYNotas(); } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnGuardar.addActionListener(e -> {
            try { guardarNotas(); } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private void cargarClasesDocente() {
        try {
            clasesMap = fb.listAll("Clases");
            comboClases.removeAllItems();

            for (String id : clasesMap.keySet()) {
                Map c = (Map) clasesMap.get(id);
                if (c == null) continue;
                Object doc = c.get("docentenombre");
                if (doc != null && doc.toString().equalsIgnoreCase(docenteNombre)) {
                    comboClases.addItem(id);
                }
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    // ---------------------------------------------
    //  CARGAR ACTIVIDADES Y ESTUDIANTES
    // ---------------------------------------------
    private void cargarEstudiantesYNotas() throws Exception {
        if (comboClases.getSelectedItem()==null) return;
        modeloTabla.setRowCount(0);

        claseSeleccionadaId = comboClases.getSelectedItem().toString();
        Map clase = (Map) clasesMap.get(claseSeleccionadaId);
        if (clase==null) return;

        // Obtener curso
        List cursos = (List) clase.get("cursos");
        if (cursos==null || cursos.isEmpty()) return;
        String curso = cursos.get(0).toString();

        // Cargar actividades
        comboActividades.removeAllItems();
        Map<String,Object> acts = fb.listAll("Notas/"+claseSeleccionadaId);
        if (acts!=null) {
            for (String actId : acts.keySet()) comboActividades.addItem(actId);
        }

        // Cargar estudiantes del curso
        Map<String,Object> usuarios = fb.listAll("Usuarios");
        for (String idUser : usuarios.keySet()) {
            Map u = (Map) usuarios.get(idUser);
            if (u==null) continue;
            Object tipo = u.get("tipo");
            Object c = u.get("curso");

            if (tipo!=null && tipo.equals("estudiante") && c!=null && c.equals(curso)) {
                modeloTabla.addRow(new Object[]{idUser, u.get("nombre"), ""});
            }
        }
    }

    private void guardarNotas() throws Exception {
        if (comboClases.getSelectedItem()==null) return;
        if (comboActividades.getSelectedItem()==null) return;

        claseSeleccionadaId = comboClases.getSelectedItem().toString();
        actividadSeleccionada = comboActividades.getSelectedItem().toString();

        String rutaBase = "Notas/" + claseSeleccionadaId + "/" + actividadSeleccionada;

        for (int i=0;i<modeloTabla.getRowCount();i++){
            String idEst = modeloTabla.getValueAt(i,0).toString();
            String nota = modeloTabla.getValueAt(i,2).toString();

            Map<String,Object> notaObj = new HashMap<>();
            notaObj.put("nota", nota);
            fb.updateGenericNode(rutaBase + "/" + idEst, notaObj);
        }

        JOptionPane.showMessageDialog(this,"Notas guardadas");
    }
}
