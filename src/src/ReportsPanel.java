package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportsPanel extends JPanel {
    private JTable reportTable;
    private DefaultTableModel tableModel;
    
    public ReportsPanel() {
        setLayout(new BorderLayout());
        
        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton salesBtn = new JButton("Sales Report");
        salesBtn.addActionListener(e -> loadSalesReport());
        
        JButton productsBtn = new JButton("Products Report");
        productsBtn.addActionListener(e -> loadProductsReport());
        
        JButton dailyBtn = new JButton("Daily Sales");
        dailyBtn.addActionListener(e -> loadDailySales());
        
        topPanel.add(salesBtn);
        topPanel.add(productsBtn);
        topPanel.add(dailyBtn);
        
        // Table for reports
        String[] columns = {"Type", "Details", "Value"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load initial report
        loadSalesReport();
    }
    
    private void loadSalesReport() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total sales
            String sql = "SELECT COUNT(*) as count, SUM(total_amount) as total FROM bills";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                double total = rs.getDouble("total");
                
                tableModel.addRow(new Object[]{"Total Bills", "Number of bills", count});
                tableModel.addRow(new Object[]{"Total Sales", "Revenue", String.format("$%.2f", total)});
                tableModel.addRow(new Object[]{"Average Bill", "Per transaction", 
                    count > 0 ? String.format("$%.2f", total/count) : "$0.00"});
            }
            
            // Today's sales
            sql = "SELECT COUNT(*) as count, SUM(total_amount) as total " +
                  "FROM bills WHERE DATE(bill_date) = CURDATE()";
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int todayCount = rs.getInt("count");
                double todayTotal = rs.getDouble("total");
                
                tableModel.addRow(new Object[]{"Today's Bills", "Number of bills", todayCount});
                tableModel.addRow(new Object[]{"Today's Sales", "Revenue", String.format("$%.2f", todayTotal)});
            }
            
            // Top 5 products
            tableModel.addRow(new Object[]{"---", "Top Products", "---"});
            
            sql = "SELECT p.name, COALESCE(SUM(bi.quantity), 0) as sold " +
                  "FROM products p " +
                  "LEFT JOIN bill_items bi ON p.name = bi.product_name " +
                  "GROUP BY p.name ORDER BY sold DESC LIMIT 5";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    "Product", 
                    rs.getString("name"), 
                    rs.getInt("sold") + " sold"
                });
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            tableModel.addRow(new Object[]{"Error", e.getMessage(), ""});
        }
    }
    
    private void loadProductsReport() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DBConnection.getConnection()) {
            // Product summary
            String sql = "SELECT COUNT(*) as count, SUM(quantity) as stock, " +
                        "SUM(price * quantity) as value FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                int stock = rs.getInt("stock");
                double value = rs.getDouble("value");
                
                tableModel.addRow(new Object[]{"Total Products", "Different items", count});
                tableModel.addRow(new Object[]{"Total Stock", "Units available", stock});
                tableModel.addRow(new Object[]{"Inventory Value", "At current prices", String.format("$%.2f", value)});
            }
            
            // Low stock items
            sql = "SELECT name, quantity, price FROM products WHERE quantity <= 10 ORDER BY quantity";
            rs = stmt.executeQuery(sql);
            
            tableModel.addRow(new Object[]{"---", "Low Stock Items (≤10)", "---"});
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    "Low Stock", 
                    rs.getString("name"), 
                    rs.getInt("quantity") + " left ($" + rs.getDouble("price") + ")"
                });
            }
            
            // Out of stock items
            sql = "SELECT name, price FROM products WHERE quantity = 0";
            rs = stmt.executeQuery(sql);
            
            int outOfStock = 0;
            while (rs.next()) {
                outOfStock++;
                tableModel.addRow(new Object[]{
                    "Out of Stock", 
                    rs.getString("name"), 
                    "Restock needed ($" + rs.getDouble("price") + ")"
                });
            }
            
            if (outOfStock == 0) {
                tableModel.addRow(new Object[]{"Out of Stock", "All items in stock", "✓"});
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            tableModel.addRow(new Object[]{"Error", e.getMessage(), ""});
        }
    }
    
    private void loadDailySales() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT DATE(bill_date) as date, " +
                        "COUNT(*) as bills, " +
                        "SUM(total_amount) as sales " +
                        "FROM bills " +
                        "GROUP BY DATE(bill_date) " +
                        "ORDER BY date DESC LIMIT 7";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            tableModel.addRow(new Object[]{"Date", "Bills", "Sales"});
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getDate("date"),
                    rs.getInt("bills"),
                    String.format("$%.2f", rs.getDouble("sales"))
                });
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            tableModel.addRow(new Object[]{"Error", e.getMessage(), ""});
        }
    }
}