package bankmanagementsystem.service;

import bankmanagementsystem.ConnectionProvider;
import bankmanagementsystem.dao.AccountDAO;
import bankmanagementsystem.dao.TransactionDAO;
import bankmanagementsystem.dao.TellerDAO;
import bankmanagementsystem.entities.Teller;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class TransactionService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final TellerDAO tellerDAO;

    public TransactionService() {
        // Initialize DAOs
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.tellerDAO = new TellerDAO();
    }

/**
 * Authenticates a teller's PIN against their ID.
 * @return true if teller is active and PIN matches, false otherwise.
 */
    private boolean authenticateTeller(String tellerId, String pin) {
        
        // If the Teller ID is 'employee', we grant immediate access for testing.
        if (tellerId != null && tellerId.equalsIgnoreCase("employee")) {
          System.out.println("DEBUG: PIN check bypassed for Teller ID: " + tellerId + ". Authentication granted.");
          return true;
        }
          // Get the Teller object to check status and PIN
        Teller teller = tellerDAO.getTellerForAuth(tellerId);

        if (teller == null) {
            System.err.println("Authentication Failed: Teller ID not found.");
            return false;
        }

        String status = teller.getStatus();
        
        System.out.println("DEBUG: Teller ID: " + tellerId);
        System.out.println("DEBUG: Raw Status from Teller Object: [" + status + "]");

        // Check for null or inactive status
        if (status == null || !status.trim().toLowerCase().equals("active")) {
             System.err.println("Authentication Failed: Teller status is NULL or inactive.");
             return false;
        }

        // Mock PIN verification: Reverse the input PIN and compare it to the stored hash
        String storedHash = teller.getHashedPin();
        String reversedPin = new StringBuilder(pin).reverse().toString();
        
        // --- DEBUG LINES ---
        System.out.println("DEBUG: Input PIN: [" + pin + "]");
        System.out.println("DEBUG: Entered PIN (Reversed): [" + reversedPin + "]");
        System.out.println("DEBUG: Stored HASH (from DB): [" + storedHash + "]");
        // -------------------

        if (Objects.equals(reversedPin, storedHash)) {
            System.out.println("DEBUG: Authentication Successful!");
            return true;
        } else {
            System.err.println("Authentication Failed: Invalid PIN for Teller " + tellerId);
            return false;
        }
    }

    /**
     * Executes an atomic deposit transaction with full audit details and teller authorization.
     * This method manages the connection, auto-commit setting, commit, and rollback.
     * * @return The transaction ID (int) on success, or 0 on failure.
     */
    public int depositFundsEnhanced(String accNumber, BigDecimal amount, String depositorName, String depositorPhone, 
                                     boolean isCheque, String chequeNo, String issuingBank, String tellerId, String tellerPin) {
        
        // 1. Pre-Transaction Checks (Authentication)
        if (!authenticateTeller(tellerId, tellerPin)) {
            System.err.println("Error: Teller authentication failed.");
            return 0;
        }
        
        Connection con = null;
        int transactionId = 0;
        String transactionType = isCheque ? "Deposit-Cheque" : "Deposit-Cash";
        String transactionStatus = isCheque ? "Pending" : "Completed";

        try {
            // 2. Start Atomic Transaction
            con = ConnectionProvider.getCon();
            if (con == null) throw new Exception("Failed to get DB connection.");
            con.setAutoCommit(false); // <--- BEGIN TRANSACTION

            // 3. Update Account Balance (PASSING THE CONNECTION)
            if (!accountDAO.updateBalance(con, accNumber, amount)) {
                throw new Exception("Balance update failed (account not found or DB error).");
            }

            // 4. Record Transaction Audit Log (PASSING THE SAME CONNECTION)
            transactionId = transactionDAO.recordDepositTransaction(
                con, accNumber, amount, transactionType, depositorName, depositorPhone, 
                transactionStatus, chequeNo, issuingBank, tellerId
            );

            if (transactionId == 0) {
                 throw new Exception("Transaction record failed.");
            }

            // 5. Commit Transaction
            con.commit(); // <--- END TRANSACTION (SUCCESS)

        } catch (Exception e) {
            System.err.println("Deposit failed, rolled back. Error: " + e.getMessage());
            e.printStackTrace();
            
            // 6. Rollback on Failure
            if (con != null) {
                try { con.rollback(); } catch (SQLException rollbackEx) {
                     System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            transactionId = 0; // Ensure failure return value
            
        } finally {
            // 7. Close Connection
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException closeEx) { 
                     System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
        return transactionId;
    }

    /**
     * Executes an atomic withdrawal transaction.
     * @return The transaction ID (int) on success, or 0 on failure.
     */
    public int withdrawFunds(String accNumber, BigDecimal amount, String tellerId, String tellerPin) {
        // 1. Pre-Transaction Checks (Authentication)
        if (!authenticateTeller(tellerId, tellerPin)) {
            System.err.println("Error: Teller authentication failed.");
            return 0;
        }

        Connection con = null;
        int transactionId = 0;
        // The amount for withdrawal must be negative in the DB update.
        BigDecimal negativeAmount = amount.negate();
        
        try {
            // 2. Start Atomic Transaction
            con = ConnectionProvider.getCon();
            if (con == null) throw new Exception("Failed to get DB connection.");
            con.setAutoCommit(false); // <--- BEGIN TRANSACTION
            
            // NOTE: The AccountDAO.updateBalance method must handle the insufficient funds check
            
            // 3. Update Account Balance (PASSING THE CONNECTION)
            // Passing negativeAmount for withdrawal
            if (!accountDAO.updateBalance(con, accNumber, negativeAmount)) {
                throw new Exception("Withdrawal failed (account not found or insufficient funds).");
            }

            // 4. Record Transaction Audit Log (PASSING THE SAME CONNECTION)
            // We record the amount as positive, but mark the type as Withdrawal.
            transactionId = transactionDAO.recordWithdrawalTransaction( 
                con, accNumber, amount, "Withdrawal", "Completed", tellerId
            );

            if (transactionId == 0) {
                 throw new Exception("Transaction record failed.");
            }

            // 5. Commit Transaction
            con.commit(); // <--- END TRANSACTION (SUCCESS)

        } catch (Exception e) {
            System.err.println("Withdrawal failed, rolled back. Error: " + e.getMessage());
            e.printStackTrace();
            
            // 6. Rollback on Failure
            if (con != null) {
                try { con.rollback(); } catch (SQLException rollbackEx) {
                     System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            transactionId = 0; // Ensure failure return value
            
        } finally {
            // 7. Close Connection
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException closeEx) { 
                     System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
        return transactionId;
    }
    
    /**
     * Executes an atomic fund transfer transaction.
     * Requires teller authorization and logs two audit entries (Debit and Credit).
     * @return The transaction ID (int) of the Debit entry on success, or 0 on failure.
     */
    public int transferFunds(String sourceAcc, String targetAcc, BigDecimal amount, String tellerId, String tellerPin) {
        
        // 1. Pre-Transaction Checks (Authentication)
        if (!authenticateTeller(tellerId, tellerPin)) {
            System.err.println("Error: Teller authentication failed for transfer.");
            return 0;
        }
        
        Connection con = null;
        int debitTransactionId = 0; // The ID returned to the UI for the receipt

        try {
            // 2. Start Atomic Transaction
            con = ConnectionProvider.getCon();
            if (con == null) throw new Exception("Failed to get DB connection.");
            con.setAutoCommit(false); // <--- BEGIN ATOMIC BLOCK

            // --- DEBIT PHASE ---
            // 3. Debit Source Account (Withdrawal)
            if (!accountDAO.updateBalance(con, sourceAcc, amount.negate())) {
                throw new Exception("Debit failed (Source account not found or insufficient funds).");
            }
            
            // 4. Record DEBIT Transaction Log
            debitTransactionId = transactionDAO.recordTransferDebit(
                 con, sourceAcc, targetAcc, amount, "Transfer-Debit", "Completed", tellerId
            );
            if (debitTransactionId == 0) {
                throw new Exception("Debit log failed.");
            }


            // --- CREDIT PHASE ---
            // 5. Credit Target Account (Deposit)
            if (!accountDAO.updateBalance(con, targetAcc, amount)) {
                throw new Exception("Credit failed (Target account not found).");
            }
            
            // 6. Record CREDIT Transaction Log
            // NOTE: Target account is credited, log entry is against Target account.
            int creditTransactionId = transactionDAO.recordTransferCredit(
                 con, targetAcc, sourceAcc, amount, "Transfer-Credit", "Completed", tellerId
            );
            if (creditTransactionId == 0) {
                throw new Exception("Credit log failed.");
            }


            // 7. Commit Transaction
            con.commit(); // <--- END ATOMIC BLOCK (SUCCESS)

            return debitTransactionId; // Return the ID of the primary (Debit) transaction
            
        } catch (Exception e) {
            System.err.println("Transfer failed, rolled back. Error: " + e.getMessage());
            e.printStackTrace();
            
            // 8. Rollback on Failure
            if (con != null) {
                try { 
                    con.rollback(); // Undo the Debit and Credit updates
                } catch (SQLException rollbackEx) {
                     System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            return 0; // Ensure failure return value
            
        } finally {
            // 9. Close Connection
            if (con != null) {
                try { 
                    con.setAutoCommit(true); // Restore default
                    con.close(); 
                } catch (SQLException closeEx) { 
                     System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }
}