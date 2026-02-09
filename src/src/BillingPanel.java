package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class BillingPanel extends JPanel {
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JTextField customerField, searchField;
    private JLabel totalLabel;
    private Map<Integer, Product> productsMap = new HashMap<>();
    
    public BillingPanel() {
        setLayout(new BorderLayout());
        
        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Customer Name:"));
        customerField = new JTextField(20);
        topPanel.add(customerField);
        
        topPanel.add(new JLabel("Search Product:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);
        
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchProducts());
        topPanel.add(searchBtn);
        
        // Product list (left side)
        String[] productColumns = {"ID", "Name", "Price", "Stock"};
        DefaultTableModel productModel = new DefaultTableModel(productColumns, 0);
        JTable productTable = new JTable(productModel);
        loadProductList(productModel);
        
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = productTable.getSelectedRow();
                if (row >= 0) {
                    int id = (int) productModel.getValueAt(row, 0);
                    String name = (String) productModel.getValueAt(row, 1);
                    double price = (double) productModel.getValueAt(row, 2);
                    int stock = (int) productModel.getValueAt(row, 3);
                    
                    // Ask for quantity
                    String qtyStr = JOptionPane.showInputDialog(this, 
                        "Enter quantity for " + name + " (Stock: " + stock + "):", "1");
                    
                    if (qtyStr != null && !qtyStr.isEmpty()) {
                        try {
                            int quantity = Integer.parseInt(qtyStr);
                            if (quantity > 0 && quantity <= stock) {
                                addToCart(id, name, price, quantity);
                            } else {
                                JOptionPane.showMessageDialog(this, "Invalid quantity");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Enter valid number");
                        }
                    }
                }
            }
        });
        
        JScrollPane productScroll = new JScrollPane(productTable);
        productScroll.setPreferredSize(new Dimension(400, 300));
        
        // Cart (right side)
        String[] cartColumns = {"Product", "Price", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cartColumns, 0);
        cartTable = new JTable(cartModel);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.add(new JLabel("Shopping Cart", SwingConstants.CENTER), BorderLayout.NORTH);
        cartPanel.add(cartScroll, BorderLayout.CENTER);
        
        // Total and buttons
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(Color.RED);
        
        JButton removeBtn = new JButton("Remove Item");
        removeBtn.addActionListener(e -> removeFromCart());
        
        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());
        
        JButton generateBillBtn = new JButton("Generate Bill");
        generateBillBtn.addActionListener(e -> generateBill());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(removeBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(generateBillBtn);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        cartPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Split pane for product list and cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productScroll, cartPanel);
        splitPane.setDividerLocation(400);
        
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void loadProductList(DefaultTableModel model) {
        model.setRowCount(0);
        productsMap.clear();
        
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM products WHERE quantity > 0");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("quantity");
                
                model.addRow(new Object[]{id, name, price, stock});
                productsMap.put(id, new Product(id, name, price, stock));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void searchProducts() {
        // Basic search functionality
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            DefaultTableModel model = (DefaultTableModel) ((JTable)((JScrollPane)((JSplitPane)getComponent(1)).getLeftComponent()).getViewport().getView()).getModel();
            model.setRowCount(0);
            
            String sql = "SELECT * FROM products WHERE quantity > 0 AND (name LIKE ? OR id LIKE ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("quantity");
                
                model.addRow(new Object[]{id, name, price, stock});
                productsMap.put(id, new Product(id, name, price, stock));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addToCart(int productId, String name, double price, int quantity) {
        // Check if already in cart
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(name)) {
                int currentQty = (int) cartModel.getValueAt(i, 2);
                cartModel.setValueAt(currentQty + quantity, i, 2);
                cartModel.setValueAt((currentQty + quantity) * price, i, 3);
                updateTotal();
                return;
            }
        }
        
        // Add new item to cart
        double subtotal = price * quantity;
        cartModel.addRow(new Object[]{name, price, quantity, subtotal});
        updateTotal();
    }
    
    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) {
            cartModel.removeRow(row);
            updateTotal();
        }
    }
    
    private void clearCart() {
        cartModel.setRowCount(0);
        updateTotal();
    }
    
    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += (double) cartModel.getValueAt(i, 3);
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
    
    private void generateBill() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty");
            return;
        }
        
        String customer = customerField.getText();
        if (customer.isEmpty()) customer = "Walk-in Customer";
        
        // Create bill items list
        java.util.List<BillItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String name = (String) cartModel.getValueAt(i, 0);
            double price = (double) cartModel.getValueAt(i, 1);
            int quantity = (int) cartModel.getValueAt(i, 2);
            items.add(new BillItem(name, price, quantity));
        }
        
        // Ask for tax rate
        String taxRateStr = JOptionPane.showInputDialog(this, 
            "Enter tax rate (%):", "13");
        double taxRate = 13.0;
        try {
            if (taxRateStr != null && !taxRateStr.isEmpty()) {
                taxRate = Double.parseDouble(taxRateStr);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Using default tax rate 13%");
        }
        
        // Ask for discount
        String discountStr = JOptionPane.showInputDialog(this, 
            "Enter discount (%):", "0");
        double discountPercent = 0.0;
        try {
            if (discountStr != null && !discountStr.isEmpty()) {
                discountPercent = Double.parseDouble(discountStr);
            }
        } catch (NumberFormatException e) {
            // No discount
        }
        
        // Generate bill summary using BillCalculator
        String billSummary = BillCalculator.generateBillSummary(customer, items, taxRate, discountPercent);
        
        // Calculate final amount
        double subtotal = BillCalculator.calculateTotal(items);
        double finalAmount = BillCalculator.calculateFinalAmount(subtotal, taxRate, discountPercent);
        
        // Show bill summary
        JTextArea textArea = new JTextArea(billSummary);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), 
            "Bill Generated", JOptionPane.INFORMATION_MESSAGE);
        
        // Ask for payment
        String paymentStr = JOptionPane.showInputDialog(this, 
            "Total Amount: $" + String.format("%.2f", finalAmount) + 
            "\nEnter amount paid:", String.format("%.2f", finalAmount));
        
        if (paymentStr != null && !paymentStr.isEmpty()) {
            try {
                double amountPaid = Double.parseDouble(paymentStr);
                
                if (BillCalculator.validatePayment(amountPaid, finalAmount)) {
                    double change = BillCalculator.calculateChange(amountPaid, finalAmount);
                    
                    // Show receipt with change
                    String receipt = billSummary + 
                        String.format("\nAmount Paid: $%.2f", amountPaid) +
                        String.format("\nChange: $%.2f", change) +
                        "\n\nPayment Successful!";
                    
                    JOptionPane.showMessageDialog(this, receipt, 
                        "Payment Receipt", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Save to database
                    saveBillToDatabase(customer, finalAmount);
                    
                    // Clear cart
                    clearCart();
                    customerField.setText("");
                    
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Insufficient payment!\nRequired: $" + finalAmount, 
                        "Payment Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid payment amount");
            }
        }
    }
    
    private void saveBillToDatabase(String customer, double total) {
        try (Connection conn = DBConnection.getConnection()) {
            // Save bill
            String billSql = "INSERT INTO bills (customer_name, total_amount) VALUES (?, ?)";
            PreparedStatement billStmt = conn.prepareStatement(billSql);
            billStmt.setString(1, customer);
            billStmt.setDouble(2, total);
            billStmt.executeUpdate();
            
            // Update product quantities
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String productName = (String) cartModel.getValueAt(i, 0);
                int qtySold = (int) cartModel.getValueAt(i, 2);
                
                String updateSql = "UPDATE products SET quantity = quantity - ? WHERE name = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, qtySold);
                updateStmt.setString(2, productName);
                updateStmt.executeUpdate();
            }
            
            JOptionPane.showMessageDialog(this, "Bill saved successfully!");
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving bill: " + e.getMessage());
        }
    }
    
    class Product {
        int id;
        String name;
        double price;
        int stock;
        
        Product(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }
    }
}