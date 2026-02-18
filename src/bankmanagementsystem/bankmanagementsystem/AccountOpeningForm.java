package bankmanagementsystem;

import com.github.lgooddatepicker.components.DatePicker;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Random;
import javax.swing.*;
import java.sql.PreparedStatement;
import javax.swing.border.LineBorder;
import java.time.format.DateTimeFormatter; // Added for date formatting

public class AccountOpeningForm extends JFrame {

    // --- CORE CONSTANTS & FORM STATE ---
    private final String APPLICATION_NO; 
    private JPanel[] pages = new JPanel[4];
    private int currentPage = 0;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private boolean isMinor = false;
    
    private String tellerId = "T-452";
    
    // --- LAYOUT CONSTANTS ---
    private static final int WIDTH = 650;
    private static final int HEIGHT = 800;
    private static final int LABEL_X = 50; 
    private static final int FIELD_X = 250; 
    private static final int FIELD_WIDTH = 350;
    private static final int FIELD_HEIGHT = 30; 
    private static final int LINE_SPACING = 45; 
    
    // --- Navigation Buttons ---
    private JButton prevBtn, nextBtn, submitBtn, cancelBtn;

    // --- Page 1 fields ---
    private JTextField fullNameField, fatherNameField, motherNameField, emailField, mobileNoField, streetAddressField, districtField, provinceField;
    private DatePicker dobPicker;
    private JRadioButton male, female, married, unmarried;
    
    // --- Page 2 fields (Dynamic) ---
    private JTextField guardianNameField, guardianRelationField, citizenshipField;
    private String adultIdPath; 
    private JLabel adultIdStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);
    
    // Minor Documents:
    private String dobCertificatePath, minorSignaturePath, guardianCitizenshipPath, guardianSignaturePath;
    private JLabel dobCertStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);
    private JLabel minorSignStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);
    private JLabel guardianCitizenStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);
    private JLabel guardianSignStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);

    // --- Page 3 fields ---
    private JComboBox<String> accountTypeBox, purposeBox, currencyBox, branchBox, occupationBox, sourceBox, depositModeBox;
    private JTextField depositAmountField, annualIncomeField;

    // --- Page 4 fields ---
    private JCheckBox atmCardBox, mobileBankingBox, chequeBookBox, emailAlertsBox;
    private String passportPhotoPath, signaturePath;
    private JCheckBox termsBox;
    private JLabel photoStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);
    private JLabel signatureStatusLabel = new JLabel("Status: Not Uploaded", JLabel.LEFT);
    
    private JTextField chequeNumberField; 
    private JTextField issuingBankField;

    // --- CONSTRUCTOR ---
    public AccountOpeningForm() {
        this.APPLICATION_NO = generateApplicationNumber();
        
        // Frame Setup
        setTitle("Opening Bank Account - Form No: " + APPLICATION_NO);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout()); 
        getContentPane().setBackground(Color.WHITE);
        
        add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Color.WHITE);
        
        initializeButtons(); 
        createPages(); 
        
        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createFooterPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        prevBtn.addActionListener(e -> showPage(currentPage - 1));
        nextBtn.addActionListener(e -> handleNextButton());
        submitBtn.addActionListener(e -> submitForm());
        cancelBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel the application?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) dispose();
        });

        showPage(0);
        setVisible(true);
    }
    
    // --- UTILITY: Generate unique application number ---
    private String generateApplicationNumber() {
        return "APP" + String.valueOf((new Random().nextInt(9000) + 1000));
    }

    // --- UI HELPER: Creates the application header ---
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(WIDTH, 100));
        header.setBackground(new Color(240, 248, 255));

        JLabel image = new JLabel();
        try {
            // Note: Assuming ConnectionProvider is in the same folder structure, or the path is correct
            ImageIcon i1 = new ImageIcon(getClass().getResource("/banklogo.jpeg")); 
            Image i2 = i1.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT);
            image.setIcon(new ImageIcon(i2));
        } catch (Exception e) {
            image.setText("Bank Logo");
            image.setFont(new Font("Arial", Font.ITALIC, 12));
            image.setForeground(Color.RED);
        }
        image.setBounds(20, 10, 80, 80);
        header.add(image);
        
        JLabel title = new JLabel("Opening Bank Account", SwingConstants.CENTER);
        title.setFont(new Font("Raleway", Font.BOLD, 28));
        title.setBounds(100, 10, 400, 40);
        header.add(title);
        
        JLabel formNoLabel = new JLabel("APPLICATION FORM NO. " + APPLICATION_NO, SwingConstants.CENTER);
        formNoLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        formNoLabel.setForeground(new Color(0, 102, 204));
        formNoLabel.setBounds(100, 55, 400, 30);
        header.add(formNoLabel);
        
        return header;
    }
    
    // --- UI HELPER: Initialize and style buttons ---
    private void initializeButtons() {
        prevBtn = createStyledButton("<< Previous", Color.GRAY); 
        nextBtn = createStyledButton("Next >>", new Color(0, 128, 0));
        submitBtn = createStyledButton("Submit Application", new Color(34, 139, 34));
        cancelBtn = createStyledButton("Cancel", new Color(220, 20, 60));

        prevBtn.setEnabled(false);
        submitBtn.setVisible(false);
    }

    // --- UI HELPER: Creates a styled button ---
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    // --- UI HELPER: Creates the footer button panel ---
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        footer.setBackground(new Color(240, 248, 255));
        footer.add(prevBtn);
        footer.add(nextBtn);
        footer.add(submitBtn);
        footer.add(cancelBtn);
        return footer;
    }
    
    // --- UI HELPER: Creates a styled text field ---
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return field;
    }
    
    // --- UI HELPER: Creates a styled combo box ---
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Arial", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return box;
    }
    
    // --- FORM LOGIC: Validation Methods ---
    private boolean validatePage1() {
        if (fullNameField.getText().trim().isEmpty() || dobPicker.getDate() == null || mobileNoField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields (Full Name, DOB, Mobile No.).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please provide a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private boolean validatePage2() {
        if (isMinor) {
            if (guardianNameField.getText().trim().isEmpty() || dobCertificatePath == null || minorSignaturePath == null || guardianCitizenshipPath == null || guardianSignaturePath == null) {
                JOptionPane.showMessageDialog(this, "All guardian and minor documentation (DOB Cert, Applicant Signature, Guardian ID, Guardian Signature) are mandatory for minors.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (citizenshipField.getText().trim().isEmpty() || adultIdPath == null) {
                JOptionPane.showMessageDialog(this, "Citizenship ID and ID document upload are required for adults.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private boolean validatePage3() {
        try {
            double amount = Double.parseDouble(depositAmountField.getText());
            if (amount < 1000) { 
                JOptionPane.showMessageDialog(this, "Initial deposit must be at least 1000.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Initial Deposit Amount must be a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private boolean validatePage4() {
        if (passportPhotoPath == null || signaturePath == null) {
            JOptionPane.showMessageDialog(this, "Applicant's Passport Photo and Signature are mandatory for submission.", "Document Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!termsBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "You must agree to the terms and conditions.", "Terms Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // --- FORM LOGIC: Page Transition and Visibility ---
    private void handleNextButton() {
        if (currentPage == 0 && !validatePage1()) return;
        if (currentPage == 1 && !validatePage2()) return;
        if (currentPage == 2 && !validatePage3()) return;
        
        if (currentPage == 0) updatePage2();
        
        showPage(currentPage + 1);
    }

    private void showPage(int page) {
        if (page < 0 || page > 3) return;
        
        currentPage = page;
        cardLayout.show(mainPanel, "Page" + (page + 1));
        
        prevBtn.setEnabled(page > 0);
        nextBtn.setVisible(page < 3);
        submitBtn.setVisible(page == 3);
        
        if (page == 3 && termsBox != null) {
            submitBtn.setEnabled(termsBox.isSelected() && validateDocumentsForSubmission());
        }
        
        setTitle("Opening Bank Account - Form No: " + APPLICATION_NO + " - Page " + (page + 1) + " of 4");
    }
    
    private boolean validateDocumentsForSubmission() {
        return passportPhotoPath != null && signaturePath != null;
    }

    // --- FORM LOGIC: Page Creation using ABSOLUTE POSITIONING ---
    private void createPages() {
        
        // --- Page 1: Personal & Contact Information ---
        pages[0] = new JPanel(null); 
        pages[0].setBackground(Color.WHITE);
        int y_pos = 30;

        JLabel page1Header = new JLabel("PAGE 1", SwingConstants.CENTER);
        page1Header.setFont(new Font("Arial", Font.BOLD, 18));
        page1Header.setBounds(200, y_pos, 250, 25);
        pages[0].add(page1Header);
        y_pos += 30;
        
        JLabel page1Subtitle = new JLabel("Personal and Contact Information", SwingConstants.CENTER);
        page1Subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        page1Subtitle.setBounds(150, y_pos, 350, 20);
        pages[0].add(page1Subtitle);
        y_pos += LINE_SPACING;

        // Fields setup
        y_pos = addFormRow(pages[0], y_pos, "Full Name (Applicant):", fullNameField = createStyledTextField());
        y_pos = addFormRow(pages[0], y_pos, "Father's Name:", fatherNameField = createStyledTextField());
        y_pos = addFormRow(pages[0], y_pos, "Mother's Name:", motherNameField = createStyledTextField());
        
        // Gender 
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        genderLabel.setBounds(LABEL_X, y_pos, 200, FIELD_HEIGHT);
        pages[0].add(genderLabel);
        
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        genderPanel.setBounds(FIELD_X, y_pos, FIELD_WIDTH, FIELD_HEIGHT); 
        genderPanel.setBackground(Color.WHITE);
        male = new JRadioButton("Male"); female = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(male); genderGroup.add(female);
        genderPanel.add(male); genderPanel.add(female);
        pages[0].add(genderPanel);
        y_pos += LINE_SPACING;
        
        // Date of Birth 
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dobLabel.setBounds(LABEL_X, y_pos, 200, FIELD_HEIGHT);
        pages[0].add(dobLabel);
        
        dobPicker = new DatePicker(); 
        dobPicker.setBounds(FIELD_X, y_pos, FIELD_WIDTH, FIELD_HEIGHT); 
        pages[0].add(dobPicker);
        y_pos += LINE_SPACING;

        // Marital Status 
        JLabel maritalLabel = new JLabel("Marital Status:");
        maritalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        maritalLabel.setBounds(LABEL_X, y_pos, 200, FIELD_HEIGHT);
        pages[0].add(maritalLabel);
        
        JPanel maritalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        maritalPanel.setBounds(FIELD_X, y_pos, FIELD_WIDTH, FIELD_HEIGHT); 
        maritalPanel.setBackground(Color.WHITE);
        married = new JRadioButton("Married"); unmarried = new JRadioButton("Unmarried");
        ButtonGroup maritalGroup = new ButtonGroup();
        maritalGroup.add(married); maritalGroup.add(unmarried);
        maritalPanel.add(married); maritalPanel.add(unmarried);
        pages[0].add(maritalPanel);
        y_pos += LINE_SPACING;
        
        y_pos = addFormRow(pages[0], y_pos, "Email Address:", emailField = createStyledTextField());
        y_pos = addFormRow(pages[0], y_pos, "Mobile Number:", mobileNoField = createStyledTextField());
        
        // Address Details
        y_pos = addFormRow(pages[0], y_pos, "Street/Tole Address:", streetAddressField = createStyledTextField());
        y_pos = addFormRow(pages[0], y_pos, "District:", districtField = createStyledTextField());
        y_pos = addFormRow(pages[0], y_pos, "Province/Zone:", provinceField = createStyledTextField());
        
        mainPanel.add(pages[0], "Page1");

        // --- Page 2: Applicant Status (Dynamic) ---
        pages[1] = new JPanel(null);
        pages[1].setBackground(Color.WHITE);
        mainPanel.add(pages[1], "Page2");

        // --- Page 3: Account & Financial Profile ---
        pages[2] = new JPanel(null);
        pages[2].setBackground(Color.WHITE);
        int y_pos_p3 = 20;
        
        JLabel subtitle3 = new JLabel("PAGE 3", SwingConstants.CENTER);
        subtitle3.setFont(new Font("Arial", Font.BOLD, 18));
        subtitle3.setBounds(200, y_pos_p3, 250, 25);
        pages[2].add(subtitle3);
        y_pos_p3 += 30;
        
        JLabel page3Subtitle = new JLabel("Account and Financial Details", SwingConstants.CENTER);
        page3Subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        page3Subtitle.setBounds(150, y_pos_p3, 350, 20);
        pages[2].add(page3Subtitle);
        y_pos_p3 += LINE_SPACING;

        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Account Type:", accountTypeBox = createStyledComboBox(new String[]{"Saving Account", "Current Account", "Fixed Deposit Account"}));
        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Purpose of Account:", purposeBox = createStyledComboBox(new String[]{"Saving", "Business", "Investment", "Others"}));
        
        // NOTE: Currency box is defined with full names, but we will extract the 3-letter code for DB insertion.
        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Account Currency:", currencyBox = createStyledComboBox(new String[]{"NPR - Nepali Rupees", "INR - Indian Rupees", "USD - US Dollar"}));
        
        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Branch:", branchBox = createStyledComboBox(new String[]{"Kawasoti Branch", "Kathmandu Branch", "Pokhara Branch"}));
        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Deposit Mode:", depositModeBox = createStyledComboBox(new String[]{"Cash", "Cheque", "Transfer"}));
        y_pos_p3 = addFormRow(pages[2], y_pos_p3, "Initial Deposit (Min 1000):", depositAmountField = createStyledTextField());
        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Occupation:", occupationBox = createStyledComboBox(new String[]{"Student", "Business", "Farmer/Agriculture", "Salaried"}));
        y_pos_p3 = addFormRow(pages[2], y_pos_p3, "Annual Income:", annualIncomeField = createStyledTextField());
        y_pos_p3 = addComboRow(pages[2], y_pos_p3, "Source of Funds:", sourceBox = createStyledComboBox(new String[]{"Salary", "Business Profit", "Inheritance", "Savings"}));

        mainPanel.add(pages[2], "Page3");

        // --- Page 4: Services and Documents ---
        pages[3] = new JPanel(null);
        pages[3].setBackground(Color.WHITE);
        y_pos = 20;

        JLabel page4Header = new JLabel("PAGE 4", SwingConstants.CENTER);
        page4Header.setFont(new Font("Arial", Font.BOLD, 18));
        page4Header.setBounds(200, y_pos, 250, 25);
        pages[3].add(page4Header);
        y_pos += 30;
        
        JLabel page4Subtitle = new JLabel("Required Services and Submission", SwingConstants.CENTER);
        page4Subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        page4Subtitle.setBounds(150, y_pos, 350, 20);
        pages[3].add(page4Subtitle);
        y_pos += LINE_SPACING;

        // Row 1: Services (2x2 layout using absolute positioning)
        y_pos = addLabel(pages[3], y_pos, "Required Services:"); 
        
        atmCardBox = new JCheckBox("ATM Card"); 
        mobileBankingBox = new JCheckBox("Mobile Banking"); 
        chequeBookBox = new JCheckBox("Cheque Book"); 
        emailAlertsBox = new JCheckBox("EMAIL alerts");

        // Row 1: ATM Card | Mobile Banking
        atmCardBox.setBounds(FIELD_X, y_pos, 150, FIELD_HEIGHT);
        mobileBankingBox.setBounds(FIELD_X + 170, y_pos, 150, FIELD_HEIGHT);
        pages[3].add(atmCardBox); pages[3].add(mobileBankingBox);
        y_pos += 30; 

        // Row 2: Cheque Book | Email Alerts
        chequeBookBox.setBounds(FIELD_X, y_pos, 150, FIELD_HEIGHT);
        emailAlertsBox.setBounds(FIELD_X + 170, y_pos, 150, FIELD_HEIGHT);
        pages[3].add(chequeBookBox); pages[3].add(emailAlertsBox);
        y_pos += LINE_SPACING; 

        // Row 3: Photo (Applicant)
        y_pos = addDocumentRow(pages[3], y_pos, "Passport Photo (Applicant):", path -> passportPhotoPath = path, photoStatusLabel);

        // Row 4: Signature (Applicant)
        y_pos = addDocumentRow(pages[3], y_pos, "Signature Photo (Applicant):", path -> signaturePath = path, signatureStatusLabel);
        
        y_pos += LINE_SPACING;
        
        // Row 5: Terms
        y_pos = addLabel(pages[3], y_pos, "Terms and Conditions:");
        termsBox = new JCheckBox("I agree to the terms and conditions");
        termsBox.setBounds(FIELD_X, y_pos, FIELD_WIDTH, FIELD_HEIGHT);
        termsBox.setBackground(Color.WHITE);
        termsBox.addActionListener(e -> submitBtn.setEnabled(termsBox.isSelected() && validateDocumentsForSubmission()));
        pages[3].add(termsBox);
        y_pos += LINE_SPACING;
        
        mainPanel.add(pages[3], "Page4");
    }

    // --- UI HELPER: Adds Label and Text Field row using absolute positioning ---
    private int addFormRow(JPanel panel, int y_pos, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setBounds(LABEL_X, y_pos, 200, FIELD_HEIGHT);
        panel.add(label);
        
        field.setBounds(FIELD_X, y_pos, FIELD_WIDTH, FIELD_HEIGHT);
        panel.add(field);
        
        return y_pos + LINE_SPACING;
    }
    
    // --- UI HELPER: Adds Label and Combo Box row using absolute positioning ---
    private int addComboRow(JPanel panel, int y_pos, String labelText, JComboBox<String> combo) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setBounds(LABEL_X, y_pos, 200, FIELD_HEIGHT);
        panel.add(label);
        
        combo.setBounds(FIELD_X, y_pos, FIELD_WIDTH, FIELD_HEIGHT);
        panel.add(combo);
        
        return y_pos + LINE_SPACING;
    }

    // --- UI HELPER: Adds Label only for specific components ---
    private int addLabel(JPanel panel, int y_pos, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setBounds(LABEL_X, y_pos, 200, FIELD_HEIGHT);
        panel.add(label);
        return y_pos; 
    }
    
    // --- FORM LOGIC: Dynamic Page 2 Content Update ---
    private void updatePage2() {
        pages[1].removeAll();
        int y_pos = 20;
        
        JLabel page2Header = new JLabel("PAGE 2", SwingConstants.CENTER);
        page2Header.setFont(new Font("Arial", Font.BOLD, 18));
        page2Header.setBounds(200, y_pos, 250, 25);
        pages[1].add(page2Header);
        y_pos += 30;
        
        JLabel page2Subtitle = new JLabel("Applicant Status and Documents", SwingConstants.CENTER);
        page2Subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        page2Subtitle.setBounds(150, y_pos, 350, 20);
        pages[1].add(page2Subtitle);
        y_pos += LINE_SPACING;
        
        LocalDate dob = dobPicker.getDate();
        int age = (dob != null) ? Period.between(dob, LocalDate.now()).getYears() : 0;
        isMinor = age < 18;

        String statusText = isMinor ? 
            "<html><p style='color:red;'>Applicant Status: MINOR (Age: " + age + ")</p></html>" : 
            "<html><p style='color:green;'>Applicant Status: ADULT (Age: " + age + ")</p></html>";

        JLabel statusLabel = new JLabel(statusText, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setBounds(FIELD_X - 50, y_pos, 400, 25);
        pages[1].add(statusLabel);
        y_pos += LINE_SPACING;

        if (isMinor) {
            y_pos = addFormRow(pages[1], y_pos, "Guardian Name:", guardianNameField = createStyledTextField());
            y_pos = addFormRow(pages[1], y_pos, "Guardian Relationship:", guardianRelationField = createStyledTextField());
            
            y_pos = addDocumentRow(pages[1], y_pos, "Applicant DOB Certificate:", path -> dobCertificatePath = path, dobCertStatusLabel);
            y_pos = addDocumentRow(pages[1], y_pos, "Applicant Signature:", path -> minorSignaturePath = path, minorSignStatusLabel);
            y_pos = addDocumentRow(pages[1], y_pos, "Guardian Citizenship Doc:", path -> guardianCitizenshipPath = path, guardianCitizenStatusLabel);
            y_pos = addDocumentRow(pages[1], y_pos, "Guardian Signature Doc:", path -> guardianSignaturePath = path, guardianSignStatusLabel); 

            citizenshipField = new JTextField(); adultIdPath = null;
        } else {
            y_pos = addFormRow(pages[1], y_pos, "Citizenship/ID Number:", citizenshipField = createStyledTextField());
            y_pos = addDocumentRow(pages[1], y_pos, "Citizenship/ID Photo (Applicant):", path -> adultIdPath = path, adultIdStatusLabel);

            guardianNameField = new JTextField(); guardianRelationField = new JTextField();
            dobCertificatePath = null; minorSignaturePath = null; guardianCitizenshipPath = null; guardianSignaturePath = null;
        }
        
        pages[1].revalidate();
        pages[1].repaint();
    }
    
    // --- UI HELPER: Adds a document upload row for dynamic pages ---
    private int addDocumentRow(JPanel panel, int y_pos, String labelText, java.util.function.Consumer<String> pathSetter, JLabel statusLabel) {
        y_pos = addLabel(panel, y_pos, labelText); 
        
        JButton uploadBtn = createStyledButton("Upload Document", new Color(100, 149, 237));
        uploadBtn.setBounds(FIELD_X, y_pos, 180, FIELD_HEIGHT); 
        
        statusLabel.setBounds(FIELD_X + 190, y_pos, 180, FIELD_HEIGHT);
        statusLabel.setForeground(statusLabel.getText().contains("Uploaded") ? new Color(0, 128, 0) : Color.RED);
        
        uploadBtn.addActionListener(e -> updateFileStatus(labelText, statusLabel, pathSetter));
        
        panel.add(uploadBtn);
        panel.add(statusLabel);
        return y_pos + LINE_SPACING; 
    }

    // --- FORM LOGIC: Unified File Selection and Status Update ---
    private void updateFileStatus(String docType, JLabel statusLabel, java.util.function.Consumer<String> pathSetter) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select " + docType + " File (JPEG/PNG)");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            pathSetter.accept(path); 
            
            statusLabel.setText("Status: Uploaded");
            statusLabel.setForeground(new Color(0, 128, 0));
            
            JOptionPane.showMessageDialog(this, docType + " uploaded successfully.", "Upload Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            pathSetter.accept(null);
            statusLabel.setText("Status: Not Uploaded");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void submitForm() {
        // 1. Final Client-Side Validation
        if (!validatePage1() || !validatePage2() || !validatePage3() || !validatePage4()) {
            return;
        }
        
        double initialDeposit;
        try {
            initialDeposit = Double.parseDouble(depositAmountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Initial Deposit Amount must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Data Preparation
        String emailToCheck = emailField.getText().trim();
        String accountNumber = "ACC" + (new Random().nextInt(900000) + 100000); 
        String defaultPin = "1234"; // DEFAULT LOGIN PIN FOR CUSTOMER
        
        String selectedCurrency = (String) currencyBox.getSelectedItem();
        String currencyCode = selectedCurrency != null && selectedCurrency.length() >= 3 ? selectedCurrency.substring(0, 3) : "NPR";
        
        String services = "";
        if(atmCardBox.isSelected()) services += "ATM Card,";
        if(mobileBankingBox.isSelected()) services += "Mobile Banking,";
        if(chequeBookBox.isSelected()) services += "Cheque Book,";
        int leaves = 0;
                    if (chequeBookBox.isSelected()) {
                        leaves = 5; 
                    }
        if(emailAlertsBox.isSelected()) services += "EMAIL alerts,";
        if (!services.isEmpty()) services = services.substring(0, services.length() - 1);

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // --- START TRANSACTION (Safety Feature) ---
            con.setAutoCommit(false); 

            try {
                // 2. INSERT INTO ACCOUNTS TABLE
                String queryAccount = "INSERT INTO Accounts (application_form_no, account_number, full_name, father_name, mother_name, gender, dob, marital_status, email, phone, street_address, district, province, is_minor, guardian_name, guardian_relation, citizenship_id, minor_dob_cert, minor_signature, guardian_citizenship_doc, guardian_signature_doc, account_type, purpose, currency, branch, initial_deposit_mode, initial_deposit_amount, occupation, annual_income, source_of_funds, services, photo_path, signature_path, terms_agreed, balance, status, creation_time, opened_by_user_id, cheque_leaves_remaining) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement pst = con.prepareStatement(queryAccount);
                pst.setString(1, APPLICATION_NO); 
                pst.setString(2, accountNumber); 
                pst.setString(3, fullNameField.getText());
                pst.setString(4, fatherNameField.getText());
                pst.setString(5, motherNameField.getText());
                pst.setString(6, male.isSelected() ? "Male" : (female.isSelected() ? "Female" : "Other"));
                pst.setDate(7, Date.valueOf(dobPicker.getDate()));
                pst.setString(8, married.isSelected() ? "Married" : (unmarried.isSelected() ? "Unmarried" : "Single"));
                pst.setString(9, emailToCheck); 
                pst.setString(10, mobileNoField.getText());
                pst.setString(11, streetAddressField.getText());
                pst.setString(12, districtField.getText());
                pst.setString(13, provinceField.getText());
                pst.setBoolean(14, isMinor);
                pst.setString(15, isMinor ? guardianNameField.getText() : null);
                pst.setString(16, isMinor ? guardianRelationField.getText() : null);
                pst.setString(17, isMinor ? null : citizenshipField.getText()); 
                pst.setString(18, isMinor ? dobCertificatePath : null);
                pst.setString(19, isMinor ? minorSignaturePath : null);
                pst.setString(20, isMinor ? guardianCitizenshipPath : null);
                pst.setString(21, isMinor ? guardianSignaturePath : null); 
                pst.setString(22, (String) accountTypeBox.getSelectedItem());
                pst.setString(23, (String) purposeBox.getSelectedItem());
                pst.setString(24, currencyCode); 
                pst.setString(25, (String) branchBox.getSelectedItem());
                pst.setString(26, (String) depositModeBox.getSelectedItem());
                pst.setDouble(27, initialDeposit);
                pst.setString(28, (String) occupationBox.getSelectedItem());
                pst.setString(29, annualIncomeField.getText());
                pst.setString(30, (String) sourceBox.getSelectedItem());
                pst.setString(31, services);
                pst.setString(32, passportPhotoPath);
                pst.setString(33, signaturePath);
                pst.setBoolean(34, termsBox.isSelected());
                pst.setDouble(35, initialDeposit); 
                pst.setString(36, "Active"); 
                pst.setTimestamp(37, new Timestamp(System.currentTimeMillis()));
                pst.setString(38, "T-452"); 
                pst.setInt(39, leaves);
                pst.executeUpdate();

                // 3. INSERT INTO USERS TABLE (The Login Key)
                String queryUser = "INSERT INTO Users (user_id, password, pin, role, status, account_number) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstUser = con.prepareStatement(queryUser)) {
                    pstUser.setString(1, accountNumber); // Using Account Number as Login ID
                    pstUser.setString(2, defaultPin);   // Password: 1234
                    pstUser.setString(3, defaultPin);   // PIN: 1234
                    pstUser.setString(4, "Customer");
                    pstUser.setString(5, "Active");
                    pstUser.setString(6, accountNumber); // BRIDGE LINK
                    pstUser.executeUpdate();
                }

             // 4. INSERT INTO TRANSACTIONS TABLE
             String queryTransaction = "INSERT INTO Transactions (account_number, transaction_type, amount, deposit_mode, cheque_number, issuing_bank) VALUES (?, ?, ?, ?, ?, ?)";
             try (PreparedStatement pstTransaction = con.prepareStatement(queryTransaction)) {
                 pstTransaction.setString(1, accountNumber);
                 pstTransaction.setString(2, "Deposit");
                 pstTransaction.setDouble(3, initialDeposit);
                 String depositMode = (String) depositModeBox.getSelectedItem();
                 pstTransaction.setString(4, depositMode);

                 if ("Cheque".equalsIgnoreCase(depositMode)) {
                     pstTransaction.setString(5, chequeNumberField.getText());
                     pstTransaction.setString(6, issuingBankField.getText());
                 } else {
                     pstTransaction.setNull(5, java.sql.Types.VARCHAR);
                     pstTransaction.setNull(6, java.sql.Types.VARCHAR);
                 }
                 pstTransaction.executeUpdate();
             }

             // 5. AUTO-ASSIGN 5 CHEQUE LEAVES & COLLECT NUMBERS
             StringBuilder chequeList = new StringBuilder();
             String queryCheques = "INSERT INTO cheque_leaves (cheque_no, account_number, status, is_free) VALUES (?, ?, 'UNUSED', true)";
             try (PreparedStatement pstCheque = con.prepareStatement(queryCheques)) {
                 // Generate a starting cheque number
                 int startCheque = 100001 + (new java.util.Random().nextInt(90000)); 

                 for (int i = 0; i < 5; i++) {
                     int currentChq = startCheque + i;
                     pstCheque.setInt(1, currentChq);
                     pstCheque.setString(2, accountNumber);
                     pstCheque.addBatch();

                     // Add to our list for the success message
                     chequeList.append(" - ").append(currentChq).append("\n");
                 }
                 pstCheque.executeBatch();
             }

             // COMMIT ALL INSERTS
             con.commit();

             // Success Message with Cheque Details
             String successMessage = "Account Opened Successfully!\n" +
                                      "Account Number: " + accountNumber + "\n" +
                                      "Customer Login ID: " + accountNumber + "\n" +
                                      "Default PIN: " + defaultPin + "\n\n" +
                                      "Assigned Cheque Numbers:\n" + chequeList.toString() +
                                      "\n(Please inform the customer of their cheque range.)";
             
             // 2. Add the Log
             AuditService.log(tellerId, "ACCOUNT_OPENING", "Created new account for " + fullNameField.getText());

             JOptionPane.showMessageDialog(this, successMessage, "Account Created Successfully!", JOptionPane.INFORMATION_MESSAGE);


             this.dispose();

            } catch (SQLException e) {
                con.rollback(); // Undo everything if one part fails
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

