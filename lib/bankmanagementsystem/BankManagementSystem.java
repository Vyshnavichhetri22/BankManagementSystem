/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package bankmanagementsystem;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

/**
 *
 * @author dell
 */
public class BankManagementSystem {
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
  
        try{ 
        
        Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bankDb","root","")) { 
                System.out.println( con + "connected");
            }         
            
        } catch (ClassNotFoundException | SQLException ex ) {
       
            Logger.getLogger(BankManagementSystem.class.getName()).log(Level.SEVERE, null, ex);    
        }
    }
}