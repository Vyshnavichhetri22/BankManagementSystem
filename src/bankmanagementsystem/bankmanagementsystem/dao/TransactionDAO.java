package bankmanagementsystem.dao;

import bankmanagementsystem.ConnectionProvider;
import bankmanagementsystem.entities.Transaction;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    // --- NEW SQL QUERY (Reusable for all enhanced logging) ---
    private static final String INSERT_ENHANCED_LOG = "INSERT INTO transactions " +
        "(account_number, amount, transaction_type, recipient_account, depositor_name, " +
        "depositor_phone, status, cheque_number, issuing_bank, executed_by_teller_id, transaction_time) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

    /**
     * Records a deposit with full enhanced details and returns the DB-generated transaction ID.
     */
    public int recordDepositTransaction(Connection con, String accNumber, BigDecimal amount, String type, String depositorName, String depositorPhone, String transactionStatus, String chequeNo, String issuingBank, String tellerId) throws SQLException {
        String query = INSERT_ENHANCED_LOG;
        int transactionId = 0;

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, accNumber);
            pst.setBigDecimal(2, amount.abs());
            pst.setString(3, type);
            pst.setNull(4, java.sql.Types.VARCHAR); // recipient_account is NULL for deposits
            pst.setString(5, depositorName);
            pst.setString(6, depositorPhone);
            pst.setString(7, transactionStatus);
            pst.setString(8, chequeNo);
            pst.setString(9, issuingBank);
            pst.setString(10, tellerId);

            if (pst.executeUpdate() > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        transactionId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("DB Error recording ENHANCED deposit: " + e.getMessage());
            throw e;
        }
        return transactionId;
    }

    /**
     * Records a standard transaction (like a Simple Debit/Credit).
     */
    public boolean recordTransaction(String accNumber, BigDecimal amount, String type, String recipientAcc, String tellerId) {
        String query = INSERT_ENHANCED_LOG;
        Connection con = ConnectionProvider.getCon();

        if (con == null) return false;

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, accNumber);
            pst.setBigDecimal(2, amount.abs());
            pst.setString(3, type);
            pst.setString(4, recipientAcc);

            // Default/Null values for non-transfer fields
            pst.setNull(5, java.sql.Types.VARCHAR); // depositor_name
            pst.setNull(6, java.sql.Types.VARCHAR); // depositor_phone
            pst.setString(7, "Completed");          // status
            pst.setNull(8, java.sql.Types.VARCHAR); // cheque_number
            pst.setNull(9, java.sql.Types.VARCHAR); // issuing_bank

            // Audit Field (MANDATORY)
            pst.setString(10, tellerId);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DB Error recording general transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { /* Log close error */ }
        }
    }
    /**
     * Retrieves the most recent transaction history for a given account.
     */
    // Paste this into your clean text editor.
    public List<Transaction> getRecentTransactions(String accNumber, int limit) {
        List<Transaction> transactions = new ArrayList<>();
    
        String query = "SELECT transaction_id, account_number, transaction_time, amount, transaction_type, recipient_account, deposit_mode" + 
                       " FROM transactions WHERE account_number = ? OR recipient_account = ? " + 
                       "ORDER BY transaction_time DESC LIMIT ?";

        try (java.sql.Connection con = bankmanagementsystem.ConnectionProvider.getCon();
             java.sql.PreparedStatement pst = con.prepareStatement(query)) {
    
            pst.setString(1, accNumber);
            pst.setString(2, accNumber);
            pst.setInt(3, limit);
    
            try (java.sql.ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    
                    int transactionId = rs.getInt("transaction_id");
                    String accountNumber = rs.getString("account_number");
                    java.sql.Timestamp sqlTimestamp = rs.getTimestamp(3); //"transaction_time" 
                    java.time.LocalDateTime localDateTime = (sqlTimestamp != null) ? sqlTimestamp.toLocalDateTime() : null;
                    
                     String type = rs.getString("transaction_type");
                    java.math.BigDecimal amount = rs.getBigDecimal("amount");
                    // NEW LOGIC TO SIGN THE AMOUNT CORRECTLY FOR THE STATEMENT:
                    //if (type != null && type.contains("Debit")) { // Check if it's a Debit/Withdrawal/Transfer-OUT
                    if (type != null && (type.contains("Withdrawal") || type.contains("Debit"))) {
                        // This transaction reduces the balance of the statement account.
                        amount = amount.abs().negate(); //make negative
                    } else if (type != null && (type.contains("Deposit") || type.contains("Credit"))) {
                        // This transaction increases the balance of the statement account.
                        amount = amount.abs();// keep positive
                    }
                   
                    String relatedAccount = rs.getString("recipient_account");
                    String depositMode = rs.getString("deposit_mode");
    
                    // Constructor Call (MUST match the 6-argument constructor)
                    bankmanagementsystem.entities.Transaction txn = new bankmanagementsystem.entities.Transaction(
                        transactionId,
                        accountNumber,
                        localDateTime,
                        amount,
                        type,
                        relatedAccount,
                        depositMode
                    );
    
                    transactions.add(txn);
                }
            }
    
        } catch (Exception e) {
            System.err.println("DB Error fetching transactions for " + accNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
    
        return transactions;
    }


      
    /**
     * Records a withdrawal into the enhanced transaction log.
     */
    public int recordWithdrawalTransaction(Connection con, String accNumber, BigDecimal amount, String type, String transactionStatus, String tellerId) throws SQLException {
        int transactionId = 0;

        try (PreparedStatement pst = con.prepareStatement(INSERT_ENHANCED_LOG, Statement.RETURN_GENERATED_KEYS)) {
            // Core Data
            pst.setString(1, accNumber);                               
            pst.setBigDecimal(2, amount.abs().negate());               
            pst.setString(3, type);                                    

            // Withdrawal Specifics (Setting unused fields to NULL)
            pst.setNull(4, java.sql.Types.VARCHAR);                    
            pst.setNull(5, java.sql.Types.VARCHAR);                    
            pst.setNull(6, java.sql.Types.VARCHAR);                    
            
            // Status and Meta
            pst.setString(7, transactionStatus);                       
            pst.setNull(8, java.sql.Types.VARCHAR);                    
            pst.setNull(9, java.sql.Types.VARCHAR);                    

            // MANDATORY: The Teller ID for your Manager Audit Log
            pst.setString(10, tellerId);                               

            if (pst.executeUpdate() > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) transactionId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Logging Error: " + e.getMessage());
            throw e; 
        }
        return transactionId;
    }

    /**
     * Records a transfer transaction (Debit log for source account) and returns the transaction ID.
     * @return The transaction ID (int) on success, or 0 on failure.
     */
    public int recordTransferTransaction(Connection con, String sourceAcc, String targetAcc, BigDecimal amount, String status) throws SQLException {
        int transactionId = 0;
        String query = INSERT_ENHANCED_LOG;

        try (PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // BINDINGS FOR INSERT_ENHANCED_LOG (10 placeholders)
            pst.setString(1, sourceAcc);
            pst.setBigDecimal(2, amount.abs().negate()); // Debit amount (Negative)
            pst.setString(3, "Transfer-Debit"); // Transaction Type
            pst.setString(4, targetAcc); // Recipient Account

            // Set all unused deposit/cheque fields to NULL
            pst.setNull(5, java.sql.Types.VARCHAR); // depositor_name
            pst.setNull(6, java.sql.Types.VARCHAR); // depositor_phone
            pst.setString(7, status); // Status (e.g., Completed)
            pst.setNull(8, java.sql.Types.VARCHAR); // cheque_number
            pst.setNull(9, java.sql.Types.VARCHAR); // issuing_bank

            // IMPORTANT: Setting this to NULL because the method signature doesn't pass tellerId.
            // If executed_by_teller_id is NOT NULL in DB, this will cause an error!
            pst.setNull(10, java.sql.Types.VARCHAR); // executed_by_teller_id

            if (pst.executeUpdate() > 0) {
                // Retrieve the generated ID
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        transactionId = rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("DB Error recording transfer: " + e.getMessage());
            throw e;
        }
        return transactionId;
    }
    

    /**
     * Executes the DEBIT (TRANSFER_OUT) leg of a fund transfer.
     * * @param con The active connection from the service layer.
     * @return The generated transaction_id for the debit record.
     */
    
    public int recordTransferDebit(Connection con, String sourceAcc, String targetAcc, BigDecimal amount, String transferDebit, String completed, String tellerId) throws SQLException {
        int generatedDebitId = -1;

        // --- STEP 1: RECORD THE LOG ONLY (The "Update Balance" code was REMOVED) ---
        String insertTransactionQuery = "INSERT INTO Transactions ("
            + "account_number, transaction_type, status, amount, depositor_name, depositor_phone, "
            + "deposit_mode, recipient_account, executed_by_teller_id, transaction_time"
            + ") VALUES (?, ?, ?, ?, NULL, NULL, 'Transfer', ?, ?, NOW())";

        try (PreparedStatement pstInsert = con.prepareStatement(insertTransactionQuery, 
                                                               Statement.RETURN_GENERATED_KEYS)) {

            pstInsert.setString(1, sourceAcc);
            pstInsert.setString(2, transferDebit);  
            pstInsert.setString(3, completed);      
            pstInsert.setBigDecimal(4, amount.abs().negate()); // Logs as negative for the sender
            pstInsert.setString(5, targetAcc);      
            pstInsert.setString(6, tellerId);

            pstInsert.executeUpdate();

            try (ResultSet rs = pstInsert.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedDebitId = rs.getInt(1);
                }
            }
        }
        return generatedDebitId;
    }
    
    public int recordTransferCredit(Connection con, String targetAcc, String sourceAcc, BigDecimal amount, String transferCredit, String completed, String tellerId) throws SQLException {
        int generatedCreditId = -1;

        // --- STEP 1: RECORD THE LOG ONLY (The "Update Balance" code was REMOVED) ---
        String insertTransactionQuery = "INSERT INTO Transactions ("
            + "account_number, transaction_type, status, amount, depositor_name, depositor_phone, "
            + "deposit_mode, recipient_account, executed_by_teller_id, transaction_time"
            + ") VALUES (?, ?, ?, ?, NULL, NULL, 'Transfer', ?, ?, NOW())";

        try (PreparedStatement pstInsert = con.prepareStatement(insertTransactionQuery, 
                                                               Statement.RETURN_GENERATED_KEYS)) {

            pstInsert.setString(1, targetAcc);
            pstInsert.setString(2, transferCredit); 
            pstInsert.setString(3, completed);      
            pstInsert.setBigDecimal(4, amount.abs()); // Logs as positive for the receiver
            pstInsert.setString(5, sourceAcc);      
            pstInsert.setString(6, tellerId);

            pstInsert.executeUpdate();

            try (ResultSet rs = pstInsert.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedCreditId = rs.getInt(1);
                }
            }
        }
        return generatedCreditId;
    }
    
    public java.math.BigDecimal getCurrentBalance(String accNumber) {
        // NOTE: This SQL assumes you have an 'accounts' table with a 'current_balance' column.
        // Adjust the table/column names to match your database schema.
        String query = "SELECT balance FROM accounts WHERE account_number = ?";

        try (java.sql.Connection con = bankmanagementsystem.ConnectionProvider.getCon();
             java.sql.PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, accNumber);

            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }
        } catch (Exception e) {
            System.err.println("DB Error fetching current balance for " + accNumber + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Return null if balance could not be found or an error occurred
        return null; 
    }
}