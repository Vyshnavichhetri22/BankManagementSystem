package bankmanagementsystem;

import bankmanagementsystem.dao.TransactionDAO;
import bankmanagementsystem.entities.Transaction;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class StatementView extends JFrame {

    private JTextField accField;
    private JButton viewBtn;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private final TransactionDAO transactionDAO;
    private final int TRANSACTION_LIMIT = 20; // Show last 20 transactions

    public StatementView() {
        this.transactionDAO = new TransactionDAO();

        setTitle("View Account Statement");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 242, 245));

        // --- Header ---
        JLabel header = new JLabel(" Customer Transaction Statement", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setForeground(new Color(186, 85, 211)); // Purple for Statement/History
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        
        // --- Input Panel (Fixed to include the Header Label) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        inputPanel.setBackground(new Color(240, 242, 245));
        
        inputPanel.add(new JLabel("Enter Account No.:"));
        accField = new JTextField(15);
        accField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(accField);
        
        viewBtn = new JButton("View Statement");
        viewBtn.setBackground(new Color(186, 85, 211));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Arial", Font.BOLD, 14));
        viewBtn.addActionListener(e -> fetchStatement());
        inputPanel.add(viewBtn);
        
        topPanel.add(inputPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH); 

        // --- Table Setup ---
        String[] columnNames = {"Time", "Type", "Description", "Amount", "Balance After", "Related Account"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Make cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        
        // Custom rendering for credit/debit column (optional, but good practice)
        transactionTable.setDefaultRenderer(Object.class, new CustomTransactionRenderer());
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Last " + TRANSACTION_LIMIT + " Transactions"));
        add(scrollPane, BorderLayout.CENTER);
    }
    

    private void fetchStatement() {
    String accNumber = accField.getText().trim();
    
    // Clear previous data
    tableModel.setRowCount(0);

    if (accNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter an Account Number.", "Input Required", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // --- DAO CALL (Fetches List<Transaction> which contains the data) ---
    List<bankmanagementsystem.entities.Transaction> transactions = transactionDAO.getRecentTransactions(accNumber, TRANSACTION_LIMIT);

    if (transactions.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No transactions found for Account " + accNumber + " (or account does not exist).", "Information", JOptionPane.INFORMATION_MESSAGE);
        
        // 4. Add padding rows if fewer than the limit were returned
        while (tableModel.getRowCount() < TRANSACTION_LIMIT) {
             tableModel.addRow(new Object[]{"", "", "", "", "", ""});
        }
        return;
    }

    // --- LOGIC FOR RUNNING BALANCE (BALANCE AFTER) ---
    
    // 1. Fetch the user's current account balance
    java.math.BigDecimal runningBalance = transactionDAO.getCurrentBalance(accNumber); 
    
    if (runningBalance == null) {
        JOptionPane.showMessageDialog(this, "Could not retrieve current balance. Balance After column will be blank.", "Error", JOptionPane.ERROR_MESSAGE);
        runningBalance = java.math.BigDecimal.ZERO; // Use 0 to continue processing
    }

    // 2. Reverse the list: We need to calculate from OLDEST to NEWEST.
    // Since the DAO returns NEWEST to OLDEST (DESC), we reverse it.
    Collections.reverse(transactions); 
    
    // List to hold the final rows, will be reversed again for display
    List<Object[]> rows = new ArrayList<>(); 
    
    // 3. Populate the table model
    for (bankmanagementsystem.entities.Transaction t : transactions) {
        
        // Ensure amount is not null and is signed correctly (+ for credit, - for debit)
        java.math.BigDecimal amount = t.getAmount() != null ? t.getAmount() : java.math.BigDecimal.ZERO;
        
        // The balance BEFORE this transaction:
        java.math.BigDecimal balanceBefore = runningBalance.subtract(amount); 
        
        // --- YOUR EXISTING LOGIC FOR TIME/TYPE/DESCRIPTION ---
        
        // 1. TIME: Use the safe getter and format the LocalDateTime object
        java.time.LocalDateTime transactionTime = t.getTransactionTime(); 
        String timeString = (transactionTime != null) 
                                 ? transactionTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                                 : "N/A"; // Still "N/A" if DAO failed to fetch time

        // 2. TYPE & DESCRIPTION: Your original, correct logic
        String type = t.getTransactionType(); 
        String description;
        String relatedAccount = "N/A"; 

        if (type != null) {
            if (type.contains("Transfer")) {
                relatedAccount = t.getRelatedAccount() != null ? t.getRelatedAccount() : "N/A";
                description = type.contains("Debit") 
                                  ? "Transfer to Acc: " + relatedAccount
                                  : "Transfer from Acc: " + relatedAccount;
            } else if (type.contains("Deposit")) {
                String mode = t.getDepositMode() != null ? t.getDepositMode() : "Cash/Cheque";
                description = "Deposit (" + mode + ")";
            } else {
                description = type;
            }
        } else {
            type = "UNKNOWN";
            description = "Unidentified Transaction";
        }
        
        // --- ADD ROW TO TEMPORARY LIST ---
        String amountString = t.getAmount() != null ? t.getAmount().toString(): "0.00";
        Object[] row = new Object[]{
            timeString,
            type, 
            description,            
            amountString,
            runningBalance.toString(), // The balance AFTER this transaction
            relatedAccount 
        };
        rows.add(row);
        
        // Update running balance for next iteration:
        // We set the balance back to the state *before* this transaction for the next loop.
        runningBalance = balanceBefore;
    }
    
        // 4. Reverse the rows list back to newest-first order and add to table model
        Collections.reverse(rows);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }

        // 5. Add padding rows... (your existing while loop)
        while (tableModel.getRowCount() < TRANSACTION_LIMIT) {
            tableModel.addRow(new Object[]{"", "", "", "", "", ""});
        }
    }   

    // Simple custom renderer to highlight credit/debit rows
    private static class CustomTransactionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Assuming 'Type' is in column 1 (index 1)
            Object typeValue = table.getValueAt(row, 1);
            String type = (typeValue != null) ? typeValue.toString().toLowerCase() : "";

            if (type.contains("deposit") || type.contains("credit")) {
                cell.setForeground(new Color(0, 128, 0)); // Green for Credit
            } else if (type.contains("withdraw") || type.contains("debit") || type.contains("transfer")) {
                cell.setForeground(new Color(178, 34, 34)); // Red for Debit/Withdrawal
            } else {
                cell.setForeground(Color.BLACK);
            }
            return cell;
        }
    }
}