package bankmanagementsystem;

import bankmanagementsystem.service.TransactionService;
import bankmanagementsystem.dao.AccountDAO;
import bankmanagementsystem.dao.ChequeDAO;
import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Map;
import java.sql.*;

public class WithdrawForm extends JFrame {

    private JPanel mainPanel, cardPanel, page1, page2;
    private CardLayout cardLayout;
    private JTextField p1AccField, p2AmountField, p2ChequeNoField;
    private JLabel p1StatusLabel, p2HolderNameLabel, p2BalanceLabel;
    private JComboBox<String> p2WithdrawTypeBox;
    private JButton p2ExecuteBtn, p2ReceiptBtn, p2RequestApprovalBtn, checkStatusBtn;
    private JPanel p2ChequeDetailsPanel;
    
    private final Color PRIMARY_NAVY = new Color(0, 45, 98);
    private final Color SECONDARY_RED = new Color(139, 0, 0);
    private final Color BG_LIGHT = new Color(240, 242, 245);

    private final TransactionService transactionService; 
    private final AccountDAO accountDAO; 
    private final ChequeDAO chequeDAO; 
    private final String tellerId = "T-452"; 

    private String verifiedAccNumber = "";
    private String accountType = "Saving"; 
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private int confirmedTxnId = 0;
    private BigDecimal confirmedPrincipal = BigDecimal.ZERO;
    private BigDecimal confirmedFee = BigDecimal.ZERO;

    public WithdrawForm(String employeeId) {
        this.transactionService = new TransactionService(); 
        this.accountDAO = new AccountDAO(); 
        this.chequeDAO = new ChequeDAO();

        setTitle("Authorized Withdrawal Terminal - Teller " + tellerId);
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_NAVY);
        headerPanel.setPreferredSize(new Dimension(900, 75));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 5, 0, new Color(212, 175, 55)));
        
        JLabel title = new JLabel("    WITHDRAWAL MODULE", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        
        createPage1();
        createPage2();
        
        cardPanel.add(page1, "Page1");
        cardPanel.add(page2, "Page2");
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void createPage1() {
        page1 = new JPanel(new GridBagLayout());
        page1.setBackground(BG_LIGHT);
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        card.setPreferredSize(new Dimension(450, 300));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel("ENTER ACCOUNT NUMBER", JLabel.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(lbl, gbc);

        p1AccField = new JTextField(15);
        p1AccField.setFont(new Font("Monospaced", Font.BOLD, 20));
        gbc.gridy = 1; card.add(p1AccField, gbc);

        JButton verifyBtn = createStyledButton("VERIFY ACCOUNT", PRIMARY_NAVY);
        verifyBtn.addActionListener(e -> fetchAccount(p1AccField.getText().trim()));
        gbc.gridy = 2; card.add(verifyBtn, gbc);

        p1StatusLabel = new JLabel("Standing by...", JLabel.CENTER);
        gbc.gridy = 3; card.add(p1StatusLabel, gbc);

        JButton nextBtn = createStyledButton("PROCEED >>", Color.GRAY);
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> cardLayout.show(cardPanel, "Page2"));
        gbc.gridy = 4; card.add(nextBtn, gbc);
        page1.putClientProperty("next", nextBtn);

        page1.add(card);
    }

    private void createPage2() {
        page2 = new JPanel(new GridBagLayout());
        page2.setBackground(BG_LIGHT);
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        card.add(new JLabel("Account Holder:"), gbc);
        p2HolderNameLabel = new JLabel("---");
        p2HolderNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 1; card.add(p2HolderNameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("Available Balance:"), gbc);
        p2BalanceLabel = new JLabel("---");
        p2BalanceLabel.setForeground(new Color(0, 150, 0));
        p2BalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 1; card.add(p2BalanceLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Withdrawal Mode:"), gbc);
        p2WithdrawTypeBox = new JComboBox<>(new String[]{"Cash", "Cheque"});
        p2WithdrawTypeBox.addActionListener(e -> { toggleChequePanel(); updateUIForLimits(); });
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(p2WithdrawTypeBox, gbc);

        p2ChequeDetailsPanel = new JPanel(new BorderLayout(10, 0));
        p2ChequeDetailsPanel.setOpaque(false);
        p2ChequeNoField = new JTextField(10);
        checkStatusBtn = new JButton("🔍 CHEQUE BOOK");
        checkStatusBtn.addActionListener(e -> {
            if(!verifiedAccNumber.isEmpty()) new ChequeInquiryDialog(this, verifiedAccNumber).setVisible(true);
        });
        p2ChequeDetailsPanel.add(new JLabel("Cheque No:"), BorderLayout.WEST);
        p2ChequeDetailsPanel.add(p2ChequeNoField, BorderLayout.CENTER);
        p2ChequeDetailsPanel.add(checkStatusBtn, BorderLayout.EAST);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        card.add(p2ChequeDetailsPanel, gbc);
        p2ChequeDetailsPanel.setVisible(false);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        card.add(new JLabel("Amount (NPR):"), gbc);
        p2AmountField = new JTextField(12);
        p2AmountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 1; card.add(p2AmountField, gbc);

        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        actionPanel.setOpaque(false);
        
        p2RequestApprovalBtn = createStyledButton("📤 SEND MANAGER APPROVAL REQUEST", new Color(255, 140, 0));
        p2RequestApprovalBtn.setVisible(false);
        p2RequestApprovalBtn.addActionListener(e -> sendApprovalRequest());
        
        p2ExecuteBtn = createStyledButton("EXECUTE WITHDRAWAL", SECONDARY_RED);
        p2ExecuteBtn.addActionListener(e -> executeWithdrawal());
        
        p2ReceiptBtn = createStyledButton("VIEW OFFICIAL RECEIPT", PRIMARY_NAVY);
        p2ReceiptBtn.setEnabled(false);
        p2ReceiptBtn.addActionListener(e -> showReceipt());

        actionPanel.add(p2RequestApprovalBtn);
        actionPanel.add(p2ExecuteBtn);
        actionPanel.add(p2ReceiptBtn);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 0, 0);
        card.add(actionPanel, gbc);

        page2.add(card);
         

        p2AmountField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updateUIForLimits(); }
            public void removeUpdate(DocumentEvent e) { updateUIForLimits(); }
            public void insertUpdate(DocumentEvent e) { updateUIForLimits(); }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        return btn;
    }

    private void fetchAccount(String acc) {
        Map<String, String> details = accountDAO.getAccountLookupDetails(acc);
        if (details != null) {
            verifiedAccNumber = acc;
            accountType = details.getOrDefault("account_type", "Saving");
            p2HolderNameLabel.setText(details.get("full_name").toUpperCase() + " [" + accountType + "]");
            currentBalance = new BigDecimal(details.getOrDefault("balance", "0"));
            p2BalanceLabel.setText("NPR " + String.format("%,.2f", currentBalance));
            p1StatusLabel.setText("✔ Verified: " + details.get("full_name"));
            p1StatusLabel.setForeground(new Color(0, 120, 0));
            ((JButton)page1.getClientProperty("next")).setEnabled(true);
            ((JButton)page1.getClientProperty("next")).setBackground(PRIMARY_NAVY);
        } else {
            p1StatusLabel.setText("❌ Account not found.");
            p1StatusLabel.setForeground(SECONDARY_RED);
        }
    }

    private void updateUIForLimits() {
        SwingUtilities.invokeLater(() -> {
            try {
                String text = p2AmountField.getText().trim();
                if (text.isEmpty()) { p2RequestApprovalBtn.setVisible(false); return; }
                BigDecimal amt = new BigDecimal(text);
                String mode = (String) p2WithdrawTypeBox.getSelectedItem();
                p2RequestApprovalBtn.setVisible(mode.equals("Cash") && amt.compareTo(new BigDecimal("1000000")) >= 0 && !accountType.equalsIgnoreCase("Business"));
            } catch (Exception ex) { p2RequestApprovalBtn.setVisible(false); }
        });
    }

    private void executeWithdrawal() {
        Connection con = null;
        try {
            BigDecimal amount = new BigDecimal(p2AmountField.getText().trim());
            String mode = (String) p2WithdrawTypeBox.getSelectedItem();
            BigDecimal fee = mode.equals("Cheque") ? new BigDecimal("15.00") : BigDecimal.ZERO;
            BigDecimal total = amount.add(fee);

            if (currentBalance.compareTo(total) < 0) {
                JOptionPane.showMessageDialog(this, "Insufficient Balance.");
                return;
            }

            if (!accountType.equalsIgnoreCase("Business")) {
                if (mode.equals("Cheque") && amount.compareTo(new BigDecimal("1000000")) >= 0) {
                    JOptionPane.showMessageDialog(this, "🛑 LIMIT EXCEEDED: Cheque withdrawal < 10 Lakhs Only."); return;
                }
                if (mode.equals("Cash") && amount.compareTo(new BigDecimal("1000000")) >= 0 && !isApprovedByManager(amount)) {
                    JOptionPane.showMessageDialog(this, "Pending Manager approval."); return;
                }
            }
            
            if(mode.equals("Cheque") && !chequeDAO.verifyChequeStatus(verifiedAccNumber, p2ChequeNoField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Invalid Cheque Leaf."); return;
            }

           // String pin = JOptionPane.showInputDialog(this, "Verify Teller PIN: 452");
            String pin = showTellerAuthDialog(); 
            if (pin == null || pin.trim().isEmpty()) return;

            // --- NEW TRANSACTION LOGIC START ---
            con = ConnectionProvider.getCon();
            con.setAutoCommit(false); // Start Transaction

            // 1. Withdraw the funds
            int txnId = transactionService.withdrawFunds(verifiedAccNumber, total, tellerId, pin);

            if (txnId > 0) {
                // 2. If it's a cheque, use a leaf
                if (mode.equals("Cheque")) {
                    // We pass the connection 'con' so it's part of the same transaction
                    useChequeLeaf(verifiedAccNumber, con); 
                    chequeDAO.markChequeAsUsed(p2ChequeNoField.getText().trim(), txnId, con);
                }

                con.commit(); // SAVE EVERYTHING AT ONCE

                this.confirmedTxnId = txnId;
                this.confirmedPrincipal = amount;
                this.confirmedFee = fee;
                p2ReceiptBtn.setEnabled(true);
                p2ExecuteBtn.setEnabled(false);
                
                AuditService.log(tellerId, "WITHDRAW", "NPR " + p2AmountField.getText() + " from A/C: " + p1AccField.getText());
                
                JOptionPane.showMessageDialog(this, "✔ Transaction Successful!");
            }
        } catch (Exception ex) {
            try { if(con != null) con.rollback(); } catch (SQLException se) {}
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        } finally {
            try { if(con != null) con.setAutoCommit(true); } catch (SQLException se) {}
        }
    }

    // --- RESTORED PROFESSIONAL RECEIPT WINDOW ---
    private void showReceipt() {
        String receiptText = String.format(
            "\n" +
            "                NEPAL BANK\n" +
            "        ==============================\n" +
            "        TRANSACTION RECORD (WITHDRAWAL)\n\n" +
            "        TXN ID    : #%d\n" +
            "        TELLER    : %s\n" +
            "        DATE      : %s\n" +
            "        ==============================\n" +
            "        ACCOUNT NO: %s\n" +
            "        ACC TYPE  : %s\n" +
            "        HOLDER    : %s\n" +
            "        ------------------------------\n" +
            "        MODE      : %s\n" +
            "        AMOUNT    : NPR %,.2f\n" +
            "        FEE/CHARGE: NPR %,.2f\n" +
            "        ------------------------------\n" +
            "        TOTAL AMT : NPR %,.2f\n" +
            "        ==============================\n" +
            "             AUTHORIZED SIGNATURE",
            confirmedTxnId, tellerId, new java.util.Date().toString(),
            verifiedAccNumber, accountType, p2HolderNameLabel.getText(),
            p2WithdrawTypeBox.getSelectedItem(), confirmedPrincipal, confirmedFee, confirmedPrincipal.add(confirmedFee)
        );

        JTextArea area = new JTextArea(receiptText);
        area.setFont(new Font("Monospaced", Font.BOLD, 13));
        area.setEditable(false);
        area.setBackground(new Color(255, 255, 240));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(400, 500));
        
        JOptionPane.showMessageDialog(this, pane, "Official Bank Receipt", JOptionPane.PLAIN_MESSAGE);
    
    }

    private void toggleChequePanel() {
        p2ChequeDetailsPanel.setVisible(p2WithdrawTypeBox.getSelectedItem().equals("Cheque"));
        revalidate(); repaint();
    }
    
    public void recordLog(String empId, String type, String detail) {
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(
                 "INSERT INTO daily_logs (employee_id, action_type, details) VALUES (?, ?, ?)")) {
            pst.setString(1, empId);
            pst.setString(2, type);
            pst.setString(3, detail);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    // CORRECTED: This method now gets the unique ID of the request to track it
    private void sendApprovalRequest() {
        try {
            BigDecimal amt = new BigDecimal(p2AmountField.getText().trim());
            // Added 'Statement.RETURN_GENERATED_KEYS' to get the ID back from the DB
            String sql = "INSERT INTO dashboard_notifications (sender_id, receiver_role, request_type, account_number, amount, status) VALUES (?, 'Manager', 'WITHDRAW_APPROVAL', ?, ?, 'PENDING')";
            
            try (Connection con = ConnectionProvider.getCon(); 
                 PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pst.setString(1, tellerId); 
                pst.setString(2, verifiedAccNumber); 
                pst.setBigDecimal(3, amt);
                
                int affectedRows = pst.executeUpdate();
                
                if (affectedRows > 0) {
                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int requestId = generatedKeys.getInt(1); // THIS IS THE KEY!
                        
                        p2RequestApprovalBtn.setText("⏳ WAITING FOR MANAGER...");
                        p2RequestApprovalBtn.setEnabled(false);
                        p2ExecuteBtn.setEnabled(false);
                        
                        JOptionPane.showMessageDialog(this, "Approval request sent! (ID: " + requestId + ")\nKeep this window open; it will update automatically.");
                        
                        // START THE MONITORING HEARTBEAT
                        waitForManagerApproval(requestId);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Request Error: " + ex.getMessage());
        }
    }

    // CORRECTED: This updates the UI as soon as the manager clicks "Approve"
    private void waitForManagerApproval(int requestId) {
        Timer approvalMonitor = new Timer(3000, e -> {
            try (Connection con = ConnectionProvider.getCon()) {
                String sql = "SELECT status FROM dashboard_notifications WHERE id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, requestId);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        String status = rs.getString("status");
                        
                        if ("APPROVED".equalsIgnoreCase(status)) {
                            ((Timer)e.getSource()).stop(); // Stop checking
                            
                            // UI Feedback
                            p2ExecuteBtn.setEnabled(true);
                            p2RequestApprovalBtn.setVisible(true);
                            p2RequestApprovalBtn.setEnabled(false);
                            p2RequestApprovalBtn.setText("✔ APPROVED BY MANAGER");
                            p2RequestApprovalBtn.setBackground(new Color(22, 163, 74));
                            p2RequestApprovalBtn.setForeground(Color.WHITE);

                            JOptionPane.showMessageDialog(this, "🎉 MANAGER APPROVED!\nYou can now click 'EXECUTE WITHDRAWAL' to finish.");
                            
                        } else if ("REJECTED".equalsIgnoreCase(status)) {
                            ((Timer)e.getSource()).stop();
                            p2RequestApprovalBtn.setText("❌ REJECTED BY MANAGER");
                            p2RequestApprovalBtn.setBackground(Color.RED);
                            JOptionPane.showMessageDialog(this, "The Manager has REJECTED this withdrawal request.");
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Heartbeat Sync Error: " + ex.getMessage());
            }
        });
        approvalMonitor.start();
    }

    private boolean isApprovedByManager(BigDecimal amt) {
        String sql = "SELECT status FROM dashboard_notifications WHERE account_number = ? AND amount = ? AND status = 'APPROVED' AND request_type = 'WITHDRAW_APPROVAL' ORDER BY created_at DESC LIMIT 1";
        try (Connection con = ConnectionProvider.getCon(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, verifiedAccNumber); pst.setBigDecimal(2, amt);
            ResultSet rs = pst.executeQuery(); return rs.next();
        } catch (Exception e) { return false; }
    }
    
    private void useChequeLeaf(String accNo, Connection con) throws SQLException {
    // This query finds exactly ONE unused cheque and marks it as USED
        String useSql = "UPDATE cheque_leaves SET status = 'USED' " +
                        "WHERE account_number = ? AND status = 'UNUSED' " +
                        "LIMIT 1"; 

        try (PreparedStatement pst = con.prepareStatement(useSql)) {
            pst.setString(1, accNo);
            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated == 0) {
                // This stops the transaction if the user has no cheques left
                throw new SQLException("Transaction Failed: No unused cheques available in your book.");
            }
        }
    }
    
    // --- Teller Authorization Modal ---
    private String showTellerAuthDialog() {
        JPasswordField pf = new JPasswordField();
        pf.setEchoChar('●'); // Sets the masking character to a solid dot

        // Request focus so the teller doesn't have to click the box to start typing
        SwingUtilities.invokeLater(() -> pf.requestFocusInWindow());

        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.add(new JLabel("Teller ID: " + tellerId));
        panel.add(new JLabel("Enter Transaction PIN:"));
        panel.add(pf);

        int option = JOptionPane.showConfirmDialog(this, panel, 
                "Teller Authorization Required", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            return new String(pf.getPassword());
        }
        return null;
    }
    
   
}