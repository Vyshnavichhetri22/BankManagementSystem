package bankmanagementsystem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.Random;
import java.sql.*;

public class LoanApplicationForm extends JFrame {
    private JTextField txtAcc, txtAmount, txtTenure, txtUni, txtProp, txtKYC, txtIncome;
    private JComboBox<String> cbType;
    private JPanel dynamicPanel;
    private CardLayout cardLayout;
    private JLabel lblAppID, lblSummaryType, lblEMI, lblTotalInterest, lblFinalAmount;
    private String appId;
    
    private String tellerId = "T-452";

    // Bank Interest Rates Logic
    private final double RATE_EDUCATION = 7.0;
    private final double RATE_HOME = 8.5;
    private final double RATE_PERSONAL = 12.0;
    
    private double lastCalculatedEMI = 0.0;
    private double lastCalculatedInterest = 0.0;
    private double lastCalculatedRate = 0.0;

    // Unified Keys to prevent typos
    private final String EDU_KEY = "Education Loan";
    private final String HOME_KEY = "Home Loan";
    private final String PER_KEY = "Personal Loan";
    
    public LoanApplicationForm() {
        this.appId = "L26-" + (10000 + new Random().nextInt(89999));
        initProfessionalUI();
        setupRealTimeCalculators();
    }

    private void setupRealTimeCalculators() {
        DocumentListener calculatorListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { runCalculations(); }
            public void removeUpdate(DocumentEvent e) { runCalculations(); }
            public void changedUpdate(DocumentEvent e) { runCalculations(); }
        };

        txtAmount.getDocument().addDocumentListener(calculatorListener);
        txtTenure.getDocument().addDocumentListener(calculatorListener);
       
       // Update both the CardLayout AND the EMI when the category changes
        cbType.addActionListener(e -> {
            String selected = (String) cbType.getSelectedItem();
            cardLayout.show(dynamicPanel, selected); // Fixes the switching mistake
            lblSummaryType.setText("<html><font color='#aaaaaa'>PLAN</font><br><font color='#ffffff'><b>" + selected.toUpperCase() + "</b></font></html>");
            runCalculations();
        });
    }
    
    private void runCalculations() {
        try {
            if(txtAmount.getText().isEmpty() || txtTenure.getText().isEmpty()) return;

            double p = Double.parseDouble(txtAmount.getText().trim());
            int years = Integer.parseInt(txtTenure.getText().trim());

            double annualRate = (cbType.getSelectedIndex() == 0) ? RATE_EDUCATION : 
                               (cbType.getSelectedIndex() == 1) ? RATE_HOME : RATE_PERSONAL;

            // Save the rate to our class variable
            this.lastCalculatedRate = annualRate; 

            double r = annualRate / 12 / 100;
            int n = years * 12;

            double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
            double totalPayable = emi * n;
            double totalInterest = totalPayable - p;

            // SAVE THE RAW NUMBERS HERE (No HTML, no commas)
            this.lastCalculatedEMI = emi;
            this.lastCalculatedInterest = totalInterest;

            // Keep your beautiful UI labels as they are
            lblEMI.setText("<html><font color='#aaaaaa'>MONTHLY EMI</font><br><font color='#00E676' size='5'><b>NPR " + String.format("%,.2f", emi) + "</b></font></html>");
            lblTotalInterest.setText("<html><font color='#aaaaaa'>INTEREST COST</font><br><font color='#ffffff'><b>NPR " + String.format("%,.2f", totalInterest) + "</b></font></html>");
            lblFinalAmount.setText("<html><font color='#aaaaaa'>TOTAL PAYABLE</font><br><font color='#ffffff'><b>NPR " + String.format("%,.2f", totalPayable) + "</b></font></html>");

        } catch (Exception e) {
            resetDashboard();
        }
    }

    private void resetDashboard() {
        lblEMI.setText("<html><font color='#aaaaaa'>MONTHLY EMI</font><br><font color='#ffffff'><b>NPR 0.00</b></font></html>");
        lblTotalInterest.setText("<html><font color='#aaaaaa'>INTEREST COST</font><br><font color='#ffffff'><b>NPR 0.00</b></font></html>");
        lblFinalAmount.setText("<html><font color='#aaaaaa'>TOTAL PAYABLE</font><br><font color='#ffffff'><b>NPR 0.00</b></font></html>");
    }

    private void initProfessionalUI() {
        setTitle("Nepal Bank | Loan Application Form");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        // --- LEFT SIDEBAR (The Dashboard) ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBackground(new Color(13, 27, 42)); // Midnight Blue
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(40, 30, 40, 30));

        lblAppID = createSidebarLabel("APPLICATION ID", appId, true);
        lblSummaryType = createSidebarLabel("SELECTED PLAN", "General", false);
        lblEMI = createSidebarLabel("MONTHLY EMI", "NPR 0.00", false);
        lblTotalInterest = createSidebarLabel("INTEREST COST", "NPR 0.00", false);
        lblFinalAmount = createSidebarLabel("TOTAL PAYABLE", "NPR 0.00", false);

        sidebar.add(lblAppID); sidebar.add(Box.createRigidArea(new Dimension(0, 40)));
        sidebar.add(lblSummaryType); sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(lblEMI); sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(lblTotalInterest); sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(lblFinalAmount);
        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        // Modern Header
       // JPanel header = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        JLabel title = new JLabel("Loan Application & Risk Assessment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(20, 30, 20, 0));
        header.add(title, BorderLayout.WEST);
        mainContent.add(header, BorderLayout.NORTH);

        // Scrollable Form Grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.weightx = 1.0;
        
        // ComboBox setup using constants
        cbType = new JComboBox<>(new String[]{EDU_KEY, HOME_KEY, PER_KEY});

        // Form Fields
        addModernField(formGrid, gbc, 0, "Account Number", txtAcc = new JTextField());
        addModernField(formGrid, gbc, 1, "Loan Category", cbType = new JComboBox<>(new String[]{"Education Loan (7.0%)", "Home Loan (8.5%)", "Personal Loan (12%)"}));
        addModernField(formGrid, gbc, 2, "Principal Amount (NPR)", txtAmount = new JTextField());
        addModernField(formGrid, gbc, 3, "Repayment Tenure (Years)", txtTenure = new JTextField());

        // Dynamic Verification Panel
        gbc.gridy = 4; gbc.gridwidth = 2;
        cardLayout = new CardLayout();
        dynamicPanel = new JPanel(cardLayout);
        dynamicPanel.setBackground(Color.WHITE);
        dynamicPanel.setOpaque(false);
        
        dynamicPanel.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), "Verification Details"));
        dynamicPanel.add(createDynamicSubPanel("University Name", txtUni = new JTextField()), EDU_KEY);
        dynamicPanel.add(createDynamicSubPanel("Property Valuation Address", txtProp = new JTextField()), HOME_KEY);
        
        JPanel pnlPersonal = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlPersonal.setOpaque(false);
        pnlPersonal.add(new JLabel("<html><i>No additional collateral required for Personal Loans.</i></html>"));
        dynamicPanel.add(pnlPersonal, PER_KEY);
        
        formGrid.add(dynamicPanel, gbc);
        
        // Document Uploads
        gbc.gridy = 5;
        JPanel docs = new JPanel(new GridLayout(1, 2, 20, 0));
        docs.setOpaque(false);
        docs.add(createUploadGroup("KYC Proof(Citizenship)", txtKYC = new JTextField()));
        docs.add(createUploadGroup("Income Statement (Last 6 Months)", txtIncome = new JTextField()));
        formGrid.add(docs, gbc);

        mainContent.add(new JScrollPane(formGrid), BorderLayout.CENTER);

        // Footer Action
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        JButton btnSubmit = new JButton("SUBMIT FOR APPROVAL");
        btnSubmit.setPreferredSize(new Dimension(220, 45));
        btnSubmit.setBackground(new Color(33, 150, 243)); // Google Blue
        
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.addActionListener(e -> btnSubmitActionPerformed());
        
        footer.add(btnSubmit);
        mainContent.add(footer, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        // 1. Setup the listener BEFORE adding it to the UI
        cbType.addActionListener(e -> {
            String selected = (String) cbType.getSelectedItem();

            // Safety: Check if selected string contains the keyword to avoid typo errors
            if (selected.contains("Education")) {
                cardLayout.show(dynamicPanel, EDU_KEY);
            } else if (selected.contains("Home")) {
                cardLayout.show(dynamicPanel, HOME_KEY);
            } else if (selected.contains("Personal")) {
                cardLayout.show(dynamicPanel, PER_KEY);
            }

            // Update the Sidebar UI
            lblSummaryType.setText("<html><font color='#aaaaaa'>PLAN</font><br><font color='#ffffff'><b>" 
                                    + selected.toUpperCase() + "</b></font></html>");
            runCalculations();
        });
    }

    private void addModernField(JPanel p, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridy = y; gbc.gridx = 0;
        JPanel wrap = new JPanel(new BorderLayout(5, 5));
        wrap.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(100, 100, 100));
        wrap.add(l, BorderLayout.NORTH);
        comp.setPreferredSize(new Dimension(0, 35));
        wrap.add(comp, BorderLayout.CENTER);
        p.add(wrap, gbc);
    }

    private JPanel createDynamicSubPanel(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createUploadGroup(String label, JTextField pathField) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(l, BorderLayout.NORTH);
        
        JPanel inner = new JPanel(new BorderLayout(5, 0));
        inner.setOpaque(false);
        inner.add(pathField, BorderLayout.CENTER);
        
        JButton btn = new JButton("Select File");
        // FIX: Working Document Upload Logic
        btn.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle("Select " + label);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                pathField.setText(file.getAbsolutePath());
            }
        });
        
        inner.add(btn, BorderLayout.EAST);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    private JLabel createSidebarLabel(String title, String value, boolean isMain) {
        return new JLabel("<html><font color='#8899A6' size='3'>" + title + "</font><br>" +
                "<font color='#ffffff' size='" + (isMain ? "6" : "4") + "'><b>" + value + "</b></font></html>");
    }
    
    private double cleanValue(String htmlValue) {
        try {
            String clean = htmlValue.replaceAll("<[^>]*>", "") // Remove HTML tags
                                   .replaceAll("NPR", "")      // Remove Currency
                                   .replaceAll(",", "")        // Remove Commas
                                   .trim();
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
        
    private void btnSubmitActionPerformed() {
        
        if (txtKYC.getText().isEmpty() || txtIncome.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please upload both KYC and Income documents first!");
            return;
        }
        
        try (Connection con = ConnectionProvider.getCon()) {
            if (lastCalculatedEMI <= 0) {
                JOptionPane.showMessageDialog(this, "Please calculate the loan first!");
                return;
            }

            // 1. CLEAN THE ID
            String htmlText = lblAppID.getText();
            String rawAppId = htmlText.replaceAll("<[^>]*>", "")
                                       .replace("APPLICATION ID", "")
                                       .replace(":", "").trim();

            con.setAutoCommit(false); // Start Transaction

            // 2. Insert into loans
            String sqlLoan = "INSERT INTO loans (application_no, account_no, loan_type, interest_rate, amount, emi, tenure_years, loan_status, applied_by, kyc_doc_path, income_doc_path) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sqlLoan);
            ps.setString(1, rawAppId);
            ps.setString(2, txtAcc.getText());
            ps.setString(3, cbType.getSelectedItem().toString());
            ps.setDouble(4, lastCalculatedRate);
            ps.setDouble(5, Double.parseDouble(txtAmount.getText()));
            ps.setDouble(6, lastCalculatedEMI);
            ps.setInt(7, Integer.parseInt(txtTenure.getText()));
            ps.setString(8, tellerId);
            ps.setString(9, txtKYC.getText());
            ps.setString(10, txtIncome.getText());
            
            ps.executeUpdate();

            // 3. FIXED: Notify the Manager (Added ? placeholder)
            // Note: Check your table structure. Does it have 3 columns or more?
            String sqlNotify = "INSERT INTO dashboard_notifications (account_number, request_type, amount, status, created_at) VALUES (?, ?, ?, 'PENDING', NOW())";
            PreparedStatement psNotify = con.prepareStatement(sqlNotify);
            psNotify.setString(1, txtAcc.getText());
            psNotify.setString(2, "LOAN_REQUEST "); // Now slot 1 exists!
            psNotify.setDouble(3, Double.parseDouble(txtAmount.getText()));
            psNotify.executeUpdate();

            // 4. Record in Transactions
            String sqlTxn = "INSERT INTO transactions (account_number, transaction_type, amount, status, executed_by_teller_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psTxn = con.prepareStatement(sqlTxn);
            psTxn.setString(1, txtAcc.getText());
            psTxn.setString(2, "LOAN_APP");
            psTxn.setDouble(3, Double.parseDouble(txtAmount.getText()));
            psTxn.setString(4, "Pending");
            psTxn.setString(5, tellerId);
            psTxn.executeUpdate();

            con.commit(); 
            JOptionPane.showMessageDialog(this, "Application " + rawAppId + " sent to Manager for Approval!");
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Submit Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



