package src;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private ShopApp mainApp;
    
    public DashboardPanel(ShopApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        
        // Top menu
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> mainApp.showLogin());
        topPanel.add(logoutBtn);
        
        // Center buttons
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        JButton productsBtn = new JButton("Manage Products");
        productsBtn.setFont(new Font("Arial", Font.BOLD, 16));
        productsBtn.addActionListener(e -> openProducts());
        
        JButton billingBtn = new JButton("Create Bill");
        billingBtn.setFont(new Font("Arial", Font.BOLD, 16));
        billingBtn.addActionListener(e -> openBilling());
        
        JButton reportsBtn = new JButton("View Reports");
        reportsBtn.setFont(new Font("Arial", Font.BOLD, 16));
        reportsBtn.addActionListener(e -> openReports());
        
        centerPanel.add(productsBtn);
        centerPanel.add(billingBtn);
        centerPanel.add(reportsBtn);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void openProducts() {
        JFrame frame = new JFrame("Product Management");
        frame.setSize(800, 500);
        frame.add(new ProductPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void openBilling() {
        JFrame frame = new JFrame("Billing");
        frame.setSize(1000, 600);
        frame.add(new BillingPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void openReports() {
        JFrame frame = new JFrame("Reports");
        frame.setSize(800, 500);
        frame.add(new ReportsPanel());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}