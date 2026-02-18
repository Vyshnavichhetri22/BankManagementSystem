package bankmanagementsystem;

import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.border.EmptyBorder;

public class LoanRepaymentForm extends JFrame {
    private final Color PRIMARY_BLUE = new Color(21, 101, 192);
    private final Color SUCCESS_GREEN = new Color(46, 125, 50);
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private JTextField txtAccNo, txtAmount, txtChqNo;
    private JComboBox<String> cbMode;
    private JPanel panelCheque;
    
    private JLabel lblSavings;
    
    private JButton btnAuth;
    private JButton btnCloseAccount;
    
    private final double RATE_EDUCATION = 7.0;
    private final double RATE_HOME = 8.5;
    private final double RATE_PERSONAL = 12.0;
    
    private JLabel lblName, lblCategory, lblEmi, lblDisbursed, lblRepaid, lblOutstanding, lblTenure;
    
    private int calculatedMonthsSaved = 0;
    private double calculatedInterestSaved = 0.0;
    
    private double currentMinEmi = 0.0;
    private double currentOutstanding = 0.0;
    private final String tellerId;

    public LoanRepaymentForm(String tellerId) {
        this.tellerId = "T-452";
        initUI();
    }

    private void initUI() {
        setTitle("NEPAL BANK | Loan Repayment");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        txtAmount = new JTextField();
        
        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculatePotentialSavings();
            }
        });
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_BLUE);
        header.setPreferredSize(new Dimension(0, 70));
        JLabel title = new JLabel("    LOAN SETTLEMENT TERMINAL");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Main Workspace
        JPanel workspace = new JPanel(new GridLayout(1, 2, 25, 0));
        workspace.setBorder(new EmptyBorder(25, 25, 25, 25));
        workspace.setBackground(new Color(245, 247, 250));

        workspace.add(createExecutionPanel());
        workspace.add(createDashboardPanel());

        add(workspace, BorderLayout.CENTER);

         // Inside initUI() footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 15));
        footer.setBackground(Color.WHITE);

        
        // Inside initUI() footer section
        btnCloseAccount = new JButton("CLOSE LOAN ACCOUNT");
        btnCloseAccount.setPreferredSize(new Dimension(220, 50));
        btnCloseAccount.setBackground(new Color(33, 33, 33)); // Dark/Professional Color
        btnCloseAccount.setForeground(Color.WHITE);
        btnCloseAccount.setVisible(false); // Hidden by default!
        btnCloseAccount.addActionListener(e -> finalizeLoanClosure());

        footer.add(btnCloseAccount);

        // 2. Your existing Authorize Button (Execution Tool)
        btnAuth = new JButton("AUTHORIZE & PAY");
        btnAuth.setPreferredSize(new Dimension(220, 50));
        btnAuth.setBackground(SUCCESS_GREEN);
        btnAuth.setForeground(Color.WHITE);
        btnAuth.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAuth.addActionListener(e -> processSecureRepayment());

        //footer.add(btnCompare); // Add the new button
        footer.add(btnAuth);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createExecutionPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(30, 30, 30, 30)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(8,0,8,0);
        gbc.gridx = 0;

        p.add(new JLabel("CUSTOMER ACCOUNT NUMBER"), gbc);
        txtAccNo = new JTextField();
        txtAccNo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridy = 1; p.add(txtAccNo, gbc);

        JButton btnFetch = new JButton("VERIFY ACCOUNT");
        btnFetch.setPreferredSize(new Dimension(150, 30));
        btnFetch.setBackground(PRIMARY_BLUE);
        btnFetch.setForeground(Color.WHITE);
        btnFetch.addActionListener(e -> fetchLoanData());
        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        p.add(btnFetch, gbc);

        gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(Box.createVerticalStrut(30), gbc); 

        p.add(new JLabel("PAYMENT AMOUNT (NPR)"), gbc);
        txtAmount = new JTextField();
        txtAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridy = 4; p.add(txtAmount, gbc);

        p.add(new JLabel("SETTLEMENT MODE"), gbc);
        cbMode = new JComboBox<>(new String[]{"Cash Deposit ", "Cheque Payment "});
        gbc.gridy = 5; p.add(cbMode, gbc);

        panelCheque = new JPanel(new BorderLayout(10, 0));
        panelCheque.setOpaque(false);
        txtChqNo = new JTextField();
        JButton btnInq = new JButton("CHEQUE BOOK");
        btnInq.addActionListener(e -> {
            String acc = txtAccNo.getText().trim();
            if(!acc.isEmpty()) new ChequeInquiryDialog(this, acc).setVisible(true);
        });
        panelCheque.add(new JLabel("Cheque No:"), BorderLayout.WEST);
        panelCheque.add(txtChqNo, BorderLayout.CENTER);
        panelCheque.add(btnInq, BorderLayout.EAST);
        panelCheque.setVisible(false);
        
        cbMode.addActionListener(e -> {
            panelCheque.setVisible(cbMode.getSelectedIndex() == 1);
            revalidate();
        });

        gbc.gridy = 6; p.add(panelCheque, gbc);
        return p;
    }

    private JPanel createDashboardPanel() {
        JPanel mainRight = new JPanel(new GridBagLayout());
        mainRight.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // ROW 1: Name and Category (Full Width)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        lblName = createInfoCard(mainRight, "CUSTOMER NAME", "---", gbc);

        gbc.gridy = 1;
        lblCategory = createInfoCard(mainRight, "LOAN TYPE @ RATE", "--- @ 0.00%", gbc);

        // ROW 2: EMI and Months Left (Split 50/50)
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5;
        lblEmi = createInfoCard(mainRight, "SCHEDULED MONTHLY EMI", "NPR 0.00", gbc);

        gbc.gridx = 1;
        lblTenure = createInfoCard(mainRight, "EST. MONTHS LEFT", "0", gbc);

        // ROW 3: Total Loan Disbursed (Full Width)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        lblDisbursed = createInfoCard(mainRight, "TOTAL LOAN DISBURSED", "NPR 0.00", gbc);

        // ROW 4: Repaid vs Outstanding (Split 50/50)
        gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.5;
        lblRepaid = createInfoCard(mainRight, "TOTAL REPAID", "NPR 0.00", gbc);
        lblRepaid.setForeground(SUCCESS_GREEN);

        gbc.gridx = 1;
        lblOutstanding = createInfoCard(mainRight, "CURRENT BALANCE", "NPR 0.00", gbc);
        lblOutstanding.setForeground(new Color(211, 47, 47));

        // ROW 5: The Integrated Savings Action Center
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        mainRight.add(createIntegratedSavingsTool(), gbc);

        return mainRight;
    }
    
    
    private JPanel createIntegratedSavingsTool() {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setBackground(new Color(240, 248, 255));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 230)), "SAVINGS & PAYOFF ANALYTICS"));

        lblSavings = new JLabel("<html><i>Enter amount to calculate payoff...</i></html>");
        lblSavings.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSavings.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnSmallCompare = new JButton("VIEW COMPARISON");
        btnSmallCompare.setBackground(PRIMARY_BLUE);
        btnSmallCompare.setForeground(Color.WHITE);
        btnSmallCompare.addActionListener(e -> {
            // 1. Force a final calculation check
            calculatePotentialSavings(); 

            // Check if it's the final month
            String tenureText = lblTenure.getText();
            if (tenureText.contains("0 Months") || tenureText.equals("1 Months Remaining")) {
                JOptionPane.showMessageDialog(this, 
                    "This is the final payment. Comparison is only for long-term interest savings.", 
                    "Settlement Mode", JOptionPane.INFORMATION_MESSAGE);
                return; 
            }

            try {
                // 1. Clean the input and get the typed amount
                String rawAmt = txtAmount.getText().replace(",", "").trim();
                if (rawAmt.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter an amount first.");
                    return;
                }
                double typedAmt = Double.parseDouble(rawAmt);

                // 2. Refresh calculations based on the typed amount
                calculatePotentialSavings(); 

                // 3. Logic Check: Show table if they pay even 1 Rupee more than EMI
                if (typedAmt > currentMinEmi) {
                    // Even if it doesn't save a full month, we show the interest savings
                    showComparisonTable(typedAmt, calculatedMonthsSaved, calculatedInterestSaved);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Comparison is only available for extra payments.\nMinimum EMI is NPR " + df.format(currentMinEmi));
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.");
            }
        });
        
        p.add(lblSavings, BorderLayout.CENTER);
        p.add(btnSmallCompare, BorderLayout.EAST);
        return p;
    }  
    
    private JLabel createInfoCard(JPanel parent, String title, String value, GridBagConstraints gbc) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230)), new EmptyBorder(10, 15, 10, 15)));

        JLabel t = new JLabel(title); 
        t.setFont(new Font("Segoe UI", Font.BOLD, 10)); 
        t.setForeground(Color.GRAY);

        JLabel v = new JLabel(value); 
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));

        card.add(t, BorderLayout.NORTH); 
        card.add(v, BorderLayout.CENTER);

        parent.add(card, gbc);
        return v;
    }
    
   
    private JPanel createSavingsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(232, 245, 233)); // Light Mint Green
        p.setBorder(new LineBorder(new Color(129, 199, 132), 1));

        lblSavings = new JLabel("<html><b>TIP:</b> Enter an amount above EMI to see interest savings.</html>");
        lblSavings.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSavings.setBorder(new EmptyBorder(10, 10, 10, 10));

        p.add(lblSavings, BorderLayout.CENTER);
        return p;
    }

    private void fetchLoanData() {
        String acc = txtAccNo.getText().trim();
        if (acc.isEmpty()) return;
        
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT a.full_name, l.loan_type, l.interest_rate, l.amount as disbursed, l.emi, l.tenure_years, " +
                         "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE account_number = ? AND transaction_type = 'LOAN_REPAYMENT') as total_paid " +
                         "FROM accounts a JOIN loans l ON a.account_number = l.account_no WHERE a.account_number = ?";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, acc); ps.setString(2, acc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                
                recordLog(con, "ACCOUNT_INQUIRY", "Teller accessed loan details for: " + acc);
                
                String loanType = rs.getString("loan_type");
                double rate = 0.0;
                
                // 1. Logic to set labels based on your constants
                if (loanType.equalsIgnoreCase("Personal Loan")) {
                    lblCategory.setText("PERSONAL LOAN @ " + RATE_PERSONAL + "%");
                } else if (loanType.equalsIgnoreCase("Home Loan")) {
                    lblCategory.setText("HOME LOAN @ " + RATE_HOME + "%");
                } else if (loanType.equalsIgnoreCase("Education Loan")) {
                    lblCategory.setText("EDUCATION LOAN @ " + RATE_EDUCATION + "%");
                } else {
                    lblCategory.setText(loanType.toUpperCase());
                }
                
                double interestRate = rs.getDouble("interest_rate");
                double emi = rs.getDouble("emi");
                double disbursed = rs.getDouble("disbursed");
                double totalpaid = rs.getDouble("total_paid");
                int years = rs.getInt("tenure_years");
                
                // 2. SMART FALLBACK: If EMI is NULL/0, calculate it roughly or show warning
                if (emi <= 0) {
                    // Temporary calculation if DB is empty: (Principal / (Years * 12))
                    emi = disbursed / (years * 12); 
                }
            
                this.currentMinEmi = emi;
                this.currentOutstanding = disbursed - totalpaid;
                
                if (this.currentOutstanding < 0.5) this.currentOutstanding = 0;

                String logSql = "INSERT INTO daily_logs (employee_id, action_type, details) VALUES (?, ?, ?)";
                try (PreparedStatement logPst = con.prepareStatement(logSql)) {
                    logPst.setString(1, tellerId); // This is "101"
                    logPst.setString(2, "ACCOUNT_INQUIRY");
                    logPst.setString(3, "Viewed loan details for A/C: " + acc + " (" + rs.getString("full_name") + ")");
                    logPst.executeUpdate();
                }
                calculatePotentialSavings();

                lblName.setText(rs.getString("full_name").toUpperCase());
            
                lblEmi.setText("NPR " + df.format(emi));
                
                lblDisbursed.setText("NPR " + df.format(disbursed));
                lblRepaid.setText("NPR " + df.format(totalpaid));
                lblOutstanding.setText("NPR " + df.format(currentOutstanding));
            
                if (emi > 0 && currentOutstanding > 0) {
                    int monthsLeft = (int) Math.ceil(currentOutstanding / emi);
                    lblTenure.setText(monthsLeft + " Months Remaining");

                    if (monthsLeft <= 1 || currentOutstanding <= emi) {
                        // 1. Alert the teller this is a final payoff
                        lblSavings.setText("<html><font color='blue'><b>FINAL PAYOFF MODE:</b> Comparison disabled for final month.</font></html>");

                        // 2. Automatically set the amount to the EXACT outstanding balance
                        txtAmount.setText(String.valueOf(currentOutstanding));

                        // 3. Highlight the outstanding balance in a "Settlement" color
                        lblOutstanding.setForeground(new Color(255, 140, 0)); // Dark Orange
                    } else {
                        // Normal behavior for middle-of-loan
                        txtAmount.setText(String.valueOf(emi));
                        lblOutstanding.setForeground(new Color(211, 47, 47)); // Red
                    }
                } else {
                    lblTenure.setText("0 Months (Settled)");
                    txtAmount.setText("0.00");
                }

               txtAmount.setText(String.valueOf(emi));
               
               // --- 🚀 ADD THE BUTTON LOCK LOGIC HERE (Right before the end of the 'if(rs.next())' block) ---
                if (this.currentOutstanding <= 0) {
                    btnAuth.setEnabled(false);
                    btnAuth.setBackground(Color.GRAY);
                    btnCloseAccount.setVisible(true);
                    lblSavings.setText("<html><b style='color:green'>ACCOUNT SETTLED: No further payments accepted.</b></html>");
                } else {
                    btnAuth.setVisible(true);
                    btnAuth.setEnabled(true);
                    btnAuth.setBackground(SUCCESS_GREEN);
                    btnCloseAccount.setVisible(false);
                    // This resets the label if they fetch a different account that ISN'T settled
                    calculatePotentialSavings(); 
                }
            
                revalidate(); // Refresh UI to show/hide buttons
                repaint();
            }else{
                JOptionPane.showMessageDialog(this, "No active loan found for A/C: " + acc);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Sync Error: " + e.getMessage()); }
    }
        
    private void calculatePotentialSavings() {
        try {
            String amtStr = txtAmount.getText().replace(",", "").trim();
            if (amtStr.isEmpty() || currentMinEmi <= 0) return;

            double typedAmount = Double.parseDouble(amtStr);
            double activeRate = 0.0;

            // Use the RATE constants directly based on the type
            String type = lblCategory.getText().toUpperCase();
            if (type.contains("PERSONAL")) activeRate = RATE_PERSONAL;
            else if (type.contains("HOME")) activeRate = RATE_HOME;
            else if (type.contains("EDUCATION")) activeRate = RATE_EDUCATION;

            if (typedAmount > currentMinEmi && activeRate > 0) {
                double extra = typedAmount - currentMinEmi;

                // 1. GET CURRENT MONTHS LEFT (from the label)
                String tenureStr = lblTenure.getText().replaceAll("[^0-9]", "");
                int currentRemainingMonths = tenureStr.isEmpty() ? 0 : Integer.parseInt(tenureStr);

                // 2. CALCULATE SAVINGS
                calculatedMonthsSaved = (int) Math.floor(extra / currentMinEmi);
                calculatedInterestSaved = extra * (activeRate / 100) * (currentRemainingMonths / 12.0);

                // If the savings are more than what is actually left, cap it at the total remaining
                if (calculatedMonthsSaved > currentRemainingMonths) {
                    calculatedMonthsSaved = currentRemainingMonths - 1;
                }
            
                if (calculatedMonthsSaved > 0) {
                    lblSavings.setText("<html><font color='#2e7d32'><b>🚀 SAVINGS:</b> " + 
                                       calculatedMonthsSaved + " months & NPR " + 
                                       df.format(calculatedInterestSaved) + "</font></html>");
                }
            } else {
                lblSavings.setText("<html><i>Paying exactly NPR " + df.format(currentMinEmi) + " (Standard EMI)</i></html>");
                calculatedMonthsSaved = 0;
                calculatedInterestSaved = 0;
            }
        } catch (Exception e) {
            lblSavings.setText("<html><font color='red'>Invalid amount format</font></html>");
        }
    }
         
    private void showComparisonTable(double typedAmount, int monthsSaved, double interestSaved) {
        JDialog dialog = new JDialog(this, "Loan Payoff Comparison", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        
        int currentTenure = Integer.parseInt(lblTenure.getText().replaceAll("[^0-9]", ""));
        int acceleratedTenure = currentTenure - monthsSaved;
        
        double remainingAfterPayment = currentOutstanding - typedAmount;
        
        int displayRemainingMonths;
        if (remainingAfterPayment < 1.0) {
            displayRemainingMonths = 0; // It's fully paid
        } else {
            // Standard calculation for partial payments
            displayRemainingMonths = currentTenure - monthsSaved;
        }

        // Final safety check for the table display
        if (acceleratedTenure < 0) acceleratedTenure = 0;

        String[] columns = {"Metric", "Current Plan", "Accelerated Plan"};
        Object[][] data = {
            {"Monthly Payment", "NPR " + df.format(currentMinEmi), "NPR " + df.format(typedAmount)},
            {"Remaining Months", currentTenure + " Months", acceleratedTenure + " Months"},
            {"Total Interest", "Standard APR", "Saved NPR " + df.format(interestSaved)},
            {"Status", "Regular Plan", ">>>FAST TRACK <<<"}
        };

        JTable table = new JTable(data, columns);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Header Styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(33, 150, 243));
        table.getTableHeader().setForeground(Color.WHITE);

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close Comparison");
        btnClose.addActionListener(e -> dialog.dispose());
        dialog.add(btnClose, BorderLayout.SOUTH);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        dialog.setVisible(true);
    }
        
    private void processSecureRepayment() {
        try {
            // 1. Basic Validation
            String acc = txtAccNo.getText().trim();
            String amtStr = txtAmount.getText().replace(",", "").trim();
            double typedAmt = Double.parseDouble(txtAmount.getText().replace(",", ""));

            if (currentOutstanding <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "TRANSACTION DENIED: This loan is already fully settled (Balance: NPR 0.00).", 
                    "Account Closed", JOptionPane.ERROR_MESSAGE);
                return; // Stop the payment process
            }
            
            if (acc.isEmpty() || amtStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please verify an account and enter an amount.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double amount = Double.parseDouble(amtStr);
            
            // Safety check: Don't let them pay more than the remaining balance
            if (amount > currentOutstanding) {
                int choice = JOptionPane.showConfirmDialog(this, 
                    "The payment amount (NPR " + amount + ") exceeds the remaining balance (NPR " + currentOutstanding + ").\n" +
                    "Do you want to adjust the payment to exactly NPR " + currentOutstanding + "?",
                    "Excess Payment Detected", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    amount = currentOutstanding;
                    txtAmount.setText(String.valueOf(amount));
                } else {
                    return; // Stop if they don't want to adjust
                }
            }

            // 2. Policy Check: Ensure payment isn't below EMI
            if (amount < currentMinEmi) {
                JOptionPane.showMessageDialog(this, "POLICY ERROR: Repayment cannot be less than EMI NPR " + df.format(currentMinEmi), "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Proceed to security
            String pin = showTellerAuthDialog();
            if (pin != null && pin.equals("452")) {
                executeDBRepayment(typedAmt);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
        }
    }

    
      private String showTellerAuthDialog() {
        JPasswordField pf = new JPasswordField();
        
        JLabel tellerIdLabel = new JLabel("Teller ID: " + tellerId);
        
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
     
    private void executeDBRepayment(double amt) {
        // Show 'Waiting' state
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        double serviceCharge = (cbMode.getSelectedIndex() == 1) ? 15.0 : 0.0;
        long generatedTxnId = 0;
        
        try (Connection con = ConnectionProvider.getCon()) {
            con.setAutoCommit(false);

            String sql = "INSERT INTO transactions (account_number, transaction_type, amount, service_charge, executed_by_teller_id, cheque_no) VALUES (?, 'LOAN_REPAYMENT', ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS );
            pst.setString(1, txtAccNo.getText());
            pst.setDouble(2, amt);
            pst.setDouble(3, serviceCharge);
            pst.setString(4, tellerId);
            pst.setString(5, cbMode.getSelectedIndex() == 1 ? txtChqNo.getText() : "CASH");

            int rowAffected = pst.executeUpdate();
            
            // --- 2. NEW: Capture the Transaction ID ---
            
            java.sql.ResultSet rsKeys = pst.getGeneratedKeys(); 
            if (rsKeys.next()) {
                generatedTxnId = rsKeys.getLong(1);
            }
                
           // 3. NEW: Update Cheque Status to 'USED' if it's a cheque payment
            if (cbMode.getSelectedIndex() == 1 && generatedTxnId > 0) {
               String sqlChq = "UPDATE cheque_leaves SET status = 'USED', used_in_txn_id = ?  WHERE cheque_no = ? AND account_number = ?";
                PreparedStatement pstChq = con.prepareStatement(sqlChq);
                    
                pstChq.setLong(1, generatedTxnId);
                pstChq.setString(2, txtChqNo.getText());
                pstChq.setString(3, txtAccNo.getText());
                    
                pstChq.executeUpdate();
            }

            // 🚀 4. NEW: UPDATE LOAN ASSETS (Decrease Balance)
           // This makes sure the 'Total Loan Assets' on your Dashboard actually goes down
           String sqlUpdateLoan = "UPDATE loans SET outstanding_balance = outstanding_balance - ? WHERE account_no = ?";
           PreparedStatement pstLoan = con.prepareStatement(sqlUpdateLoan);
           pstLoan.setDouble(1, amt);
           pstLoan.setString(2, txtAccNo.getText());
           pstLoan.executeUpdate();

            String logDetails = "Loan Repayment: NPR " + amt + " for Account: " + txtAccNo.getText() + 
                                " | Mode: " + cbMode.getSelectedItem().toString();

            // 5. Log the Action   Insert into the daily_logs table
            String sqlLog = "INSERT INTO daily_logs (employee_id, action_type, details, log_timestamp) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement pstLog = con.prepareStatement(sqlLog);
            pstLog.setString(1, tellerId); 
            pstLog.setString(2, "LOAN_REPAYMENT");
            pstLog.setString(3, logDetails);
            pstLog.executeUpdate();
            
            con.commit();
            
            String status = (currentOutstanding <= amt) ? "FULL_SETTLEMENT" : "PARTIAL_REPAYMENT";
            recordLog(con, "LOAN_REPAYMENT", status + " of NPR " + amt + " for Acc: " + txtAccNo.getText());

            if (rowAffected > 0) {
                con.commit();

                // --- PROFESSIONAL UI RESET ---                
                showOfficialReceipt(amt, serviceCharge);
               
                fetchLoanData();
               
                // --- CONGRATULATIONS LOGIC ---
                // 2. USE A STRICT CHECK: 
                // Only show if the balance is truly zero AND the tenure is also 0
                String tenureCheck = lblTenure.getText();
                if (this.currentOutstanding <= 1.0 && (tenureCheck.contains("0 Months") || tenureCheck.contains("Settled"))) {
                    showCompletionBadge(); 
                } else {
                    // Just show the normal success message for regular EMI
                    JOptionPane.showMessageDialog(this, "Monthly EMI Repayment Authorized Successfully!\nTxn ID: #" + generatedTxnId);
                }
    
                clearForm();                
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    // 1. Method to clear the form for the next customer
    private void clearForm() {
        txtAccNo.setText("");
        txtAmount.setText("");
        txtChqNo.setText("");
        cbMode.setSelectedIndex(0);
        lblName.setText("---");
        lblCategory.setText("--- @ 0.00%");
        lblEmi.setText("NPR 0.00");
        lblTenure.setText("0");
        lblDisbursed.setText("NPR 0.00");
        lblRepaid.setText("NPR 0.00");
        lblOutstanding.setText("NPR 0.00");
        currentMinEmi = 0.0;
        currentOutstanding = 0.0;
    }

    // 2. Method to generate the receipt text for the digital file
    private void showOfficialReceipt(double amount, double charge) {
        // 1. Create the Dialog
        JDialog receiptDialog = new JDialog(this, "Official Bank Receipt", true);
        receiptDialog.setSize(450, 600);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setLayout(new BorderLayout());

        // 2. Build the Receipt Text (Terminal Style)
        String accNo = txtAccNo.getText();
        String date = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").format(new java.util.Date());
        String total = df.format(amount + charge);

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("             NEPAL CENTRAL BANK             \n");
        sb.append("============================================\n");
        sb.append("       LOAN REPAYMENT ==> EMI PAYMENT       \n");
        sb.append("\n");
        sb.append(String.format(" TXN ID     : #%d\n", System.currentTimeMillis() % 100000));
        sb.append(String.format(" TELLER     : %s\n", tellerId));
        sb.append(String.format(" DATE       : %s\n", date));
        sb.append("============================================\n");
        sb.append(String.format(" ACCOUNT NO : %s\n", accNo));
        sb.append(String.format(" HOLDER     : %s\n", lblName.getText()));
        sb.append("--------------------------------------------\n");
        sb.append(String.format(" MODE       : %s\n", cbMode.getSelectedItem().toString()));
        sb.append(String.format(" AMOUNT     : NPR %s\n", df.format(amount)));
        sb.append(String.format(" TAX/FEE    : NPR %s\n", df.format(charge)));
        sb.append("--------------------------------------------\n");
        sb.append(String.format(" TOTAL AMT  : NPR %s\n", total));
        sb.append("============================================\n");
        sb.append("\n");
        sb.append("          AUTHORIZED SIGNATURE              \n");
        sb.append("\n");

        // 3. Setup the Text Area
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13)); // The secret to the bank look
        area.setEditable(false);
        area.setBackground(new Color(255, 255, 240)); // Slightly off-white/ivory like paper
        area.setMargin(new Insets(20, 20, 20, 20));

        // 4. Add a ScrollPane and OK button
        JScrollPane scroll = new JScrollPane(area);
        receiptDialog.add(scroll, BorderLayout.CENTER);

        JButton btnOk = new JButton("OK");
        btnOk.setPreferredSize(new Dimension(100, 40));
        btnOk.addActionListener(e -> receiptDialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnOk);
        receiptDialog.add(btnPanel, BorderLayout.SOUTH);

        receiptDialog.setVisible(true);
    }
    
    private void recordLog(Connection con, String action, String details) {
        String sql = "INSERT INTO daily_logs (employee_id, action_type, details) VALUES (?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tellerId); 
            pst.setString(2, action);
            pst.setString(3, details);
            pst.executeUpdate();
            
            String type;
            if (currentOutstanding <= 0) {
                type = "LOAN_CLOSED";
                details = "🎉 FULL SETTLEMENT: Account " + txtAccNo.getText() + " cleared by " + tellerId;
            } else {
                type = "LOAN_REPAYMENT";
                details = "Partial repayment of NPR " + txtAmount + " for Acc: " + txtAccNo.getText();
            }
            

        } catch (SQLException e) {
            System.err.println("Audit Log Error: " + e.getMessage());
        }
    }

    private void showCompletionBadge() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // Add a Golden/Green Success Icon
        JLabel iconLabel = new JLabel("SUCCESS", SwingConstants.CENTER);
        iconLabel.setForeground(new Color(46, 125, 50));
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));

        JLabel msgLabel = new JLabel("<html><div style='text-align: center;'>"
            + "<b style='font-size: 16px; color: #2e7d32;'>LOAN FULLY SETTLED!</b><br><br>"
            + "Account " + txtAccNo.getText() + " now has zero outstanding balance.<br>"
            + "A 'No Objection Certificate' (NOC) request has been queued.</div></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(msgLabel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Congratulations!", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void finalizeLoanClosure() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to officially CLOSE this loan account?\nThis action is permanent.", 
            "Confirm Account Closure", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try (Connection con = ConnectionProvider.getCon()) {
                // Update the loan status to CLOSED
                String sql = "UPDATE loans SET status = 'CLOSED' WHERE account_no = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, txtAccNo.getText());

                int updated = pst.executeUpdate();
                if (updated > 0) {
                    // Log the closure
                    recordLog(con, "LOAN_CLOSED", "Loan account " + txtAccNo.getText() + " officially closed and archived.");

                    JOptionPane.showMessageDialog(this, "Account Closed Successfully! NOC is now available.");
                    clearForm();
                    btnCloseAccount.setVisible(false);
                    btnAuth.setVisible(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Closure Error: " + e.getMessage());
            }
        }
    }
    
    private void btnViewComparisonActionPerformed(java.awt.event.ActionEvent evt) {
        // 1. Check if the loan is ALREADY settled
        if (currentOutstanding <= 0) {
            JOptionPane.showMessageDialog(this, 
                "This loan is fully settled. There are no future interests to save!", 
                "Account Closed", JOptionPane.INFORMATION_MESSAGE);
            return; 
        }

        // 2. Check if this is the very last month
        String tenureText = lblTenure.getText();
        if (tenureText.contains("0 Months") || tenureText.contains("Settled")) {
             // This is where your current popup is coming from. 
             // Change it to a ConfirmDialog so they can STILL see the table if they want.
             int choice = JOptionPane.showConfirmDialog(this, 
                 "This is the final payment. Comparison metrics may be limited. View anyway?", 
                 "Final Payment Mode", JOptionPane.YES_NO_OPTION);

             if (choice != JOptionPane.YES_OPTION) return;
        }

        // 3. ONLY NOW open the comparison window
       // Pass the variables that your method requires
        double typedAmt = Double.parseDouble(txtAmount.getText().replace(",", ""));
        showComparisonTable(typedAmt, calculatedMonthsSaved, calculatedInterestSaved);
    }
}