package bankmanagementsystem.dao;

import bankmanagementsystem.ConnectionProvider;
import bankmanagementsystem.entities.LoanApplication;
import java.sql.*;
import java.math.BigDecimal;


public class LoanDAO {
    public boolean submitApplication(LoanApplication loan) {

        // 1. The main loan insertion (Fixed the ? count to 13)
        String sqlLoan = "INSERT INTO loans (application_no, account_no, loan_type, amount, emi, interest_rate, tenure_years, university_name, property_address, kyc_doc_path, income_doc_path, applied_by, loan_status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        // 2. The Transaction Log insertion (To show up on Manager Dashboard)
        String sqlTxn = "INSERT INTO transactions (account_number, transaction_type, amount, status, executed_by_teller_id, transaction_time) VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection con = ConnectionProvider.getCon()) {
            con.setAutoCommit(false); // Use a transaction so both succeed or both fail

            // Part A: Save the Loan Application
            try (PreparedStatement pstLoan = con.prepareStatement(sqlLoan)) {
                pstLoan.setString(1, loan.appId);
                pstLoan.setString(2, loan.accNo);
                pstLoan.setString(3, loan.type);
                pstLoan.setBigDecimal(4, loan.amount);
                pstLoan.setDouble(5, loan.emi);
                pstLoan.setDouble(6, loan.interestRate);
                pstLoan.setInt(7, loan.tenure);
                pstLoan.setString(8, loan.uniName);
                pstLoan.setString(9, loan.propAddr);
                pstLoan.setString(10, loan.kycPath);
                pstLoan.setString(11, loan.incomePath);
                pstLoan.setString(12, loan.appliedBy);
                pstLoan.setString(13, "PENDING");
                pstLoan.executeUpdate();
            }

            // Part B: Log it as a "Transaction" so the Manager sees it instantly
            try (PreparedStatement pstTxn = con.prepareStatement(sqlTxn)) {
                pstTxn.setString(1, loan.accNo);
                pstTxn.setString(2, "LOAN_APPLICATION");
                pstTxn.setBigDecimal(3, loan.amount);
                pstTxn.setString(4, "Pending");
                pstTxn.setString(5, loan.appliedBy); // The Teller ID
                pstTxn.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

         
    public boolean approveAndDisburse(String appId, String accNo, BigDecimal amount) {
        Connection con = null;
        try {
            con = ConnectionProvider.getCon();
            con.setAutoCommit(false); 

            // 1. Update Loan Status
            String sql1 = "UPDATE loans SET status = 'APPROVED' WHERE application_no = ?";
            PreparedStatement pst1 = con.prepareStatement(sql1);
            pst1.setString(1, appId);
            pst1.executeUpdate();

            // 2. Credit Customer Account
            String sql2 = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement pst2 = con.prepareStatement(sql2);
            pst2.setBigDecimal(1, amount);
            pst2.setString(2, accNo);
            pst2.executeUpdate();

            // 3. Log the Transaction (MATCHED TO YOUR DATABASE COLUMNS)
            // Removed 'description' because your DB doesn't have it
            String sql3 = "INSERT INTO transactions (account_number, transaction_type, amount, executed_by_teller_id, transaction_time) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement pst3 = con.prepareStatement(sql3);
            pst3.setString(1, accNo);
            pst3.setString(2, "LOAN_DISBURSE");
            pst3.setBigDecimal(3, amount);
           
            pst3.setString(4, "SYSTEM_AUTO"); // or use the logged-in manager ID
            pst3.executeUpdate();

            con.commit(); 
            return true;
        } catch (Exception e) {
            try { if(con != null) con.rollback(); } catch(Exception ex) {}
            e.printStackTrace();
            return false;
        }
    }
}