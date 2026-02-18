package bankmanagementsystem;

import bankmanagementsystem.dao.AccountDAO;
import bankmanagementsystem.entities.Account;
import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class BalanceEnquiry extends JFrame {

    private JTextField accField, loadPhoto;
    private JButton checkBtn;
    private JLabel balanceLabel, statusLabel, accountTypeLabel, nameLabel;
    
    private final AccountDAO accountDAO;

    public BalanceEnquiry(String string) {
        this.accountDAO = new AccountDAO();

        setTitle("Account Balance Enquiry");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 242, 245));

        // --- Header ---
        JLabel header = new JLabel(" Check Account Balance", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setForeground(new Color(30, 144, 255)); // Blue for Enquiry/Information
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        inputPanel.setBackground(new Color(240, 242, 245));
        
        inputPanel.add(new JLabel("Enter Account No.:"));
        accField = new JTextField(20);
        accField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputPanel.add(accField);
        
        checkBtn = new JButton("Check Balance");
        checkBtn.setBackground(new Color(30, 144, 255));
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFont(new Font("Arial", Font.BOLD, 14));
        checkBtn.addActionListener(e -> fetchBalance());
        inputPanel.add(checkBtn);
        
        add(inputPanel, BorderLayout.CENTER);

        // --- Result Panel ---
        JPanel resultPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        resultPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Account Details", 
            TitledBorder.CENTER, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), 
            Color.DARK_GRAY
        ));
        resultPanel.setBackground(Color.WHITE);
        
        // Initialize Labels
        balanceLabel = createResultLabel("Current Balance:", "---");
        statusLabel = createResultLabel("Account Status:", "---");
        accountTypeLabel = createResultLabel("Account Type:", "---");

        resultPanel.add(accountTypeLabel);
        resultPanel.add(statusLabel);
        resultPanel.add(balanceLabel);
        
        // Add the result panel to the south, inside a container for styling
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20)); // Padding
        container.add(resultPanel, BorderLayout.CENTER);
        add(container, BorderLayout.SOUTH);
    }

    public BalanceEnquiry() {
        this(""); 
    }
    
    private JLabel createResultLabel(String prefix, String initialValue) {
        JLabel label = new JLabel(prefix + " " + initialValue);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return label;
    }
    
    private void fetchBalance() {
        String accNumber = accField.getText().trim();

        // Reset previous results
        balanceLabel.setText("Current Balance: ---");
        statusLabel.setText("Account Status: ---");
        accountTypeLabel.setText("Account Type: ---");

        if (accNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Account Number.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- DAO CALL ---
        Account account = accountDAO.getAccountByNumber(accNumber);

        if (account != null) {
            // Success: Display details
            BigDecimal balance = account.getBalance();
            String status = account.getStatus();
            String type = account.getAccountType();

            balanceLabel.setText("Current Balance: Rs. " + balance.toPlainString());
            accountTypeLabel.setText("Account Type: " + type);
            
            // Highlight status for clarity
            statusLabel.setText("Account Status: " + status.toUpperCase());
            if (status.equalsIgnoreCase("active")) {
                statusLabel.setForeground(new Color(0, 128, 0));
            } else {
                statusLabel.setForeground(new Color(178, 34, 34));
            }
            
        } else {
            // Failure: Account not found
            JOptionPane.showMessageDialog(this, "Account number not found in the system.", "Account Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Account Status: NOT FOUND");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void loadFullDetails(String accountNumber) {
        Account acc = accountDAO.getAccountByNumber(accountNumber);
        if (acc != null) {
            // This is the method that fills the JTextArea and Photo
            display(accField.getText().trim());
        }
    }

    private void display(String accountNumber) {
        // 1. Call DAO to get the account object
        Account account = accountDAO.getAccountByNumber(accountNumber);

        if (account != null) {
            // 2. Update your labels/table with the account data
            balanceLabel.setText("Rs. " + account.getBalance());
            nameLabel.setText(account.getFullName());
            
            String type = (account.getAccountType() != null) ? account.getAccountType() : "Not Defined";
            accountTypeLabel.setText("Account Type: " + type);

            statusLabel.setText("Account Status: " + account.getStatus());
            balanceLabel.setText("Current Balance: Rs. " + account.getBalance());

        } else {
            JOptionPane.showMessageDialog(this, "Account not found!");
        }
    }
    
}