package client;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class Client {
    private static String ipServer = null;
    public static String CLIENT_ROOT;
    private static MenuFrame menu;

    public Client(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar client.jar <client_path>");
            return;
        }

        menu =  new MenuFrame();
        menu.setVisible(true);

        new IpFrame().setVisible(true);


        CLIENT_ROOT = args[0];
    }

    public static MenuFrame getMenu() {
        return menu;
    }

    public static String getIp() {
        return ipServer;
    }

    public class MenuFrame extends JFrame {
        private final static int WIDTH = 600;
        private final static int HEIGHT = 400;
        private final JTextField textField;
        private final JButton prqButton;
        private final JButton wrqButton;
        private final JButton helpButton;
        private final JButton exitButton;

        public MenuFrame() {
            this.setTitle("Menu");
            this.setSize(WIDTH, HEIGHT);
            this.setResizable(false);
            this.setLayout(null);
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            textField = new JTextField("Enter the file name");
            prqButton = new JButton("Get a file");
            wrqButton = new JButton("Send a file");
            helpButton = new JButton("Get help");
            exitButton = new JButton("Exit");
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(5, 1, 20, 20));
            panel.add(textField);
            panel.add(prqButton);
            panel.add(wrqButton);
            panel.add(helpButton);
            panel.add(exitButton);
            initButtons();
            panel.setBounds(200, 30, 200, 250);
            this.add(panel);
        }

        public void initButtons() {
            prqButton.addActionListener(e -> {
                if (!textField.getText().contains(".")) {
                    Notification.createNotification(this, "Error!", true, false, "Please, specify the file format.").setVisible(true);
                } else {
                    String result = "rrq " + textField.getText();
                    new CommandParser().parse(result);
                }
            });
            wrqButton.addActionListener(e -> {
                if (!textField.getText().contains(".")) {
                    Notification.createNotification(this, "Error!", true, false, "Please, specify the file format.").setVisible(true);
                } else {
                    String result = "wrq " + textField.getText();
                    new CommandParser().parse(result);
                }
            });
            helpButton.addActionListener(e -> {
                createHelp(this, 400, 400, "Help", true).setVisible(true);
            });
            exitButton.addActionListener(e -> {
                System.exit(0);
            });
        }
    }

    private JDialog createHelp(JFrame frame, int width, int height, String title, boolean modal)
    {
        JDialog dialog = new JDialog(frame, title, modal);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setSize(width, height);
        dialog.setLayout(null);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 0, 0));
        panel.add(new JLabel("<html>* When starting the program, do not forget to specify<br> the IP address of the server.</html>"));
        panel.add(new JLabel("<html>* Get a file - a button that allows you to get a file <br> from the server.</html>"));
        panel.add(new JLabel("<html>* Send a file - a button that allows you to upload a <br> file to the server.</html>"));
        panel.add(new JLabel("<html>* Help - a button that allows you to get a information <br> about this program.</html>"));
        panel.add(new JLabel("<html>* Exit - a button that allows you to close this program.</html>"));
        panel.setBounds(50, 5, 300, 380);
        dialog.add(panel);
        return dialog;
    }

    public class IpFrame extends JFrame {
        private final int WIDTH = 300;
        private final int HEIGHT = 200;
        private final JTextField jTextField;
        private final JButton button;


        public IpFrame() {
            this.setTitle("Enter the ip of server");
            this.setResizable(false);
            this.setSize(WIDTH, HEIGHT);
            this.setLayout(null);
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(2, 1, 30, 30));
            jTextField = new JTextField();
            jTextField.setText("localhost");
            panel.add(jTextField);
            button = new JButton("Set ip address of the server");
            button.addActionListener(e -> {
                ipServer = jTextField.getText();
                if (!ipServer.contains(".") && !ipServer.equals("localhost")) {
                    Notification.createNotification(this, "Error!", true, false, "Please, enter the correct IP address").setVisible(true);
                } else {
                    this.setVisible(false);
                }
            });
            button.setSize(100, 50);
            panel.add(button);
            panel.setBounds(50, 30, 200, 100);
            this.add(panel);
        }
    }
}
