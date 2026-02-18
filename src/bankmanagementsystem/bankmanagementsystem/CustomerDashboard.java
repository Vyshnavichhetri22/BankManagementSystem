package bankmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CustomerDashboard extends JFrame {

    private final Color PRIMARY_BLUE = new Color(10, 61, 98);
    private final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private final Color CARD_BG = Color.WHITE;
    private final Color SUCCESS_GREEN = new Color(46, 125, 50);

    private String accountNumber;      
    private JButton requestBtn;
    private JButton btnViewLetter;

    private JLabel balanceLabel = new JLabel("NPR 0.00");
    private JLabel nameLabel = new JLabel("Welcome...");
    private JLabel chequeStatusLabel = new JLabel("");
    private JLabel interestLabel = new JLabel("NPR 0.00");
    
    private JLabel emailLbl = new JLabel("📧 Email: Loading...");
    private JLabel phoneLbl = new JLabel("📱 Phone: Loading...");
    private JLabel typeLbl = new JLabel("💳 Type: Loading...");

    public CustomerDashboard(String accountNumber) {
        this.accountNumber = accountNumber;
        
        setTitle("Nepal Bank - Customer Portal (" + accountNumber + ")");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(BACKGROUND_COLOR);
        mainContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        mainContent.add(nameLabel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 25, 25));
        grid.setBackground(BACKGROUND_COLOR);
        
        grid.add(createDataCard("TOTAL BALANCE", balanceLabel, "Available in Savings"));
        grid.add(createDataCard("ESTIMATED MONTHLY INTEREST", interestLabel, "Calculated at 7% p.a."));
        grid.add(createChequeManagementCard()); 
        grid.add(createProfileCard()); 

        mainContent.add(grid, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        loadCustomerData();
        checkForNotifications(accountNumber);
    }

    private void loadCustomerData() {
        try (Connection con = ConnectionProvider.getCon()) {
            
            String sql = "SELECT full_name, balance, email, phone, account_type FROM Accounts WHERE account_number = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, accountNumber);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                double balance = rs.getDouble("balance");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String type = rs.getString("account_type");

                nameLabel.setText("Welcome Back, " + fullName.toUpperCase());
                balanceLabel.setText("NPR " + String.format("%,.2f", balance));

                double monthlyInt = (balance * 0.07) / 12;
                interestLabel.setText("NPR " + String.format("%,.2f", monthlyInt));

                emailLbl.setText("📧 " + email);
                phoneLbl.setText("📱 " + phone);
                typeLbl.setText("💳 Type: " + type);
            }
            rs.close();
            pst.close();
                
            String chequeSql = "SELECT COUNT(*) AS total FROM cheque_leaves WHERE account_number = ? AND status = 'UNUSED'";
            PreparedStatement pstCheque = con.prepareStatement(chequeSql);
            pstCheque.setString(1, accountNumber);
            ResultSet rsCheque = pstCheque.executeQuery();

            if (rsCheque.next()) {
                int realLeaves = rsCheque.getInt("total");
               
                 updateChequeDisplay(realLeaves); 
            }
                
            rsCheque.close();
            pstCheque.close();
            
        } catch (Exception e) {
            System.out.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel createProfileCard() {
        JPanel card = new JPanel(new GridLayout(4, 1, 5, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("USER PROFILE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(new Color(120, 130, 140));

        card.add(title);
        card.add(typeLbl);
        card.add(emailLbl);
        card.add(phoneLbl);

        return card;
    }

    
       private void updateChequeDisplay(int leaves) {
        if (chequeStatusLabel == null) return;

        String statusText = "<html>Leaves Remaining: <b style='font-size:16px;'>" + leaves + " / 5</b>";

        if (leaves == 0) {
            statusText += "<br><font color='red'>Status: Empty - Request Refill</font></html>";
            if(requestBtn != null) requestBtn.setEnabled(true);
        } else if (leaves <= 2) {
            statusText += "<br><font color='orange'>Status: Low (Use Carefully)</font></html>";
            if(requestBtn != null) requestBtn.setEnabled(false); // Can't request yet
        } else {
            statusText += "<br><font color='green'>Status: Active</font></html>";
            if(requestBtn != null) requestBtn.setEnabled(false); // Can't request yet
        }

        chequeStatusLabel.setText(statusText);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 700));
        sidebar.setBackground(PRIMARY_BLUE);
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

        JLabel brand = new JLabel("NEPAL BANK");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        sidebar.add(brand);

        JButton dashBtn = createNavButton("  Customer Details");
        dashBtn.addActionListener(e -> loadCustomerData());
        sidebar.add(dashBtn); 

        btnViewLetter = createNavButton("  Loan Status");
        btnViewLetter.addActionListener(e -> checkFullLoanStatus(accountNumber));
        sidebar.add(btnViewLetter);

        JButton transferBtn = createNavButton(" Fund Transfer");
        transferBtn.addActionListener(e -> {
            String receiverAcc = JOptionPane.showInputDialog(this, "Enter Recipient(Receiver) Account Number:");
            if (receiverAcc == null || receiverAcc.trim().isEmpty()) return;

            String amountStr = JOptionPane.showInputDialog(this, "Enter Amount to Transfer (NPR):");
            if (amountStr == null || amountStr.trim().isEmpty()) return;

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than zero!");
                    return;
                }
                handleTransfer(receiverAcc, amount);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount!");
            }
        });
        sidebar.add(transferBtn);

        JButton transBtn = createNavButton("  Transactions");
        sidebar.add(transBtn);

        JButton settingsBtn = createNavButton("️  Settings");
        settingsBtn.addActionListener(e -> {
            String[] options = {"Change PIN", "Forgot PIN"};
            int choice = JOptionPane.showOptionDialog(this, "Security Settings", "Nepal Bank Security",
                                 JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                     updatePinInDatabase();
            } else if (choice == 1) {
                JOptionPane.showMessageDialog(this, "Security Note: Please visit your branch with your Citizenship ID to reset your PIN.");
            }
        });
        sidebar.add(settingsBtn);

        sidebar.add(Box.createRigidArea(new Dimension(200, 180)));

        JButton logoutBtn = createNavButton(" Logout");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JPanel createDataCard(String title, JLabel valueLabel, String footer) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(new Color(120, 130, 140));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(PRIMARY_BLUE);

        JLabel f = new JLabel(footer);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        f.setForeground(Color.GRAY);

        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(f, BorderLayout.SOUTH);

        return card;
    }


      private JPanel createChequeManagementCard() {
          JPanel card = new JPanel(new BorderLayout(10, 10));
          card.setBackground(CARD_BG);
          card.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
              BorderFactory.createEmptyBorder(20, 20, 20, 20)
          ));

          JLabel title = new JLabel("CHEQUE BOOK STATUS");
          title.setFont(new Font("Segoe UI", Font.BOLD, 12));
          title.setForeground(new Color(120, 130, 140));

          chequeStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

          requestBtn = new JButton("Request New Cheque Book");
          requestBtn.setBackground(SUCCESS_GREEN);
          requestBtn.setForeground(Color.WHITE);
          requestBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
          requestBtn.setFocusPainted(false);

          requestBtn.addActionListener(e -> {
              handleChequeRequest();
          });

          card.add(title, BorderLayout.NORTH);
          card.add(chequeStatusLabel, BorderLayout.CENTER);
          card.add(requestBtn, BorderLayout.SOUTH);

          return card;
      }

      private void handleChequeRequest() {
          
          int confirm = JOptionPane.showConfirmDialog(this, 
              "Requesting a new Cheque Book will incur a 200 NPR service charge.\n" +
              "This will be deducted from your account upon bank approval.\nDo you wish to proceed?", 
              "Service Fee Notice", JOptionPane.YES_NO_OPTION);

          if (confirm == JOptionPane.YES_OPTION) {
              try (Connection con = ConnectionProvider.getCon();
                   PreparedStatement pst = con.prepareStatement(
                       "INSERT INTO dashboard_notifications (account_number, request_type, amount, status) VALUES (?, ?, ?, ?)")) {

                  pst.setString(1, accountNumber);
                  pst.setString(2, "CHEQUE_REISSUE");
                  pst.setDouble(3, 200.00);
                  pst.setString(4, "PENDING");

                  pst.executeUpdate();

                  JOptionPane.showMessageDialog(this, "✅ Request Sent! Please wait for bank approval.");

                  loadCustomerData(); 

              } catch (Exception ex) {
                  ex.printStackTrace();
                  JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
              }
          }
      }

      private void updatePinInDatabase() {
        // 1. Create the hidden fields
        JPasswordField oldPinField = new JPasswordField();
        JPasswordField newPinField = new JPasswordField();
        JPasswordField confirmPinField = new JPasswordField();

        // 2. Arrange them in a panel
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Current 4-Digit PIN:"));
        panel.add(oldPinField);
        panel.add(new JLabel("New 4-Digit PIN:"));
        panel.add(newPinField);
        panel.add(new JLabel("Confirm New PIN:"));
        panel.add(confirmPinField);

        // 3. Show the dialog
        int option = JOptionPane.showConfirmDialog(this, panel, "Change Secure PIN", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String oldPin = new String(oldPinField.getPassword());
            String newPin = new String(newPinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword());

            // Validation
            if (newPin.length() != 4 || !newPin.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "New PIN must be exactly 4 digits.");
                return;
            }
            if (!newPin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(this, "New PIN and Confirmation do not match!");
                return;
            }

            // 4. Update Database
            try (Connection con = ConnectionProvider.getCon()) {
                
                // Add this line right before String sql = ...
                System.out.println("DEBUG: Account: [" + this.accountNumber + "] Old PIN: [" + oldPin + "]");
                String sql = "UPDATE users SET pin = ? WHERE account_number = ? AND pin = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, newPin);
                pst.setString(2, this.accountNumber);
                pst.setString(3, oldPin);

                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Success: PIN changed successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Error: Current PIN is incorrect.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            }
        }
    }

    private void handleTransfer(String toAcc, double amount) {
            
        try (Connection con = ConnectionProvider.getCon()) {
            String findUser = "SELECT full_name FROM Accounts WHERE account_number = ?";
            PreparedStatement pstFind = con.prepareStatement(findUser);
            pstFind.setString(1, toAcc);
            ResultSet rsFind = pstFind.executeQuery();

            if (!rsFind.next()) {
                JOptionPane.showMessageDialog(this, "Account not found in Nepal Bank.");
                return;
            }

            String receiverName = rsFind.getString("full_name");
            // --- START OF NEW HIDDEN PIN CODE ---
            JPasswordField pinField = new JPasswordField();
            // This makes the dialog look professional
            Object[] message = {
            "Confirm Transfer of NPR " + String.format("%,.2f", amount),
            "To: " + receiverName.toUpperCase(),
            "\nEnter 4-Digit Secure PIN:", pinField};

            int option = JOptionPane.showConfirmDialog(this, message, "Security Verification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option != JOptionPane.OK_OPTION) return; // Stop if user clicks Cancel

            String pin = new String(pinField.getPassword()); // Gets the hidden text
            // --- END OF NEW HIDDEN PIN CODE ---
            if (pin == null) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Transfer NPR " + amount + " to " + receiverName.toUpperCase() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
                      
            String verifySql = "SELECT * FROM users WHERE account_number = ? AND pin = ?";
            PreparedStatement pstVerify = con.prepareStatement(verifySql);
            pstVerify.setString(1, this.accountNumber);
            pstVerify.setString(2, pin);
            ResultSet rsVerify = pstVerify.executeQuery();

            if (!rsVerify.next()) {
                JOptionPane.showMessageDialog(this, "❌ Incorrect PIN! Transaction Cancelled.");
                return; // STOP HERE if PIN is wrong
            }
            
            con.setAutoCommit(false);
            
            // Deduct
            String deduct = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ? AND balance >= ?";
            PreparedStatement pstDeduct = con.prepareStatement(deduct);
            pstDeduct.setDouble(1, amount);
            pstDeduct.setString(2, accountNumber);
            pstDeduct.setDouble(3, amount);
            
            if (pstDeduct.executeUpdate() == 0) {
                JOptionPane.showMessageDialog(this, "Insufficient Funds!");
                con.rollback();
                return;
            }

            String add = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement pstAdd = con.prepareStatement(add);
            pstAdd.setDouble(1, amount);
            pstAdd.setString(2, toAcc);
            pstAdd.executeUpdate();

            String log = "INSERT INTO transactions (account_number, transaction_type, amount, deposit_mode) VALUES (?, 'Digital Transfer', ?, 'Mobile App')";
            PreparedStatement pstLog = con.prepareStatement(log);
            pstLog.setString(1, accountNumber);
            pstLog.setDouble(2, amount);
            pstLog.executeUpdate();

            con.commit(); 
            JOptionPane.showMessageDialog(this, "✅ Success! NPR " + amount + " transferred to " + receiverName);
            
            // 1. Prepare the Receipt Content in a String first
            String receiptContent = "==========================================\n" +
                                    "                NEPAL BANK                \n" +
                                    "         DIGITAL TRANSFER RECEIPT         \n" +
                                    "==========================================\n" +
                                    "Date: " + new java.util.Date() + "\n" +
                                    "Transaction Type: Digital Transfer\n" +
                                    "From Account: " + this.accountNumber + "\n" +
                                    "To Account: " + toAcc + " (" + receiverName.toUpperCase() + ")\n" +
                                    "Amount: NPR " + String.format("%,.2f", amount) + "\n" +
                                    "Status: SUCCESSFUL\n" +
                                    "==========================================\n" +
                                    "   Thank you for banking with Nepal Bank  \n";

            try {
                // 1. Get the base user path (C:\Users\dell)
                String userHome = System.getProperty("user.home");

                // 2. Try the standard Desktop first
                java.io.File desktop = new java.io.File(userHome, "Desktop");

                // 3. If standard Desktop doesn't exist, try the OneDrive Desktop
                if (!desktop.exists()) {
                    desktop = new java.io.File(userHome, "OneDrive/Desktop");
                }

                // 4. Create the final file path
                java.io.File receiptFile = new java.io.File(desktop, "Receipt_" + toAcc + ".txt");

                // 5. Write the file
                java.io.FileWriter writer = new java.io.FileWriter(receiptFile);
                writer.write(receiptContent);
                writer.close();

                // 6. Success Messages
                JOptionPane.showMessageDialog(this, "Receipt saved to: " + receiptFile.getAbsolutePath());

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(receiptFile);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "File Error: " + e.getMessage());
            }
            loadCustomerData();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if(text.contains("Transactions")) {
            btn.addActionListener(e -> new TransactionHistory(accountNumber).setVisible(true));
        }

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(20, 80, 130)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(PRIMARY_BLUE); }
        });
        return btn;
    }
    
    private void checkForLetter(String customerAccNo) {
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT sanction_letter_content FROM loans WHERE account_no = ? AND loan_status = 'APPROVED'";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, customerAccNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String content = rs.getString("sanction_letter_content");
                if (content != null) {
                    
                    JTextArea textArea = new JTextArea(content);
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Your Sanction Letter", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void checkForNotifications(String accNo) {
        try (Connection con = ConnectionProvider.getCon()) {
         
            String sql = "SELECT loan_status, sanction_letter_content FROM loans " +
                         "WHERE account_no = ? ORDER BY application_no DESC LIMIT 1";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, accNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String status = rs.getString("loan_status");
                String content = rs.getString("sanction_letter_content");

                if ("APPROVED".equals(status)) {
                  
                    btnViewLetter.setText("📄 View Sanction Letter (NEW!)");
                    btnViewLetter.setBackground(new Color(46, 204, 113)); 
                    btnViewLetter.setForeground(Color.WHITE);
                    btnViewLetter.setFont(new Font("SansSerif", Font.BOLD, 12));
                } else if ("REJECTED".equals(status)) {
                 
                    btnViewLetter.setText("📄 View Rejection Reason");
                    btnViewLetter.setBackground(new Color(231, 76, 60)); 
                    btnViewLetter.setForeground(Color.WHITE);
                } else {
                   
                    btnViewLetter.setText("📄 View Loan Status");
                    btnViewLetter.setBackground(Color.LIGHT_GRAY);
                    btnViewLetter.setForeground(Color.BLACK);
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
    
    private void openLetterView(String accNo) {
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT sanction_letter_content FROM loans WHERE account_no = ? AND loan_status = 'APPROVED'";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, accNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String content = rs.getString("sanction_letter_content");

                JTextArea letterArea = new JTextArea(15, 40);
                letterArea.setText(content);
                letterArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
                letterArea.setEditable(false);
                letterArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JScrollPane scrollPane = new JScrollPane(letterArea);

                JOptionPane.showMessageDialog(this, scrollPane, "Official Sanction Letter", JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No approved loan letters found.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void checkFullLoanStatus(String accNo) {
        try (Connection con = ConnectionProvider.getCon()) {
            
            String sql = "SELECT loan_status, rejection_reason, sanction_letter_content FROM loans " +
                         "WHERE account_no = ? ORDER BY application_no DESC LIMIT 1";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, accNo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String status = rs.getString("loan_status");
                String letter = rs.getString("sanction_letter_content");
                String reason = rs.getString("rejection_reason");

                if ("APPROVED".equals(status)) {
                    if (letter == null || letter.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Loan Approved! Funds are disbursed.");
                    } else {
                        showLetterPopup(letter);
                    }
                } else if ("REJECTED".equals(status)) {
                    String msg = (reason == null || reason.isEmpty()) ? "Documentation incomplete." : reason;
                    JOptionPane.showMessageDialog(this, "Status: REJECTED\nReason: " + msg, "Loan Update", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Status: PENDING\nYour application is under review.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No loan applications found.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showLetterPopup(String content) {
        JTextArea letterArea = new JTextArea(20, 50);
        letterArea.setText(content);
        
        letterArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        letterArea.setEditable(false);
        letterArea.setMargin(new Insets(10, 10, 10, 10));
        letterArea.setBackground(new Color(250, 250, 250));

        JScrollPane scrollPane = new JScrollPane(letterArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(44, 62, 80), 2));

        JOptionPane.showMessageDialog(this, scrollPane, "Nepal Bank - Official Sanction Letter", JOptionPane.PLAIN_MESSAGE);
    }
}
