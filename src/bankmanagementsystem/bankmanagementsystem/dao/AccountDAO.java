package bankmanagementsystem.dao;

import bankmanagementsystem.ConnectionProvider;
import bankmanagementsystem.entities.Account;
import java.sql.*;
import java.math.BigDecimal;
import javax.swing.JOptionPane;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // Common query fragment for retrieving essential deposit/lookup data
    private static final String LOOKUP_QUERY = 
        "SELECT full_name, father_name, mother_name , phone, guardian_relation, minor_dob_cert, minor_signature, guardian_citizenship_doc, guardian_signature_doc, purpose,branch, initial_deposit_mode, initial_deposit_amount, source_of_funds, services, terms_agreed, balance, status FROM Accounts WHERE account_number = ?";
    
    // Original query for core account check
    private static final String CORE_ACCOUNT_QUERY = 
        "SELECT account_number, account_type, balance, status, currency, application_form_no FROM Accounts WHERE account_number = ?";

    // Comprehensive query for Customer Lookup
    private static final String FULL_LOOKUP_QUERY = 
      "SELECT account_number, account_type, balance, status, currency, full_name, phone, " +
      "email, street_address, district, province FROM Accounts WHERE account_number = ?";
    
    // NEW QUERY: For Admin Dashboard to fetch all accounts (using key dashboard fields)
    private static final String ALL_ACCOUNTS_QUERY = 
      "SELECT account_number, full_name, balance, account_type, status FROM Accounts ORDER BY account_number";
    
    // NEW QUERY: For Soft Delete operation (Admin)
    private static final String CLOSE_ACCOUNT_QUERY = 
        "UPDATE Accounts SET status = 'Closed' WHERE account_number = ?";

    // Helper to get connection (assuming ConnectionProvider is correct)
    private Connection getCon() throws SQLException {
         Connection con = ConnectionProvider.getCon();
         if (con == null) {
            throw new SQLException("Database connection provider returned null.");
         }
         return con;
    }

    // ----------------------------------------------------------------------
    // NEW METHOD: R - READ ALL for Admin Dashboard
    // ----------------------------------------------------------------------
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();

        try (Connection con = getCon();
             PreparedStatement pst = con.prepareStatement(ALL_ACCOUNTS_QUERY);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Account account = new Account();
                account.setAccountNumber(rs.getString("account_number"));
                account.setFullName(rs.getString("full_name"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountType(rs.getString("account_type"));
                account.setStatus(rs.getString("status"));
                
                accounts.add(account);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error fetching all accounts: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return accounts;
    }
    
    // ----------------------------------------------------------------------
    // NEW METHOD: U - UPDATE for Soft Delete (Close Account)
    // ----------------------------------------------------------------------
    public boolean closeAccount(String accountNo) {
        
        try (Connection con = getCon();
             PreparedStatement pst = con.prepareStatement(CLOSE_ACCOUNT_QUERY)) {
            
            pst.setString(1, accountNo);
            int rowsAffected = pst.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "DB Error closing account: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    // ----------------------------------------------------------------------
    // EXISTING METHODS BELOW
    // ----------------------------------------------------------------------
    
    /**
     * Fetches core account details AND customer details for the lookup form.
     */
    public Account getAccountByNumber(String accNum) {
        Account account = null;
        // FIXED: Added commas and 'AS' aliases to separate the two different 'status' columns
        String sql = "SELECT a.*, a.status AS acc_status, " + 
                     "l.loan_type, l.amount, l.outstanding_balance, l.emi, l.applied_date, " + 
                     "l.status AS loan_status " + 
                     "FROM accounts a " + 
                     "LEFT JOIN loans l ON a.account_number = l.account_no " + 
                     "WHERE a.account_number = ?";

        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, accNum);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                account = new Account();
                // Data from accounts table
                account.setAccountNumber(rs.getString("account_number"));
                account.setFullName(rs.getString("full_name"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setPhotoPath(rs.getString("photo_path"));
                account.setDistrict(rs.getString("district"));
                account.setAccountType(rs.getString("account_type"));

                // FIXED: Use the alias 'acc_status' to get the correct status
                account.setStatus(rs.getString("acc_status")); 

                // Data from loans table
                account.setloanType(rs.getString("loan_type"));
                account.setLoanAmount(rs.getDouble("amount"));
                account.setOutstandingEmi(rs.getDouble("outstanding_balance"));
                account.setEmi(rs.getDouble("emi"));
                account.setloanAppliedDate(rs.getString("applied_date"));

                // FIXED: Use the alias 'loan_status' here
                account.setloanStatus(rs.getString("loan_status"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("DB Error fetching full account details: " + e.getMessage());
            e.printStackTrace();
        }
        return account;
    }
      
    public List<Account> getAccountByName(String name) {
        List<Account> list = new ArrayList<>();
        // SQL 'LIKE' makes the search case-insensitive and allows partial matches
        String sql = "SELECT * FROM accounts WHERE full_name LIKE ?";

        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + name + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Account acc = new Account();
                acc.setAccountNumber(rs.getString("account_number"));
                acc.setFullName(rs.getString("full_name"));
                acc.setMobileNo(rs.getString("phone"));
                acc.setEmail(rs.getString("email"));
                acc.setAccountType(rs.getString("account_type"));
                acc.setStreetAddress(rs.getString("street_address"));
                acc.setDistrict(rs.getString("district"));
                acc.setStatus(rs.getString("status"));
                acc.setPhotoPath(rs.getString("photo_path"));
                list.add(acc);
            }
        } catch (SQLException e) {
            System.out.println("Search Error: " + e.getMessage());
        }
        return list;
    }
    
    // For searching multiple accounts by Name
    public List<Account> getAccountsByName(String name) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT account_number, full_name, district FROM accounts WHERE full_name LIKE ?";
        return list;
    }
    
    
    /**
     * Fetches minimal details required for the Deposit Form's dynamic lookup.
     */
    public Map<String, String> getAccountLookupDetails(String accNumber) {
        String query = LOOKUP_QUERY;
        Connection con = null;
        Map<String, String> details = null;

        try {
             con = getCon();
        } catch (SQLException ex) {
            System.err.println("DB Connection failed: " + ex.getMessage());
            return null;
        }

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, accNumber);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    details = new HashMap<>();
                    details.put("full_name", rs.getString("full_name"));
                    details.put("phone", rs.getString("phone"));
                    details.put("status", rs.getString("status"));
                    details.put("balance", rs.getBigDecimal("balance").toPlainString()); 
                }
            }
        } catch (SQLException e) {
            System.err.println("DB Error fetching lookup details: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) {}
        }
        return details;
    }
    
    /**
     * Updates the balance of an account. 
     */
    public boolean updateBalance(Connection con, String accNumber, BigDecimal amount) throws SQLException {
        String query = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
        boolean success = false;

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setBigDecimal(1, amount);
            pst.setString(2, accNumber);

            if (pst.executeUpdate() > 0) {
                success = true;
            }
        } catch (SQLException e) {
            System.err.println("DB Error updating balance for " + accNumber + ": " + e.getMessage());
            throw e; 
        }
        return success;
    }
    
    public Account getBalanceDetails(String accNum) {
        Account acc = null;
        // MAKE SURE 'account_type' IS IN THE SELECT STATEMENT
        String sql = "SELECT account_type, status, balance FROM accounts WHERE account_number = ?";

        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, accNum);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                acc = new Account();
                acc.setAccountType(rs.getString("account_type")); 
                acc.setStatus(rs.getString("status"));
                acc.setBalance(rs.getBigDecimal("balance"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return acc;
    }
}