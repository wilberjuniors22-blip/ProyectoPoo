package Login.Screen;

import Login.Functionality.Firebaseconnect;
import Login.Functionality.Redirecttopage;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LoginScreen extends JFrame {
    private JTextField correoField;
    private JPasswordField codigoField;

    public LoginScreen() {
        setTitle("Sistema de Gestión Educativa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Bienvenido", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(0, 51, 153));

        JLabel subtitle = new JLabel("Sistema de Gestión Educativa", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 15));

        correoField = createRoundedField("Usuario");
        codigoField = new JPasswordField();
        codigoField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        codigoField.setBackground(new Color(245, 240, 240));
        codigoField.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JButton loginButton = new JButton("🔘 Iniciar Sesión");
        loginButton.setBackground(new Color(0, 51, 153));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 153), 2, true));

        loginButton.addActionListener(e -> {
            String correo = correoField.getText().trim();
            String codigo = new String(codigoField.getPassword()).trim();

            if (correo.isEmpty() || codigo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor ingresa correo y código.");
                return;
            }

            Map<String, Object> user = Firebaseconnect.verificarUsuario(correo, codigo);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Bienvenido " + user.get("nombre"));
                dispose();
                Redirecttopage.redirigirPorRol(user.get("rol").toString(), user.get("nombre").toString());
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas o usuario no encontrado.");
            }
        });

        gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(title, gbc);
        gbc.gridy++;
        mainPanel.add(subtitle, gbc);
        gbc.gridy++;
        mainPanel.add(correoField, gbc);
        gbc.gridy++;
        mainPanel.add(codigoField, gbc);
        gbc.gridy++;
        mainPanel.add(loginButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JTextField createRoundedField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setBackground(new Color(245, 240, 240));
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        return field;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
