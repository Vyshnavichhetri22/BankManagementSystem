package bankmanagementsystem;

import bankmanagementsystem.service.TransactionService;
import bankmanagementsystem.dao.AccountDAO;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class TransferForm extends JFrame {

    private final Color G_BLUE = new Color(26, 115, 232);
    private final Color G_GRAY_BG = new Color(248, 249, 250);
    private final Color G_BORDER = new Color(218, 220, 224);
    private final Color TEXT_PRIMARY = new Color(60, 64, 67);
    private final Color SUCCESS_GREEN = new Color(30, 142, 62);
    private final Color ERROR_RED = new Color(217, 48, 37);

    private JTextField sourceAccField, targetAccField, amountField;
    private JLabel sourceNameLabel, targetNameLabel;
    private JButton executeBtn;

    private final TransactionService transactionService;
    private final AccountDAO accountDAO;
    private final String tellerId = "T-452";

    public TransferForm(String employeeId) {
        
        this.transactionService = new TransactionService();
        this.accountDAO = new AccountDAO();

        setTitle("Secure Funds Transfer | Authorized Terminal");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(G_GRAY_BG);
        setLayout(new BorderLayout());

        setupModernUI();
        setVisible(true);
    }

    private void setupModernUI() {
        // Main Container
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Central Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(G_BORDER, 1, true),
            //new EmptyBorder(30, 35, 30, 35)
              new EmptyBorder(45, 50, 45, 50)
        ));

        // 1. Header
        JLabel header = new JLabel("Transfer Funds");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(TEXT_PRIMARY);
        header.setForeground(G_BLUE);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(header);
        
        JLabel subHeader = new JLabel("Internal Bank Transaction | Teller: " + tellerId);
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subHeader.setForeground(Color.GRAY);
        subHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subHeader);
        
        card.add(Box.createVerticalStrut(30));

        // 2. Source Account
        card.add(createInputLabel("DEBIT FROM (SENDER)"));
        sourceAccField = createStyledTextField();
        sourceNameLabel = createStatusChip();
        card.add(sourceAccField);
        card.add(sourceNameLabel);
        
        sourceAccField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { verifyAccount(sourceAccField, sourceNameLabel); }
        });

        card.add(Box.createVerticalStrut(20));

        // 3. Target Account
        card.add(createInputLabel("CREDIT TO (RECEIVER)"));
        targetAccField = createStyledTextField();
        targetNameLabel = createStatusChip();
        card.add(targetAccField);
        card.add(targetNameLabel);

        targetAccField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { verifyAccount(targetAccField, targetNameLabel); }
        });

        card.add(Box.createVerticalStrut(20));

        // 4. Amount
        card.add(createInputLabel("AMOUNT (NPR)"));
        amountField = createStyledTextField();
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(amountField);

        card.add(Box.createVerticalStrut(35));

        // 5. Execute Button
        executeBtn = new JButton("INITIATE TRANSFER");
        executeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        executeBtn.setBackground(G_BLUE);
        executeBtn.setForeground(Color.WHITE);
        executeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        executeBtn.setFocusPainted(false);
        executeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        executeBtn.setBorder(BorderFactory.createEmptyBorder());
        executeBtn.addActionListener(e -> attemptTransfer());
        
        card.add(executeBtn);

        container.add(card);
        add(container, BorderLayout.CENTER);
        
        amountField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    String val = amountField.getText().replace(",", "").trim();
                    if (!val.isEmpty()) {
                        BigDecimal bd = new BigDecimal(val);
                        // Formats to 2 decimal places with commas (e.g., 50,000.00)
                        amountField.setText(String.format("%,.2f", bd));
                    }
                } catch (Exception ex) {
                    // If they type letters, reset it
                    amountField.setText("");
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Remove commas when they click back in to edit
                String val = amountField.getText().replace(",", "");
                amountField.setText(val);
            }
        });
    }

    private void verifyAccount(JTextField field, JLabel chip) {
        String acc = field.getText().trim();
        if (acc.isEmpty()) { chip.setText(" "); return; }

        Map<String, String> details = accountDAO.getAccountLookupDetails(acc);
        if (details != null) {
            chip.setText(" ✔ " + details.get("full_name").toUpperCase() + " ");
            chip.setBackground(new Color(230, 244, 234));
            chip.setForeground(SUCCESS_GREEN);
            field.setBorder(new LineBorder(SUCCESS_GREEN, 1));
        } else {
            chip.setText(" ✘ ACCOUNT NOT FOUND ");
            chip.setBackground(new Color(252, 232, 230));
            chip.setForeground(ERROR_RED);
            field.setBorder(new LineBorder(ERROR_RED, 1));
        }
    }

    private void attemptTransfer() {
        String source = sourceAccField.getText().trim();
        String target = targetAccField.getText().trim();
        String amtStr = amountField.getText().trim();

        if (source.isEmpty() || target.isEmpty() || amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be completed.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show Professional Review
        if (showReviewDialog(source, target, amtStr)) {
            String pin = showTellerAuthDialog();
            if (pin != null) {
                executeTransfer(pin);
            }
        } else {
        }
    }

    private boolean showReviewDialog(String s, String t, String a) {
        String html = "<html><body style='width: 250px;'>" +
                "<h3 style='color:#1a73e8;'>Confirm Transaction</h3>" + "<p style='color: #5f6368; font-size: 10px;'>Digital Receipt - " + new java.util.Date() + "</p>" +
                "</div><hr style='border: 0; border-top: 1px dashed #ccc;'>" +
                "<table style='width: 100%; font-size: 12px;'>" +
         
                "<b>From(Source):</b> " + sourceNameLabel.getText() + "<br>" +
                "<b>To(Target):</b> " + targetNameLabel.getText() + "<br>" +
                 "</table>" +
                "<hr><b>Amount:</b> <span style='color:red;'>NPR " + a + "</span>" +
                "</body></html>";
        
        int res = JOptionPane.showConfirmDialog(this, new JLabel(html), "Transaction Review", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return res == JOptionPane.OK_OPTION;
    }

    private void executeTransfer(String pin) {
        executeBtn.setEnabled(false);
        executeBtn.setText("PROCESSING...");
        
        // 1. Capture the data FIRST so it doesn't get lost
        String sourceAcc = sourceAccField.getText().trim();
        String targetAcc = targetAccField.getText().trim();
        String amountStr = amountField.getText().trim();
        
        String cleanAmount = amountStr.replace(",", "");
        
        try {
            BigDecimal amount = new BigDecimal(cleanAmount);

             int txnId = transactionService.transferFunds(sourceAcc, targetAcc, amount, tellerId, pin);

            if (txnId > 0) {
                
                AuditService.log(tellerId, "FUND_TRANSFER", "From " +     sourceAccField.getText() + " to " +    targetAccField.getText());
                 
                JOptionPane.showMessageDialog(this, "Amount Transfer Successfully! Txn ID: #" + txnId);
                clearFields();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        } finally {
            executeBtn.setEnabled(true);
            
            executeBtn.setText("INITIATE TRANSFER");
        }
    }

    private String showTellerAuthDialog() {
        JPasswordField pf = new JPasswordField();
        
        JLabel tellerIdLabel = new JLabel("Teller ID: " + tellerId );   
        
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

    // --- Styled Helpers ---
    private JLabel createInputLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(95, 99, 104));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(G_BORDER), new EmptyBorder(5, 10, 5, 10)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JLabel createStatusChip() {
        JLabel l = new JLabel(" ");
        l.setOpaque(true);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void clearFields() {
        sourceAccField.setText(""); targetAccField.setText(""); amountField.setText("");
        sourceNameLabel.setText(" "); targetNameLabel.setText(" ");
        sourceAccField.setBorder(new LineBorder(G_BORDER));
        targetAccField.setBorder(new LineBorder(G_BORDER));
    }
}