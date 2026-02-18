package bankmanagementsystem.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Entity class representing a single bank account and its linked customer details.
 * Contains all fields necessary for both transactions and customer profile forms.
 */
public class Account {

    // Core Financial & Identification
    private int accountId;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType; // e.g., 'Saving Account', 'Current Account'
    private String status; // 'Active', 'Inactive', 'Closed' (CRITICAL for Soft Delete)
    private String currency;

    // Personal & Contact Details (Linked to Customer)
    private String fullName;
    private String email;
    private String mobileNo;
    private String dob; 
    private String gender;
    private String maritalStatus;
    
    // Address Details
    private String streetAddress;
    private String district;
    private String province;
    
    // KYC and Documentation
    private String citizenshipId;
    private String annualIncome;
    private String occupation;
    private boolean isMinor;
    private String guardianName;
    private String photoPath;
    private String signaturePath;
    
    private String loanType, loanStatus, loanAppliedDate;
    private double loanAmount, outstandingBalance, emi;
    
    private String tellerId = "T-452";
    
    // Audit Fields
    private String openedByUserId;
    private Timestamp creationTime;
    
    // =================================================================
    // 1. CONSTRUCTORS
    // =================================================================

    /**
     * Default (No-Argument) Constructor. Used when retrieving data via setters (like in getAllAccounts).
     */
    public Account() {}

    /**
     * Constructor for basic account retrieval (used by getAccountByNumber).
     */
    public Account(String accountNumber, String accountType, BigDecimal balance, String status, String currency) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.currency = currency;
    }

    // =================================================================
    // 2. SETTERS 
    // =================================================================
    // --- Core Financial & Identification Setters ---
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setStatus(String status) { this.status = status; }
    public void setCurrency(String currency) { this.currency = currency; }

    // --- Personal & Contact Details Setters ---
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
        
    // --- Address Details Setters ---
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public void setDistrict(String district) { this.district = district; }
    public void setProvince(String province) { this.province = province; }
        
    // --- KYC and Documentation Setters ---
    public void setCitizenshipId(String citizenshipId) { this.citizenshipId = citizenshipId; }
    public void setAnnualIncome(String annualIncome) { this.annualIncome = annualIncome; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public void setIsMinor(boolean isMinor) { this.isMinor = isMinor; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }
        
    public void setTellerId(String id) {
        this.tellerId = id;
    }
    // --- Audit Fields Setters ---
    public void setOpenedByUserId(String openedByUserId) { this.openedByUserId = openedByUserId; }
    public void setCreationTime(Timestamp creationTime) { this.creationTime = creationTime; }

    // =================================================================
    // 3. GETTERS 
    // =================================================================
    // --- Core Getters ---
    public int getAccountId() { return accountId; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public String getStatus() { return status; }
    public String getCurrency() { return currency; }
    public Timestamp getCreationTime() { return creationTime; }
    public String getOpenedByUserId() { return openedByUserId; }
    
    // --- Personal/Customer Getters ---
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobileNo() { return mobileNo; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getMaritalStatus() { return maritalStatus; }
    
    // --- Address Getters ---
    public String getStreetAddress() { return streetAddress; }
    public String getDistrict() { return district; }
    public String getProvince() { return province; }
    
    // --- KYC Getters ---
    public String getCitizenshipId() { return citizenshipId; }
    public String getAnnualIncome() { return annualIncome; }
    public String getOccupation() { return occupation; }
    public boolean isIsMinor() { return isMinor; }
    public String getGuardianName() { return guardianName; }
    public String getPhotoPath() { return photoPath; }
    public String getSignaturePath() { return signaturePath; }
    
    
    // --- ADD THESE GETTERS AND SETTERS FOR LOAN ---

    public double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(double loanAmount) { this.loanAmount = loanAmount; }

    public double getOutstandingEmi() { return outstandingBalance; }
    public void setOutstandingEmi(double outstandingEmi) { this.outstandingBalance = outstandingEmi; }

    public double getEmi() { return emi; }
    public void setEmi(double emi) { this.emi = emi; }
    
    public String getloanType() { return loanType; }
    public void setloanType(String loanType) { this.loanType = loanType; }
    
    public String getloanStatus() { return loanStatus; }
    public void setloanStatus(String loanStatus) { this.loanStatus = loanStatus; }
    
    public String getloanAppliedDate() { return loanAppliedDate; }
    public void setloanAppliedDate(String loanAppliedDate) { this.loanAppliedDate = loanAppliedDate; }
    
}
