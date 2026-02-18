package bankmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import javax.swing.border.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.math.BigDecimal;

public class ManagerLoanDashboard extends JPanel {
   
    private JTable tblLoans;
    private DefaultTableModel model;
    private JLabel lblTotalPending, lblTotalValue;
    private JLabel lblRiskScore;
    private JPanel pnlRiskIndicator;
    private JLabel lblRiskText;

    public ManagerLoanDashboard() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 242, 245));
        
        initUI();
        loadPendingLoans(); // ✅ Now this is visible to the constructor
    }

    private void initUI() {
        // --- 1. Metrics Header ---       
        lblRiskScore = new JLabel("N/A"); // Initialize the global variable
        lblTotalPending = new JLabel("0");
        lblTotalValue = new JLabel("NPR 0.00");

        JPanel header = new JPanel(new GridLayout(1, 3, 20, 0));
        header.setOpaque(false);
        
        header.add(createMetricCard("Pending Applications", lblTotalPending, new Color(52, 152, 219)));
        header.add(createMetricCard("Total Value", lblTotalValue, new Color(155, 89, 182)));
        header.add(createMetricCard("Avg Risk Score", lblRiskScore, new Color(46, 204, 113)));
        
        add(header, BorderLayout.NORTH);

        // --- 2. Table Area ---
        // Added 2 hidden columns for file paths (Index 6 and 7)
        String[] cols = {"App ID", "Account", "Type", "Amount", "Tenure", "Applied By", "KYC_PATH", "INC_PATH"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tblLoans = new JTable(model);
        tblLoans.setRowHeight(40);
        
        // Hide the path columns from the Manager's view
        tblLoans.getColumnModel().getColumn(6).setMinWidth(0);
        tblLoans.getColumnModel().getColumn(6).setMaxWidth(0);
        tblLoans.getColumnModel().getColumn(7).setMinWidth(0);
        tblLoans.getColumnModel().getColumn(7).setMaxWidth(0);
        
        add(new JScrollPane(tblLoans), BorderLayout.CENTER);

        // --- 3. Action Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel sideTitle = new JLabel("DECISION CENTER");
        sideTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        sidebar.add(sideTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton btnViewDocs = new JButton("📂 View Documents");
        btnViewDocs.setMaximumSize(new Dimension(280, 40));
        btnViewDocs.addActionListener(e -> viewCurrentLoanDocs());
        
        JButton btnApprove = new JButton("✅ APPROVE & DISBURSE");
        btnApprove.setMaximumSize(new Dimension(280, 45));
        btnApprove.setBackground(new Color(39, 174, 96));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setFont(new Font("SansSerif", Font.BOLD, 13));
        
        btnApprove.addActionListener(e -> {
            int row = tblLoans.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a loan to approve!");
                return;
            }

            try {
                String appId = tblLoans.getValueAt(row, 0).toString();
                String accNo = tblLoans.getValueAt(row, 1).toString();

                // ✅ FIX: Changed index from 2 to 3 to get the actual Amount
                Object value = tblLoans.getValueAt(row, 3); 
                String rawAmount = value.toString();

                // CLEANING: Remove everything EXCEPT digits and the dot
                String cleanAmount = rawAmount.replaceAll("[^\\d.]", "");

                if (cleanAmount.isEmpty()) {
                    throw new NumberFormatException("Empty amount");
                }

                double amount = Double.parseDouble(cleanAmount);
                // PROCESS: Call your approval method
                processApproval(appId, accNo, amount);

            } catch (NumberFormatException ex) {
                // This happened because you were trying to turn "Personal Loan" into a number!
                JOptionPane.showMessageDialog(this, "Error: Could not read amount. Please check the selected row.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
              
        sidebar.add(btnViewDocs);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnApprove);
        
        JButton btnReject = new JButton("❌ REJECT LOAN");
        btnReject.setMaximumSize(new Dimension(280, 40));
        btnReject.setBackground(new Color(231, 76, 60));
        btnReject.setForeground(Color.WHITE);
        btnReject.addActionListener(e -> processRejection());
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnReject);
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnReject);

        pnlRiskIndicator = new JPanel();
        pnlRiskIndicator.setMaximumSize(new Dimension(280, 45));
        pnlRiskIndicator.setBackground(Color.LIGHT_GRAY);
        lblRiskText = new JLabel("Select a loan to assess risk");
        pnlRiskIndicator.add(lblRiskText);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(pnlRiskIndicator);
        
        add(sidebar, BorderLayout.EAST);
        
        tblLoans.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblLoans.getSelectedRow() != -1) {
                updateRiskIndicator();
            }
        });
    }

    public void loadPendingLoans() {
        model.setRowCount(0); 
        double totalRequested = 0;
        int count = 0;

        // Use a try-with-resources for the Connection and Statement
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM loans WHERE loan_status = 'PENDING'")) {

            while(rs.next()) {
                BigDecimal amt = rs.getBigDecimal("amount");

                // Get values while handling potential NULLs to avoid UI errors
                String appId = rs.getString("application_no");
                String accNo = rs.getString("account_no");
                String type = rs.getString("loan_type");
                String teller = rs.getString("applied_by");
                String kyc = rs.getString("kyc_doc_path");
                String income = rs.getString("income_doc_path");

                model.addRow(new Object[]{
                    appId,
                    accNo,
                    type,
                    amt,
                    rs.getInt("tenure_years") + " Years",
                    (teller == null || teller.isEmpty()) ? "N/A" : teller, // Fix for "Applied By"
                    (kyc == null || kyc.isEmpty()) ? "No File" : kyc,     // Fix for "No Files"
                    (income == null || income.isEmpty()) ? "No File" : income
                });

                totalRequested += (amt != null) ? amt.doubleValue() : 0;
                count++;
            }

            // --- RISK SCORE LOGIC ---
            String grade;
            if (count == 0) grade = "SAFE";
            else if (totalRequested < 500000) grade = "A+"; 
            else if (totalRequested < 2000000) grade = "B+"; 
            else grade = "C (HIGH)"; 

            lblRiskScore.setText(grade);
            lblTotalPending.setText(String.valueOf(count));
            lblTotalValue.setText("NPR " + String.format("%,.2f", totalRequested));

        } catch (Exception e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(null, "Error Loading Dashboard: " + e.getMessage());
        }
    }
        
    private void viewCurrentLoanDocs() {
        int row = tblLoans.getSelectedRow();
        if(row == -1) return;
        
        String kyc = (String) model.getValueAt(row, 6);
        String inc = (String) model.getValueAt(row, 7);
        
        Object[] options = {"KYC", "Income Proof"};
        int n = JOptionPane.showOptionDialog(this, "Select document to view:", "Document Viewer",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        if (n == 0) openFile(kyc);
        else if (n == 1) openFile(inc);
    }

    private void openFile(String path) {
        try {
            if(path == null || path.isEmpty() || path.equals("No File Selected")) {
                JOptionPane.showMessageDialog(this, "No file uploaded.");
                return;
            }
            Desktop.getDesktop().open(new File(path));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening file.");
        }
    }

    private JPanel createMetricCard(String title, JLabel valLabel, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 5, 0, 0, c),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        valLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        p.add(new JLabel(title), BorderLayout.NORTH);
        p.add(valLabel, BorderLayout.CENTER);
        return p;
    }
        
    private void processRejection() {
        int row = tblLoans.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan application to reject.");
            return;
        }

        String appId = model.getValueAt(row, 0).toString();

        //  UX: Ask for a reason
        String reason = JOptionPane.showInputDialog(this, 
            "Enter reason for rejecting Application #" + appId + ":", 
            "Loan Rejection Reason", 
            JOptionPane.PLAIN_MESSAGE);

        // If manager clicked 'OK' and provided a reason (or left it blank)
        if (reason != null) {
            if (reason.trim().isEmpty()) reason = "Documentation incomplete or criteria not met.";

            try (Connection con = ConnectionProvider.getCon()) {
                String sql = "UPDATE loans SET loan_status = 'REJECTED', rejection_reason = ? WHERE application_no = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, reason);
                pst.setString(2, appId);

                int updated = pst.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Loan #" + appId + " has been rejected.");
                    loadPendingLoans(); // Refresh the table
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            }
        }
    }
    
    private void updateRiskIndicator() {
        int row = tblLoans.getSelectedRow();
          // Logic: Get the amount from the table
        BigDecimal amount = (BigDecimal) model.getValueAt(row, 3);

        if (amount.doubleValue() > 1000000) { // Over 10 Lakhs
            pnlRiskIndicator.setBackground(new Color(231, 76, 60)); // Red
            lblRiskText.setText("RISK: HIGH (Requires Collateral Check)");
        } else if (amount.doubleValue() > 300000) {
            pnlRiskIndicator.setBackground(new Color(241, 196, 15)); // Yellow
            lblRiskText.setText("RISK: MEDIUM (Check Income History)");
        } else {
            pnlRiskIndicator.setBackground(new Color(46, 204, 113)); // Green
            lblRiskText.setText("RISK: LOW (Pre-approved Profile)");
        }
        lblRiskText.setForeground(Color.WHITE);
    }
        
    private void processApproval(String appId, String accNo, double amount) {
        String letterBody = "====================================================\n" +
                            "                NEPAL BANK LIMITED                 \n" +
                            "             OFFICIAL SANCTION LETTER              \n" +
                            "====================================================\n" +
                            "Date: " + new java.util.Date() + "\n" +
                            "Application ID: " + appId + "\n" +
                            "Beneficiary Account: " + accNo + "\n" +
                            "Approved Amount: NPR " + String.format("%.2f", amount) + "\n" +
                            "----------------------------------------------------\n" +
                            "Status: FUNDS DISBURSED SUCCESSFULLY\n" +
                            "====================================================";

        try (Connection con = ConnectionProvider.getCon()) {
            con.setAutoCommit(false); 

            // 1. Update Status AND Letter Content
            String sql = "UPDATE loans SET loan_status = 'APPROVED', sanction_letter_content = ? WHERE application_no = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, letterBody); 
            pst.setString(2, appId);
            pst.executeUpdate();

            // 2. Update Customer Balance
            String balanceSql = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement pst2 = con.prepareStatement(balanceSql);
            pst2.setDouble(1, amount);
            pst2.setString(2, accNo);
            pst2.executeUpdate();
            
            // ✅ 3. NEW: AUDIT TRANSACTION LOG
            String auditSql = "INSERT INTO transactions (account_number, transaction_type, amount, description) VALUES (?, ?, ?, ?)";
            PreparedStatement pstAudit = con.prepareStatement(auditSql);
            pstAudit.setString(1, accNo);
            pstAudit.setString(2, "LOAN_DISBURSEMENT");
            pstAudit.setDouble(3, amount);
            pstAudit.setString(4, "Loan Approved for App ID: " + appId);
            pstAudit.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Success: Loan Approved and Funds Disbursed!");
            loadPendingLoans(); 

        } catch (Exception e) {
            try { ConnectionProvider.getCon().rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }
    
    private void handleApproval(String appId, String accNo, double amount) {
        // 1. First, create the text that the customer needs to see
        String letterBody = "====================================================\n" +
                            "                NEPAL BANK LIMITED                 \n" +
                            "             OFFICIAL SANCTION LETTER              \n" +
                            "====================================================\n" +
                            "Date: " + new java.util.Date() + "\n" +
                            "Application ID: " + appId + "\n" +
                            "Beneficiary Account: " + accNo + "\n" +
                            "Approved Amount: NPR " + amount + "\n" +
                            "----------------------------------------------------\n" +
                            "Status: FUNDS DISBURSED SUCCESSFULLY\n" +
                            "====================================================";

        try (Connection con = ConnectionProvider.getCon()) {
            con.setAutoCommit(false); // Transactions ensure both things happen or nothing happens

            // ✅ THE FIX: Update BOTH the status and the letter content
            String sql = "UPDATE loans SET loan_status = 'APPROVED', sanction_letter_content = ? WHERE application_no = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, letterBody); // This sends the text to the database
            pst.setString(2, appId);
            pst.executeUpdate();

            con.commit(); 
            JOptionPane.showMessageDialog(this, "Success: Loan Approved & Letter Generated!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during approval: " + e.getMessage());
        }
    }
    
    private void viewAuditTrail() {
        String[] cols = {"ID", "Account", "Type", "Amount", "Date"};
        DefaultTableModel auditModel = new DefaultTableModel(cols, 0);
        JTable auditTable = new JTable(auditModel);

        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT * FROM transactions ORDER BY transaction_time DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while(rs.next()) {
                auditModel.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("account_no"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("transaction_time")
                });
            }
            JOptionPane.showMessageDialog(this, new JScrollPane(auditTable), "Transaction Audit Log", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) { e.printStackTrace(); }
    }
}