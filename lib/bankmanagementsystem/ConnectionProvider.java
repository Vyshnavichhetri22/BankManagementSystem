/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bankmanagementsystem;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 *
 * @author dell
 */
public class ConnectionProvider {
    
    // 1. Define final variables for connection details
    private static final String URL = "jdbc:mysql://localhost:3306/BankDB"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    public static Connection getCon() {
        try {
            // 2. Load the driver (same as your main method)
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            
            // 3. Return the active connection object
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            return con;
        } catch (Exception e) {
            // Log the error if connection fails
            e.printStackTrace();
            return null; 
        }
    }
}
    

