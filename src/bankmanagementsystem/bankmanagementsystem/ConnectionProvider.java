package bankmanagementsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

// CRITICAL FIX: The class must be PUBLIC to be accessed by other files.
public class ConnectionProvider {
    
    // Define final variables for connection details
    private static final String URL = "jdbc:mysql://localhost:3306/BankDB"; // CHECK YOUR DB NAME!
    private static final String USER = "root";
    private static final String PASSWORD = ""; // CHECK YOUR PASSWORD!

    public static Connection getCon() {
        Connection con = null;
        try {
            // 1. Load the Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. Return the active connection object
            con = DriverManager.getConnection(URL, USER, PASSWORD); 
            return con;
            
        } catch (ClassNotFoundException e) {
            // Error if the MySQL Connector JAR is missing
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found. Ensure Connector JAR is in your project libraries.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            // Error if DB is offline, URL is wrong, or credentials fail
            JOptionPane.showMessageDialog(null, "Database connection failed. Check MySQL server status and DB details.\nDetails: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Database connection failed.");
            e.printStackTrace();
            return null;
        }
    }
    
    //Helper main method to test the connection immediately
    public static void main(String[] args) {
        Connection testCon = getCon();
        if (testCon != null) {
            JOptionPane.showMessageDialog(null, "Database Connection Successful!", "Connection Status", JOptionPane.INFORMATION_MESSAGE);
            try {
                testCon.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
             // getCon() already displays an error dialog
             System.out.println("TEST FAILED: Connection was null.");
        }
    }
}

