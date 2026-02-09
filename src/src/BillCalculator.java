package src;

// BillCalculator.java
import java.util.*;
import java.text.SimpleDateFormat;

public class BillCalculator {
    
    // Calculate total for a list of items
    public static double calculateTotal(List<BillItem> items) {
        double total = 0;
        for (BillItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }
    
    // Calculate tax
    public static double calculateTax(double subtotal, double taxRate) {
        return subtotal * (taxRate / 100);
    }
    
    // Calculate discount
    public static double calculateDiscount(double subtotal, double discountPercent) {
        return subtotal * (discountPercent / 100);
    }
    
    // Calculate final amount after tax and discount
    public static double calculateFinalAmount(double subtotal, double taxRate, double discountPercent) {
        double tax = calculateTax(subtotal, taxRate);
        double discount = calculateDiscount(subtotal, discountPercent);
        return subtotal + tax - discount;
    }
    
    // Generate bill summary
    public static String generateBillSummary(String customerName, List<BillItem> items, 
                                           double taxRate, double discountPercent) {
        StringBuilder bill = new StringBuilder();
        
        double subtotal = calculateTotal(items);
        double tax = calculateTax(subtotal, taxRate);
        double discount = calculateDiscount(subtotal, discountPercent);
        double finalAmount = calculateFinalAmount(subtotal, taxRate, discountPercent);
        
        bill.append("===================================\n");
        bill.append("           INVOICE\n");
        bill.append("===================================\n");
        bill.append("Customer: ").append(customerName).append("\n");
        bill.append("Date: ").append(new Date()).append("\n");
        bill.append("-----------------------------------\n");
        bill.append(String.format("%-20s %8s %6s %10s\n", 
            "ITEM", "PRICE", "QTY", "SUBTOTAL"));
        bill.append("-----------------------------------\n");
        
        for (BillItem item : items) {
            bill.append(String.format("%-20s %8.2f %6d %10.2f\n",
                item.getName(), item.getPrice(), item.getQuantity(), item.getSubtotal()));
        }
        
        bill.append("-----------------------------------\n");
        bill.append(String.format("%-30s %10.2f\n", "Subtotal:", subtotal));
        bill.append(String.format("%-30s %10.2f\n", "Tax (" + taxRate + "%):", tax));
        bill.append(String.format("%-30s %10.2f\n", "Discount (" + discountPercent + "%):", -discount));
        bill.append("-----------------------------------\n");
        bill.append(String.format("%-30s %10.2f\n", "TOTAL:", finalAmount));
        bill.append("===================================\n");
        bill.append("        THANK YOU!\n");
        bill.append("===================================\n");
        
        return bill.toString();
    }
    
    // Calculate change
    public static double calculateChange(double amountPaid, double totalAmount) {
        return amountPaid - totalAmount;
    }
    
    // Validate payment
    public static boolean validatePayment(double amountPaid, double totalAmount) {
        return amountPaid >= totalAmount;
    }
}

// Item class for bill calculation
class BillItem {
    private String name;
    private double price;
    private int quantity;
    
    public BillItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public double getSubtotal() {
        return price * quantity;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    
    public void setQuantity(int quantity) { this.quantity = quantity; }
}