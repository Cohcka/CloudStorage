import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

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
        JTextField fldLogin = new JTextField("login"); // tmp
        JTextField fldPassword = new JTextField("Pass"); // tmp
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(a -> {
            core.auth(fldLogin.getText(),fldPassword.getText());
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
        JButton btnNewFolder = new JButton("New folder");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnLogOut = new JButton("Logout");
        JButton btnUpload = new JButton("Upload");
        JButton btnDownload = new JButton("Download");
        btnDelete.addActionListener(a -> {
            core.deleteFile("SrvStorage/FileToTransfer.txt");
        });
        btnRefresh.addActionListener(a -> {
            core.updateFileList("SrvStorage");
        });
        btnUpload.addActionListener(a -> {
            core.uploadFile("FileToTransfer.txt");
            //core.uploadFile("TK.bak");
        });
        btnDownload.addActionListener(a -> {
            core.downloadFile("Hehe.gip");
        });
        toolbarPanel.add(btnBack);
        toolbarPanel.add(btnNewFolder);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnRefresh);
        toolbarPanel.add(btnDownload);
        toolbarPanel.add(btnUpload);
        toolbarPanel.add(btnLogOut);
        return toolbarPanel;
    }

    private JPanel createWorkspacePanel(){
        JPanel workspacePanel = new JPanel();
        return workspacePanel;
    }
}
