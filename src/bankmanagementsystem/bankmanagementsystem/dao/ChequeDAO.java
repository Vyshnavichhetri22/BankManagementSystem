package bankmanagementsystem.dao;

import bankmanagementsystem.ConnectionProvider;
import java.sql.*;
import java.math.BigDecimal;

public class ChequeDAO {

    // 1. VERIFY: Checks if a cheque is valid and unused
    public boolean verifyChequeStatus(String accountNumber, String chequeNumber) {
        // Corrected column name 'cheque_no' to match your markChequeAsUsed method
        String query = "SELECT status FROM cheque_leaves WHERE account_number = ? AND cheque_no = ?";
        
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, chequeNumber);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return "UNUSED".equalsIgnoreCase(status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. MARK AS USED: Updates the internal cheque book record
    // Added 'con' parameter to participate in the main transaction from DepositForm
    public void markChequeAsUsed(String chequeNumber, int transactionId, Connection con) throws SQLException {
        // We update status AND the link column so it no longer says 'Available'
        String query = "UPDATE cheque_leaves SET status = 'USED', used_in_txn_id = ? WHERE cheque_no = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, transactionId);
            pstmt.setString(2, chequeNumber);
            pstmt.executeUpdate();
        }
    }
        
    // 3. CHECK DUPLICATE: Prevents the same cheque from being entered twice
    public boolean isChequeAlreadyProcessed(String chequeNo) {
        String sql = "SELECT 1 FROM incoming_cheques WHERE cheque_number = ?";
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, chequeNo);
            ResultSet rs = pst.executeQuery();
            return rs.next(); 

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 4. RECORD & SYNC: Records the deposit AND updates the cheque leaf status
    public void recordIncomingCheque(String chequeNo, String accNo, BigDecimal amount, int txnId, Connection con) throws SQLException {
        // Step A: Insert into incoming_cheques log
        String sql = "INSERT INTO incoming_cheques (cheque_number, account_number, amount, transaction_id, status) VALUES (?, ?, ?, ?, 'PROCESSED')";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, chequeNo);
            pst.setString(2, accNo);
            pst.setBigDecimal(3, amount);
            pst.setInt(4, txnId);
            pst.executeUpdate();
        }

        // Step B: IMPORTANT! Also update the cheque_leaves table so it doesn't show 'Available'
        markChequeAsUsed(chequeNo, txnId, con);
    }
    
    public int getTxnIdFromLog(String chequeNo) {
        String sql = "SELECT transaction_id FROM incoming_cheques WHERE cheque_number = ?";
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, chequeNo);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("transaction_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1; // Not found in log
    }
}


