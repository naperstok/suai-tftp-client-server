package client;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class Notification {
    private final static int WIDTH = 400, HEIGHT = 150;

    public static JDialog createNotification(JFrame frame, String title, boolean modal, boolean correct, String text) {
        JDialog dialog = new JDialog(frame, title, modal);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(WIDTH, HEIGHT);
        dialog.setLayout(null);
        JPanel panel = new JPanel();
        panel.add(new JLabel(text));
        panel.setBounds(50, 50, 300, 30);
        dialog.add(panel);
        if (correct) {
            panel.setBackground(Color.green);
        } else {
            panel.setBackground(Color.red);
        }
        return dialog;
    }
}
