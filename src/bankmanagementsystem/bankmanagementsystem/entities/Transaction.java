package bankmanagementsystem.entities;

import java.math.BigDecimal; 
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Transaction {
    
    private int transactionId;
    private String accountNumber;
    private java.time.LocalDateTime localDateTime;
    private String type; // e.g., 'Deposit', 'Withdrawal', 'Transfer'
    private String description;
    private BigDecimal amount;
    private BigDecimal finalBalance; // Balance after this transaction
    private String relatedAccount; // For transfers
    
    // --- NEW FIELDS FOR ENHANCED DEPOSIT (History View Fix) ---
    private String depositorName;
    private String depositorPhone;
    private String status; // e.g., 'Completed', 'Pending Clearance'
    private String depositMode;
    // =================================================================
    // 1. CONSTRUCTORS
    // =================================================================

    /**
     * Default (No-Argument) Constructor. (CRITICAL FIX)
     */
   
    // Original All-Argument Constructor (Retained for compatibility)
    public Transaction(int transactionId, String accountNumber,  LocalDateTime localDateTime , String description, BigDecimal finalBalance, java.math.BigDecimal amount, String type,  String relatedAccount , String depositMode) {
        
        // Assign ALL input parameters correctly:
        this.transactionId = transactionId;     // <-- Use the input ID
        this.accountNumber = accountNumber;     // <-- Use the input Account Number
        this.localDateTime = localDateTime; // <-- Use the input Time
        this.description = description;         // <-- Use the input Description
        this.finalBalance = finalBalance;       // <-- Use the input Final Balance

        // Assign the remaining inputs
        this.amount = amount;
        this.type = type;
        this.relatedAccount = relatedAccount;
        this.depositMode = depositMode;
   }

    public Transaction(int transactionId, String accountNumber, LocalDateTime localDateTime, BigDecimal amount, String type, String relatedAccount, String depositMode) {
       // Assign the 7 values passed in by the DAO
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.localDateTime = localDateTime;
        this.amount = amount;
        this.type = type;
        this.relatedAccount = relatedAccount; 
        this.depositMode = depositMode;

        // Set safe defaults for the 2 fields not supplied (description and finalBalance)
        this.description = "Transaction Statement Entry"; 
        this.finalBalance = null;
    }

    // =================================================================
    // 2. SETTERS (CRITICAL FIX: Added for DAO mapping)
    // =================================================================
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.localDateTime = transactionTime; }
    public void setTransactionType(String type) { this.type = type; } // Note: DAO uses setType
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setFinalBalance(BigDecimal finalBalance) { this.finalBalance = finalBalance; }
    public void setRelatedAccount(String relatedAccount) { this.relatedAccount = relatedAccount; } // Note: DAO uses setRelatedAccount
    
    // --- NEW SETTERS ---
    public void setDepositorName(String depositorName) { this.depositorName = depositorName; }
    public void setDepositorPhone(String depositorPhone) { this.depositorPhone = depositorPhone; }
    public void setStatus(String status) { this.status = status; }
    public void setDepositMode(String depositMode) {this.depositMode = depositMode ;}


    // =================================================================
    // 3. GETTERS
    // =================================================================
    public int getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public java.time.LocalDateTime getTransactionTime() { return localDateTime; }
    public String getTransactionType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFinalBalance() { return finalBalance; }
    public String getRelatedAccount() { return relatedAccount; }
    
    // --- NEW GETTERS (For History View) ---
    public String getDepositorName() { return depositorName; }
    public String getDepositorPhone() { return depositorPhone; }
    public String getStatus() { return status; }
    public String getDepositMode() { return depositMode; }
    
}