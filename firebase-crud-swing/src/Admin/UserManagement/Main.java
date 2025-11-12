package Admin.UserManagement;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FirebaseClient fb = new FirebaseClient("conexion.txt");
                CrudFrame ui = new CrudFrame(fb);
                ui.setVisible(true);
            } catch (IOException e) {
            }
        });
    }
}
