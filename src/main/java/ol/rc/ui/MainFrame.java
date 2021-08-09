package ol.rc.ui;

import ol.rc.SettingsNet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class MainFrame extends JFrame {
    SettingsNet settingsNet;
    JPanel mainPanel;
    List<ISettingsEditor> listOfSettingsEditors;


    public MainFrame(List<ISettingsEditor> listOfSettingsEditors) {
        super();
        setDefaultLookAndFeelDecorated(true);
        this.settingsNet = settingsNet;
        mainPanel = new JPanel();
        setContentPane(new JScrollPane(mainPanel));
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        addViews();
        pack();
    }


    public void addViews() {
        listOfSettingsEditors.forEach(item -> mainPanel.add(item.getSettingsUI()));
    }

    public void updateViews() {
        listOfSettingsEditors.forEach(ISettingsEditor::updateView);
    }


    private void addFPSComponents() {
        JPanel panel = new JPanel();
        mainPanel.add(panel);
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        //panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        //panel.setMaximumSize(new Dimension(500,20));
        panel.setMinimumSize(new Dimension(500, 20));

        Label label = new Label("Address:Port");
        panel.add(label);
        JTextField tfIPPort = new JTextField();
        //tfIPPort.setMinimumSize(new Dimension(100,50));
        tfIPPort.setMinimumSize(label.getSize());


        panel.add(tfIPPort);
        tfIPPort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] ipPort = ((JTextField) e.getSource()).getText().split(":");
                int port = Integer.parseInt(ipPort[1]);
                try {
                    Socket socket = new Socket(InetAddress.getByName(ipPort[0]), port);
                    //clientInfo.socket=socket;
                } catch (Exception ex) {

                }

            }
        });

    }

    private void addSoketComponents() {
        JPanel panel = new JPanel();
        mainPanel.add(panel);
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        panel.setMinimumSize(new Dimension(500, 20));

        Label label = new Label("Address:Port");
        panel.add(label);
        JTextField tfIPPort = new JTextField();
        tfIPPort.setMinimumSize(label.getSize());

        panel.add(tfIPPort);

        tfIPPort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] ipPort = ((JTextField) e.getSource()).getText().split(":");
                int port = Integer.parseInt(ipPort[1]);
                try {
                    Socket socket = new Socket(InetAddress.getByName(ipPort[0]), port);
                    //clientInfo.socket=socket;
                } catch (Exception ex) {

                }

            }
        });
    }

    private void addButtonsClientServer() {
    }
}
