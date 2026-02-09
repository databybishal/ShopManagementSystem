package src;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterPanel extends JPanel {
    private ShopApp mainApp;
    private JTextField usernameField, emailField, phoneField;
    private JPasswordField passwordField;
    
    public RegisterPanel(ShopApp app) {
        this.mainApp = app;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        JLabel title = new JLabel("Register New Account");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);
        
        // Fields
        String[] labels = {"Username:", "Password:", "Email:", "Phone:"};
        JComponent[] fields = {
            usernameField = new JTextField(20),
            passwordField = new JPasswordField(20),
            emailField = new JTextField(20),
            phoneField = new JTextField(20)
        };
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridwidth = 1;
            gbc.gridy = i + 1; gbc.gridx = 0;
            add(new JLabel(labels[i]), gbc);
            
            gbc.gridx = 1;
            add(fields[i], gbc);
        }
        
        // Buttons
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> register());
        gbc.gridy = labels.length + 1; gbc.gridx = 0; gbc.gridwidth = 2;
        add(registerBtn, gbc);
        
        JButton backBtn = new JButton("Back to Login");
        backBtn.addActionListener(e -> mainApp.showLogin());
        gbc.gridy = labels.length + 2;
        add(backBtn, gbc);
    }
    
    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();
        String phone = phoneField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password required");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            // Check if username exists
            String checkSql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists");
                return;
            }
            
            // Insert new user
            String insertSql = "INSERT INTO users (username, password, email, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, email);
            insertStmt.setString(4, phone);
            
            insertStmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful!");
            mainApp.showLogin();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}