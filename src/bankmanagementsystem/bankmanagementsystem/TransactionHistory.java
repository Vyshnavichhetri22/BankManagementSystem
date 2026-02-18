package bankmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TransactionHistory extends JFrame {
    private String accountNumber;
    private JTable table;
    private DefaultTableModel model;

    public TransactionHistory(String accountNumber) {
        this.accountNumber = accountNumber;

        setTitle("Transaction Statement - " + accountNumber);
        setSize(900, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Header Style
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(10, 61, 98));
        JLabel header = new JLabel("Statement for Account: " + accountNumber);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        headerPanel.add(header);
        add(headerPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"ID", "Transaction Type", "Amount (NPR)", "Payment Mode", "Date & Time"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        
        // UI Table Styling
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Load Data from Database
        loadDataFromDb();
    }

    private void loadDataFromDb() {
        
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT transaction_id, transaction_type, amount, deposit_mode, transaction_time " +
                         "FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC";
            
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, accountNumber);
            ResultSet rs = pst.executeQuery();

            model.setRowCount(0); // Clear old data

            while (rs.next()) {
                String id = rs.getString("transaction_id");
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                String mode = rs.getString("deposit_mode");
                
                // Handling NULL values for deposit mode (like in your screenshot)
                if (mode == null) mode = "---";
                
                // FETCHING THE CORRECT COLUMN NAME FROM YOUR DB
                String time = rs.getTimestamp("transaction_time").toString();

                model.addRow(new Object[]{
                    "TXN-" + id,
                    type,
                    String.format("%.2f", amount),
                    mode,
                    time
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}