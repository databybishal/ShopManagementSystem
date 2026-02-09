package src;

import javax.swing.*;
import java.awt.*;

public class ShopApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    public ShopApp() {
        setTitle("Simple Shop System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Add panels
        mainPanel.add(new LoginPanel(this), "login");
        mainPanel.add(new RegisterPanel(this), "register");
        mainPanel.add(new DashboardPanel(this), "dashboard");
        
        add(mainPanel);
        showLogin();
    }
    
    public void showLogin() {
        cardLayout.show(mainPanel, "login");
    }
    
    public void showRegister() {
        cardLayout.show(mainPanel, "register");
    }
    
    public void showDashboard() {
        cardLayout.show(mainPanel, "dashboard");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ShopApp().setVisible(true);
        });
    }
}