package bankmanagementsystem.entities;

public class Teller {

    private String tellerId;
    private String fullName;
    private String branchCode;
    private String role; // e.g., 'Teller', 'Senior Teller', 'Manager'
    private String status; // 'Active', 'Suspended'
    private String hashedPin; // The securely hashed transaction PIN
    private String mobileNumber;
    private String dateHired;

  
    // Constructor for DAO Retrieval (Basic Authentication)
    public Teller(String tellerId, String fullName, String hashedPin, String status, String dateHired) {
        this.tellerId = tellerId;
        this.fullName = fullName;
        this.hashedPin = hashedPin;
        this.status = status;
        this.dateHired = dateHired;
        this.branchCode = null;
        this.role = null;
        this.mobileNumber = null;
    }

    // --- Getters ---
    public String getTellerId() { return tellerId; }
    public String getFullName() { return fullName; }
    public String getBranchCode() { return branchCode; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getHashedPin() { return hashedPin; }
    public String getMobileNumber() { return mobileNumber; }
    public String getDateHired() { return dateHired; }

    // --- Setters ---
    public void setTellerId(String tellerId) { this.tellerId = tellerId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setHashedPin(String hashedPin) { this.hashedPin = hashedPin; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
}