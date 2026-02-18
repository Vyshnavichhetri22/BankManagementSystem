package bankmanagementsystem;

import bankmanagementsystem.dao.AccountDAO;
import bankmanagementsystem.entities.Account;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.text.DecimalFormat;

public class AccountManagementForm extends JFrame {

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JButton closeAccountBtn, refreshBtn;
    
    private final AccountDAO accountDAO;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    public AccountManagementForm() {
        this.accountDAO = new AccountDAO();
        setTitle("Admin Console: Account Management (Active/Closed Status)");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 242, 245));

        setLayout(new BorderLayout(10, 10));

        // --- Header ---
        JLabel header = new JLabel("💳 Bank Account Management Console", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(new Color(255, 140, 0)); // Orange color matching the dashboard button
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- Table Setup ---
        // Column names matching the data fetched in AccountDAO.getAllAccounts()
        String[] columnNames = {"Account No.", "Full Name", "Balance", "Type", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        accountTable = new JTable(tableModel);
        accountTable.setFont(new Font("Arial", Font.PLAIN, 14));
        accountTable.setRowHeight(25);
        accountTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- Controls Panel (Bottom) ---
        add(createControlPanel(), BorderLayout.SOUTH);

        // Load data on startup
        loadAccountsData();
        
        setVisible(true);
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        controlPanel.setBackground(new Color(240, 242, 245));

        JButton createNewBtn = new JButton("1. Create New Account");
        createNewBtn.setBackground(new Color(34, 139, 34)); // Green
        createNewBtn.setForeground(Color.WHITE);
        createNewBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "To be implemented: Launching New Account Creation Form.", "WIP", JOptionPane.INFORMATION_MESSAGE));
        controlPanel.add(createNewBtn);

        // Soft Delete / Close Account Button
        closeAccountBtn = new JButton("2. Close Selected Account (Soft Delete)");
        closeAccountBtn.setBackground(new Color(178, 34, 34)); // Red
        closeAccountBtn.setForeground(Color.WHITE);
        closeAccountBtn.addActionListener(e -> attemptCloseAccount());
        controlPanel.add(closeAccountBtn);
        
        refreshBtn = new JButton("Refresh Table");
        refreshBtn.addActionListener(e -> loadAccountsData());
        controlPanel.add(refreshBtn);

        return controlPanel;
    }

    // --- Data Loading and Handling ---
    
    private void loadAccountsData() {
        tableModel.setRowCount(0); 
        List<Account> accounts = accountDAO.getAllAccounts();

        for (Account acc : accounts) {
            // Use getBalance() and getFullName() from your Account entity
            String balanceStr = "Rs " + currencyFormat.format(acc.getBalance());
            String status = acc.getStatus();
            
            // Apply color coding based on status using HTML tags
            String statusHtml = switch (status) {
                case "Active" -> "<html><b><font color='green'>ACTIVE</font></b></html>";
                case "Closed" -> "<html><b><font color='red'>CLOSED</font></b></html>";
                case "Dormant" -> "<html><b><font color='blue'>DORMANT</font></b></html>";
                default -> status;
            };
            
            tableModel.addRow(new Object[]{
                acc.getAccountNumber(), // Uses getAccountNumber()
                acc.getFullName(),       // Uses getFullName()
                balanceStr,
                acc.getAccountType(),    // Uses getAccountType()
                statusHtml
            });
        }
    }
    
    private void attemptCloseAccount() {
        int selectedRow = accountTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account from the table to close.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get Account Number (Column 0) and Status (Column 4)
        String accountNo = (String) tableModel.getValueAt(selectedRow, 0);
        String statusHtml = (String) tableModel.getValueAt(selectedRow, 4);

        if (statusHtml.toUpperCase().contains("CLOSED")) {
            JOptionPane.showMessageDialog(this, "Account " + accountNo + " is already closed.", "Already Closed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to CLOSE Account No: " + accountNo + "?\n\nThis action marks the account as 'Closed' (Soft Delete).", 
            "Confirm Account Closure", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = accountDAO.closeAccount(accountNo);

            if (success) {
                JOptionPane.showMessageDialog(this, "Account " + accountNo + " successfully marked as CLOSED.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAccountsData(); // Reload the table to reflect the new status
            } else {
                JOptionPane.showMessageDialog(this, "Failed to close account due to a system or database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}