import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {
    Core core;
    public GUI(Core core){
        setTitle("Cloud Storage");
        setBounds(300,300,800,400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel loginPanel = createLoginPanel();
        add(loginPanel,BorderLayout.NORTH);
        JPanel toolbarPanel = createToolbarPanel();
        add(toolbarPanel,BorderLayout.SOUTH);
        JPanel workspacePanel = createWorkspacePanel();
        add(workspacePanel,BorderLayout.CENTER);
        setVisible(true);
        this.core = core;
    }

    private JPanel createLoginPanel(){
        JPanel loginPanel = new JPanel(new GridLayout(1,5));
        JTextField fldLogin = new JTextField();
        JTextField fldPassword = new JTextField();
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(a -> {
            core.authSend(fldLogin.getText(),fldPassword.getText());
        });
        loginPanel.add(new JLabel("Login:"));
        loginPanel.add(fldLogin);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(fldPassword);
        loginPanel.add(btnLogin);
        add(loginPanel, BorderLayout.NORTH);
        return loginPanel;
    }

    private JPanel createToolbarPanel(){
        JPanel toolbarPanel = new JPanel(new GridLayout(1, 6));
        JButton btnBack = new JButton("Back");
        JButton btnNext = new JButton("Next");
        JButton btnNewFolder = new JButton("New folder");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnLogOut = new JButton("Logout");
        toolbarPanel.add(btnBack);
        toolbarPanel.add(btnNext);
        toolbarPanel.add(btnNewFolder);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnRefresh);
        toolbarPanel.add(btnLogOut);
        return toolbarPanel;
    }

    private JPanel createWorkspacePanel(){
        JPanel workspacePanel = new JPanel();
        return workspacePanel;
    }
}
