import java.sql.*;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        
        try {
            // Load MySQL Driver
            System.out.println("1. Loading MySQL driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL driver loaded successfully");
            
            // Test connection
            System.out.println("2. Attempting connection...");
            String url = "jdbc:mysql://localhost:3306/simple_shop";
            String username = "root";
            String password = "bishal1212";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Database connected successfully!");
            
            // Test query
            System.out.println("3. Testing query...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            
            System.out.println("Tables in database:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("✓ All tests passed!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("✗ MySQL Driver not found!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Make sure MySQL Connector JAR is added to project.");
        } catch (SQLException e) {
            System.out.println("✗ Database connection failed!");
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
        } catch (Exception e) {
            System.out.println("✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
