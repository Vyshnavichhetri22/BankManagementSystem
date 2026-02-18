package bankmanagementsystem;

import bankmanagementsystem.ConnectionProvider;
import bankmanagementsystem.dao.ChequeDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ChequeInquiryDialog extends JDialog {
    
    private final bankmanagementsystem.dao.ChequeDAO chequeDAO = new bankmanagementsystem.dao.ChequeDAO();

    public ChequeInquiryDialog(JFrame parent, String accountNumber) {
        super(parent, "Cheque Book Status - " , true); //+ accountNumber
        setSize(550, 400);
        setLocationRelativeTo(parent);

        // Standard Banking Table Columns
        String[] columns = {"Cheque Number", "Status", "Usage Type", "Linked Txn"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
      
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = table.getValueAt(row, 1).toString();
                if ("USED".equalsIgnoreCase(status)) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(new Color(0, 100, 0)); // Dark Green for Available
                }
                return c;
            }
        });
        
        // This query matches your "Open Account" table: cheque_leaves
        try (Connection con = ConnectionProvider.getCon()) {
            String query = "SELECT cheque_no, status, is_free, used_in_txn_id FROM cheque_leaves WHERE account_number = ? ORDER BY cheque_no ASC";
            
            PreparedStatement pst = con.prepareStatement(query);
         
            pst.setString(1, accountNumber);
            ResultSet rs = pst.executeQuery();

            boolean hasData = false;
//            while (rs.next()) {
//                hasData = true;
//                Object[] row = {
//                    rs.getString("cheque_no"),
//                    rs.getString("status"),
//                    rs.getBoolean("is_free") ? "Standard (Free)" : "Premium",
//                    rs.getObject("used_in_txn_id") == null ? "Available" : "TXN-" + rs.getInt("used_in_txn_id")
//                };
//                model.addRow(row);
//            }

                while (rs.next()) {
                    hasData = true;
                    String chequeNo = rs.getString("cheque_no");
                    String status = rs.getString("status");
                    Object txnObj = rs.getObject("used_in_txn_id");
                    

                    // STEP 1: Check if this cheque is in the Master Log
                    int loggedTxnId = chequeDAO.getTxnIdFromLog(chequeNo);
                    
                    String displayStatus = status;
                    String linkedTxn;


                    if (loggedTxnId != -1) {
                        // This cheque WAS deposited (like your 149380)
                        displayStatus = "USED";
                        linkedTxn = "TXN-" + loggedTxnId;

                        // Optional: Auto-fix the DB if it says UNUSED but log says it was used
                        if ("UNUSED".equals(status)) {
                            // Add a background update here if you want to fix the DB permanently
                        }
                    } else if ("USED".equalsIgnoreCase(status)) {
                
                        
                        // Cheque is used but not found in the incoming log (Internal usage)
                        linkedTxn = (txnObj == null) ?  "TXN-" + txnObj: "PROCESSED" ;
//                          linkedTxn = (txnObj == null || txnObj.toString().equals("0")) ? "PROCESSED" : "TXN-" + txnObj;
                    } else {
                        // Truly unused
                        linkedTxn = "Available";
                    }
  
            
                    Object[] row = { chequeNo, displayStatus, rs.getBoolean("is_free") ? "Standard" : "Premium", linkedTxn };
                    model.addRow(row);
                }
            
            if (!hasData) {
                JOptionPane.showMessageDialog(this, "No cheques found for Account: " + accountNumber);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        // Table Styling for Professional Look
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        setLayout(new BorderLayout(10, 10));
        add(new JLabel(" Assigned Cheque Leaf Records", SwingConstants.CENTER), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close Inquiry");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        add(closeBtn, BorderLayout.SOUTH);
    }
}