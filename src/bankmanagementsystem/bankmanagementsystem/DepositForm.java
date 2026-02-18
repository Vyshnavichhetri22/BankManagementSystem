package bankmanagementsystem;

import bankmanagementsystem.service.TransactionService;
import bankmanagementsystem.dao.AccountDAO;
import bankmanagementsystem.dao.ChequeDAO;
import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.*;

public class DepositForm extends JFrame {

    // --- UI Components ---
    private JPanel mainPanel, cardPanel, page1, page2;
    private CardLayout cardLayout;
    
    // Page 1 Components
    private JTextField p1AccField;
    private JButton p1VerifyBtn;
    private JLabel p1StatusLabel;
    
    // Page 2 Components
    private JTextField p2AmountField, p2DepositorNameField, p2ChequeNoField;        
    private JComboBox<String> p2DepositTypeBox;
    private JComboBox<String> p2ChequeBookBox = new JComboBox<>();
    private JButton p2ExecuteBtn, p2ReceiptBtn, p2BackBtn;
    private JPanel p2ChequeDetailsPanel; 
    
    // Read-Only Labels on Page 2
    private JLabel p2HolderNameLabel, p2HolderPhoneLabel;
    
    // Conditional Phone Input/Label
    private JLabel p2DepositorPhoneLabel; // Read-only label for holder phone
    private JTextField p2DepositorPhoneInput; // Input field for third-party phone
    
    // --- DAO/Service Layers ---
    private final TransactionService transactionService; 
    private final AccountDAO accountDAO; 

    // --- State Management ---
    private boolean isAccountVerified = false; 
    private String verifiedAccNumber = ""; 
    private String verifiedHolderName = ""; 
    private String verifiedHolderPhone = ""; 
    
    // Storage for Receipt Generation 
    private int lastDbTxnId = 0; 
    private BigDecimal lastAmount = BigDecimal.ZERO;
    private String lastType = "";
    private String lastDepositorName = "";
    private String lastTxnStatus = "Pending"; 
    private String lastNewBalance = ""; 
    private final String currentTellerId = "T-452"; // Mock Teller ID
    
    private final Color PRIMARY_NAVY = new Color(0, 45, 98);
    private final ChequeDAO chequeDAO = new ChequeDAO();


    public DepositForm() {
        // Initialize Services/DAOs
        this.transactionService = new TransactionService(); 
        this.accountDAO = new AccountDAO(); 

        setTitle("💸 Multi-Step Teller Deposit Module");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 248, 255)); 
        
        // --- CardLayout Setup ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        createPage1();
        createPage2();
        
        cardPanel.add(page1, "Page1");
        cardPanel.add(page2, "Page2");
        
        // --- Header ---
        JLabel header = new JLabel(" Customer Deposit Transaction", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(new Color(0, 102, 0));
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        add(mainPanel);
        
        // Start on Page 1
        cardLayout.show(cardPanel, "Page1");
        
        // Ensure that closing the window only disposes this frame
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
    
    // =================================================================
    // PAGE 1: ACCOUNT VERIFICATION
    // =================================================================
    private void createPage1() {
        page1 = new JPanel(new GridBagLayout());
        page1.setBackground(Color.WHITE);
        page1.setBorder(BorderFactory.createTitledBorder("Step 1: Account Verification"));
        GridBagConstraints gbc = getGridBagConstraints(new Insets(15, 10, 15, 10));
        
        // Row 1: Account Number
        gbc.gridx = 0; gbc.gridy = 0; page1.add(createLabel("Enter Account No.:"), gbc);
        p1AccField = createStyledTextField(15);
        gbc.gridx = 1; gbc.gridy = 0; page1.add(p1AccField, gbc);
        
        // Row 2: Verify Button
        p1VerifyBtn = createStyledButton("Verify Account", new Color(0, 120, 215));
        p1VerifyBtn.addActionListener(e -> fetchAccountDetails(p1AccField.getText().trim()));
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; page1.add(p1VerifyBtn, gbc);
        
        // Row 3: Status Message
        p1StatusLabel = createStatusLabel("Awaiting account number...", Color.BLUE);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; 
        gbc.anchor = GridBagConstraints.CENTER;
        page1.add(p1StatusLabel, gbc);
        
        gbc.gridy = 2; page1.add(new JLabel(" "), gbc); // Spacer
        
        // Row 4: Next Button (Initially Disabled)
        JButton nextBtn = createStyledButton("Continue to Deposit Details >>", new Color(34, 139, 34));
        nextBtn.setEnabled(false); // Controlled by verification success
        nextBtn.addActionListener(e -> cardLayout.show(cardPanel, "Page2"));
        
        gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        page1.add(nextBtn, gbc);
        
        // Store Next Button in State for updatePage1Controls
        page1.putClientProperty("NextButton", nextBtn);
    }
    
    private void updatePage1Controls(boolean verified) {
        isAccountVerified = verified;
        JButton nextBtn = (JButton) page1.getClientProperty("NextButton");
        if (nextBtn != null) {
            nextBtn.setEnabled(verified);
        }
    }
    
    private void fetchAccountDetails(String accNumber) {
        if (accNumber.isEmpty()) {
            p1StatusLabel.setText("Please enter an account number.");
            p1StatusLabel.setForeground(Color.ORANGE);
            updatePage1Controls(false);
            return;
        }

        Map<String, String> details = accountDAO.getAccountLookupDetails(accNumber); 
        
        if (details != null && details.get("status").equalsIgnoreCase("active")) {
            // SUCCESS
            verifiedAccNumber = accNumber;
            verifiedHolderName = details.get("full_name");
            verifiedHolderPhone = details.get("phone");
            
            p1StatusLabel.setText("✔ Account Verified: " + verifiedHolderName);
            p1StatusLabel.setForeground(new Color(0, 100, 0));
            
            // Set Page 2 initial labels
            p2HolderNameLabel.setText(verifiedHolderName);
            p2HolderPhoneLabel.setText(verifiedHolderPhone);
            
            p2DepositorNameField.setText(""); // Do NOT pre-fill depositor name
            p2DepositorNameField.grabFocus(); 
            
            updatePage2PhoneVisibility(false); // Initialize phone view to require input
            updatePage1Controls(true);
            
        } else {
            // FAILURE
            String status = details != null ? details.get("status") : "Not Found";
            p1StatusLabel.setText("❌ Invalid Account Number or Status: " + status.toUpperCase());
            p1StatusLabel.setForeground(Color.RED);
            updatePage1Controls(false);
        }
    }
        
    // =================================================================
    // PAGE 2: DEPOSIT DETAILS & EXECUTION
    // =================================================================
    private void createPage2() {
        page2 = new JPanel(new GridBagLayout());
        page2.setBackground(Color.WHITE);
        page2.setBorder(BorderFactory.createTitledBorder("Step 2: Transaction Details"));
        GridBagConstraints gbc = getGridBagConstraints(new Insets(8, 10, 8, 10));

        int y = 0;

        // Row 1: Read-Only Holder Name & Phone
        gbc.gridx = 0; gbc.gridy = y; page2.add(createLabel("Holder Name:"), gbc);
        p2HolderNameLabel = createStatusLabel("N/A", Color.BLACK); 
        gbc.gridx = 1; page2.add(p2HolderNameLabel, gbc);
        gbc.gridx = 2; page2.add(createLabel("Phone No.:"), gbc);
        p2HolderPhoneLabel = createStatusLabel("N/A", Color.BLACK); 
        gbc.gridx = 3; gbc.gridy = y++; page2.add(p2HolderPhoneLabel, gbc);

        gbc.gridwidth = 4; gbc.gridy = y++; page2.add(new JSeparator(), gbc); 
        gbc.gridwidth = 1; // Reset gridwidth

        // Row 2: Depositor Name
        gbc.gridx = 0; gbc.gridy = y; page2.add(createLabel("Depositor Name:"), gbc);
        p2DepositorNameField = createStyledTextField(15);
        p2DepositorNameField.addActionListener(e -> updatePage2PhoneVisibility(true));
        p2DepositorNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) { updatePage2PhoneVisibility(true); }
        });
        gbc.gridx = 1; gbc.gridy = y++; page2.add(p2DepositorNameField, gbc);

        // Row 3: Conditional Depositor Phone
        gbc.gridx = 0; gbc.gridy = y; page2.add(createLabel("Depositor Phone:"), gbc);
        p2DepositorPhoneLabel = createStatusLabel("Holder's Phone (Read-Only)", Color.DARK_GRAY);
        p2DepositorPhoneLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        p2DepositorPhoneInput = createStyledTextField(15);

        gbc.gridx = 1; 
        page2.add(p2DepositorPhoneLabel, gbc);
        page2.add(p2DepositorPhoneInput, gbc);
        y++; 

        // Row 4: Transaction Type
        gbc.gridx = 0; gbc.gridy = y; page2.add(createLabel("Transaction Type:"), gbc);
        p2DepositTypeBox = new JComboBox<>(new String[]{"Cash", "Cheque"});
        p2DepositTypeBox.setFont(new Font("Arial", Font.PLAIN, 16));
        p2DepositTypeBox.addActionListener(e -> updateChequeDetailsVisibility());
        gbc.gridx = 1; gbc.gridy = y++; page2.add(p2DepositTypeBox, gbc);

        // Row 5: Amount
        gbc.gridx = 0; gbc.gridy = y; page2.add(createLabel("Amount (NPR):"), gbc);
        p2AmountField = createStyledTextField(15);
        gbc.gridx = 1; gbc.gridy = y++; page2.add(p2AmountField, gbc);

        gbc.gridwidth = 4; gbc.gridy = y++; page2.add(new JSeparator(), gbc); 

        // 1. Create the panel FIRST
        p2ChequeDetailsPanel = createChequeDetailsPanel(); 

        // 2. NOW add it to page2 (This prevents the NullPointerException)
        gbc.gridx = 0; 
        gbc.gridy = y++; 
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        page2.add(p2ChequeDetailsPanel, gbc);
        p2ChequeDetailsPanel.setVisible(false); // Hide until 'Cheque' is selected

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = y++; page2.add(new JLabel(" "), gbc); // Spacer

        // Row 7: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        p2BackBtn = createStyledButton("<< Back to Verification", Color.GRAY);
        p2BackBtn.addActionListener(e -> cardLayout.show(cardPanel, "Page1"));

        p2ExecuteBtn = createStyledButton("Execute Deposit", new Color(34, 139, 34));
        p2ExecuteBtn.addActionListener(e -> attemptDeposit());

        p2ReceiptBtn = createStyledButton("Print Receipt", new Color(0, 120, 215));
        p2ReceiptBtn.setEnabled(false);
        p2ReceiptBtn.addActionListener(e -> generateReceipt());

        buttonPanel.add(p2BackBtn);
        buttonPanel.add(p2ExecuteBtn);
        buttonPanel.add(p2ReceiptBtn);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 4; 
        page2.add(buttonPanel, gbc);
    }
    
    private void updatePage2Controls(boolean transactionRunning) {
        p2ExecuteBtn.setEnabled(!transactionRunning);
        p2BackBtn.setEnabled(!transactionRunning);
    }
    
    // Conditional logic for Depositor Phone (Fixed to show the component correctly)
    private void updatePage2PhoneVisibility(boolean focusLost) {
        String depositorName = p2DepositorNameField.getText().trim();
        // Use equalsIgnoreCase for flexible matching
        boolean isHolder = depositorName.equalsIgnoreCase(verifiedHolderName) && !depositorName.isEmpty();
        
        if (isHolder) {
            // Depositor IS the account holder: Show the read-only label
            p2DepositorPhoneLabel.setText(verifiedHolderPhone + " (Holder)");
            
            p2DepositorPhoneLabel.setVisible(true);
            p2DepositorPhoneInput.setVisible(false);
            
        } else {
            // Depositor is NOT the account holder or name is empty: Require manual input field
            
            p2DepositorPhoneLabel.setVisible(false);
            p2DepositorPhoneInput.setVisible(true);
            
            // If the field is not empty, prompt the user to enter the phone number
            if (focusLost && !depositorName.isEmpty()) {
                p2DepositorPhoneInput.grabFocus();
            }
        }
        // Force the panel to update its layout based on component visibility
        page2.revalidate();
        page2.repaint();
    }
    
    private void updateChequeDetailsVisibility() {
       // String selectedType = (String) p2DepositTypeBox.getSelectedItem();
        boolean isCheque = "Cheque".equals(p2DepositTypeBox.getSelectedItem());
        p2ChequeDetailsPanel.setVisible(isCheque);
        this.revalidate();
        this.repaint();
    }
    

    // --- Updated Cheque Details Panel ---
    private JPanel createChequeDetailsPanel() { 
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("CHEQUE VERIFICATION"));

        p2ChequeNoField = new JTextField(12);
        p2ChequeNoField.setFont(new Font("Monospaced", Font.BOLD, 16));

        JButton checkStatusBtn = new JButton("🔍 CHEQUE BOOK");
        checkStatusBtn.setBackground(new Color(0, 45, 98)); // PRIMARY_NAVY
        checkStatusBtn.setForeground(Color.WHITE);

        checkStatusBtn.addActionListener(e -> {
            if (!verifiedAccNumber.isEmpty()) {
                new ChequeInquiryDialog(this, verifiedAccNumber).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please verify an account first!");
            }
        });

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.setOpaque(false);
        centerPanel.add(new JLabel("Cheque No:"));
        centerPanel.add(p2ChequeNoField);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(checkStatusBtn, BorderLayout.EAST);

        return panel; // This now works because the header is 'JPanel'
    }
    
    private void attemptDeposit() {
        String type = (String) p2DepositTypeBox.getSelectedItem();
        boolean isCheque = "Cheque".equals(type);
        String chequeNo = p2ChequeNoField.getText().trim();
        Connection con = null;

        try {
            // 1. PRE-CHECK: Prevent the "Duplicate Entry" crash before it happens
            if (isCheque) {
                if (chequeDAO.isChequeAlreadyProcessed(chequeNo)) {
                      JOptionPane.showMessageDialog(this, 
                            "SECURITY ALERT: Cheque #" + chequeNo + " has already been deposited.\n" +
                            "Please verify the cheque leaf.", "Duplicate Detected", JOptionPane.ERROR_MESSAGE);
                      return;
                }
            }

            // 2. PIN Authorization
            String tellerPin = showTellerAuthDialog();
            if (tellerPin == null) return;

            // 3. Initialize Connection & Start Transaction
            con = bankmanagementsystem.ConnectionProvider.getCon();
            con.setAutoCommit(false); 

            BigDecimal amount = new BigDecimal(p2AmountField.getText().trim());

            // 4. Execute Main Deposit Logic
            int dbTxnId = transactionService.depositFundsEnhanced(
                verifiedAccNumber, amount, p2DepositorNameField.getText(), 
                "N/A", isCheque, chequeNo, "NORMAL", currentTellerId, tellerPin
            );

            if (dbTxnId > 0) {
                // 5. SYNC: Update the 'cheque_leaves' table so it doesn't show "Available"
                if (isCheque) {
                    // This updates BOTH incoming_cheques AND cheque_leaves (status and txn_id)
                    chequeDAO.recordIncomingCheque(chequeNo, verifiedAccNumber, amount, dbTxnId, con);
                }

                con.commit(); // Save everything permanently

                // 6. Set variables for Receipt
                this.lastDbTxnId = dbTxnId;
                this.lastAmount = amount;
                this.lastType = type;
                this.lastDepositorName = p2DepositorNameField.getText().trim();
                this.lastTxnStatus = "SUCCESS";

                // Get new balance
                Map<String, String> details = accountDAO.getAccountLookupDetails(verifiedAccNumber);
                this.lastNewBalance = (details != null) ? details.get("balance") : "N/A";

                p2ReceiptBtn.setEnabled(true);
                p2ExecuteBtn.setEnabled(false);
                
                AuditService.log(currentTellerId, "DEPOSIT", "NPR " + p2AmountField.getText() + " deposited to A/C: " + p1AccField.getText());
                
                JOptionPane.showMessageDialog(this, "✔ Deposit Successful!\nTransaction ID: " + dbTxnId);
            }

        } catch (Exception ex) {
            try { if(con != null) con.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "Transaction Failed: " + ex.getMessage());
        } finally {
            try { if(con != null) con.close(); } catch (SQLException se) { se.printStackTrace(); }
        }
    }
    
    
    // --- Teller Authorization Modal ---
    private String showTellerAuthDialog() {
        JPasswordField pf = new JPasswordField();
        
        JLabel tellerIdLabel = new JLabel("Teller ID: " + currentTellerId);
        
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.add(tellerIdLabel);
        panel.add(new JLabel("Transaction PIN (Test: 452):"));
        panel.add(pf);
        
        int option = JOptionPane.showConfirmDialog(this, panel, 
                "Teller Authorization Required", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            return new String(pf.getPassword());
        }
        return null;
    }

    // --- Receipt Generation ---
    private void generateReceipt() {
        if (lastDbTxnId == 0) return;
        
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String receiptContent = String.format(
            "=========================================\n" +
            "      BANK DEPOSIT RECEIPT         \n" +
            "=========================================\n" +
            "Transaction ID:  %d\n" +
            "Date/Time:       %s\n" +
            "Teller ID:       %s\n" + 
            "-----------------------------------------\n" +
            "Account No.:     %s\n" +
            "Holder Name:     %s\n" +
            "-----------------------------------------\n" +
            "Depositor Name:  %s\n" +
            "Amount:          NPR %.2f\n" +
            "Type:            %s\n" +
            "Status:          %s\n" +
            "New Balance:     NPR %s\n" + 
            "=========================================",
            lastDbTxnId, timeStamp, currentTellerId, verifiedAccNumber, verifiedHolderName,
            lastDepositorName, lastAmount, lastType, lastTxnStatus, lastNewBalance
        );
        
        JTextArea textArea = new JTextArea(receiptContent);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Transaction Receipt", JOptionPane.PLAIN_MESSAGE);
    }
   
    // --- UI Helper methods ---
    private GridBagConstraints getGridBagConstraints(Insets insets) { 
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
        return gbc;
    }
    private JLabel createLabel(String text) { JLabel label = new JLabel(text); label.setFont(new Font("Arial", Font.BOLD, 14)); return label; }
    private JTextField createStyledTextField(int cols) { JTextField field = new JTextField(cols); field.setFont(new Font("Arial", Font.PLAIN, 16)); return field; }
    private JLabel createStatusLabel(String text, Color color) { JLabel label = new JLabel(text); label.setFont(new Font("Arial", Font.ITALIC, 14)); label.setForeground(color); return label; }
    private JButton createStyledButton(String text, Color bgColor) { JButton button = new JButton(text); button.setFont(new Font("Arial", Font.BOLD, 15)); button.setBackground(bgColor); button.setForeground(Color.WHITE); button.setFocusPainted(false); return button; }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DepositForm().setVisible(true);
        });
    }
}