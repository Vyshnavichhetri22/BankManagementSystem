package bankmanagementsystem;

import bankmanagementsystem.ConnectionProvider;
import java.awt.*;
import java.sql.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class NotificationPanel extends JFrame {

    private JTable notificationTable;
    private DefaultTableModel tableModel;
    private String employeeId;

    public NotificationPanel(String employeeId) {
        this.employeeId = employeeId;
        setTitle("Banking Notification Center - " + employeeId);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setBackground(new Color(0, 45, 98));
        JLabel title = new JLabel("PENDING ACTIONS & APPROVALS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Table Setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Acc No", "Request Type", "Amount/Details", "Status"}, 0);
        notificationTable = new JTable(tableModel);
        notificationTable.setRowHeight(30);
        add(new JScrollPane(notificationTable), BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton approveBtn = new JButton("✅ Process Request");
        approveBtn.setBackground(new Color(0, 102, 0));
        approveBtn.setForeground(Color.WHITE);

        btnPanel.add(refreshBtn);
        btnPanel.add(approveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Actions
        refreshBtn.addActionListener(e -> loadNotifications());
        approveBtn.addActionListener(e -> handleProcessing());

        loadNotifications();
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement("SELECT * FROM dashboard_notifications WHERE status = 'PENDING' ORDER BY created_at DESC")) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("account_number"),
                    rs.getString("request_type"),
                    rs.getBigDecimal("amount"),
                    rs.getString("status")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleProcessing() {
        int row = notificationTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request from the table.");
            return;
        }

        int requestId = (int) tableModel.getValueAt(row, 0);
        String accNo = (String) tableModel.getValueAt(row, 1);
        String type = (String) tableModel.getValueAt(row, 2);

        if (type.equalsIgnoreCase("CHEQUE_REISSUE")) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Process Cheque Book Request?\nSystem will deduct 200 NPR Service Fee.", 
                "Confirm Processing", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                processChequeFee(accNo, requestId);
            }
        } else {
            JOptionPane.showMessageDialog(this, "This request type must be handled via the Withdrawal Terminal or Manager Panel.");
        }
    }
    
    private void processChequeFee(String accNo, int requestId) {
        Connection con = null;
        try {
            con = ConnectionProvider.getCon();
            con.setAutoCommit(false); // Start Transaction

            // 1. Check Balance
            PreparedStatement checkBal = con.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
            checkBal.setString(1, accNo);
            ResultSet rs = checkBal.executeQuery();
            
            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal("balance");
                if (balance.compareTo(new BigDecimal("200")) < 0) {
                    JOptionPane.showMessageDialog(this, "Insufficient balance to cover 200 NPR Fee.");
                    return;
                }
            }

            // 2. Deduct 200 NPR Fee
            PreparedStatement deduct = con.prepareStatement("UPDATE accounts SET balance = balance - 200 WHERE account_number = ?");
            deduct.setString(1, accNo);
            deduct.executeUpdate();

            // 3. Record Transaction
            PreparedStatement trans = con.prepareStatement("INSERT INTO transactions (account_number, transaction_type, amount, deposit_mode, executed_by_teller_id) VALUES (?, 'FEES', 200, 'Cheque Book Reissue Fee', ?)");
            trans.setString(1, accNo);
            trans.setString(2, employeeId);
            trans.executeUpdate();

            // 4. Update Notification Status
            PreparedStatement updateNote = con.prepareStatement("UPDATE dashboard_notifications SET status = 'COMPLETED' WHERE id = ?");
            updateNote.setInt(1, requestId);
            updateNote.executeUpdate();
            
            // 5. Refill Cheque Leaves to 5
            // 5. AUTO-ASSIGN 5 NEW CHEQUE LEAVES (Refill Logic)
            String queryCheques = "INSERT INTO cheque_leaves (cheque_no, account_number, status, is_free) VALUES (?, ?, 'UNUSED', false)";
            try (PreparedStatement pstCheque = con.prepareStatement(queryCheques)) {

                // Use your same random generator logic
                int startCheque = 100001 + (new java.util.Random().nextInt(90000)); 

                for (int i = 0; i < 5; i++) {
                    int currentChq = startCheque + i;
                    pstCheque.setInt(1, currentChq);      // Parameter 1: cheque_no
                    pstCheque.setString(2, accNo);       // Parameter 2: account_number
                    pstCheque.addBatch();
                }
                pstCheque.executeBatch();
            }

            // 6. Update the static count in Accounts table as well
            PreparedStatement updateAcc = con.prepareStatement(
                "UPDATE accounts SET cheque_leaves_remaining = 5 WHERE account_number = ?");
            updateAcc.setString(1, accNo);
            updateAcc.executeUpdate();


            con.commit(); // Save all changes
            JOptionPane.showMessageDialog(this, "Success! 200 NPR deducted and request marked as COMPLETED.");
            loadNotifications();

        } catch (Exception e) {
            try { if(con != null) con.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(this, "Error processing fee: " + e.getMessage());
        }
    }
}