package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, priceField, quantityField, categoryField;
    
    public ProductPanel() {
        setLayout(new BorderLayout());
        
        // Top panel with form
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Product"));
        
        formPanel.add(new JLabel("Product Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        formPanel.add(priceField);
        
        formPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        formPanel.add(quantityField);
        
        formPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        formPanel.add(categoryField);
        
        JButton addBtn = new JButton("Add Product");
        addBtn.addActionListener(e -> addProduct());
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBtn);
        buttonPanel.add(clearBtn);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Table for products
        String[] columns = {"ID", "Name", "Price", "Quantity", "Category"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        
        // Bottom buttons
        JPanel bottomPanel = new JPanel();
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteProduct());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadProducts());
        
        bottomPanel.add(refreshBtn);
        bottomPanel.add(deleteBtn);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        loadProducts();
    }
    
    private void loadProducts() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM products");
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("category")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addProduct() {
        String name = nameField.getText();
        String priceStr = priceField.getText();
        String quantityStr = quantityField.getText();
        String category = categoryField.getText();
        
        if (name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields");
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);
            
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO products (name, price, quantity, category) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, quantity);
                stmt.setString(4, category);
                
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product added!");
                clearForm();
                loadProducts();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to delete");
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this product?", 
            "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM products WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                stmt.executeUpdate();
                
                loadProducts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        quantityField.setText("");
        categoryField.setText("");
    }
}