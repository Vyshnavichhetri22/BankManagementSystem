package bankmanagementsystem;

import bankmanagementsystem.dao.AccountDAO;
import bankmanagementsystem.entities.Account;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AccountSearchForm extends JFrame {
    private JTextField nameSearchField;
    private JTable resultsTable;
    private DefaultTableModel model;
    private AccountDAO accountDAO = new AccountDAO();

    public AccountSearchForm() {
        setTitle("Real-Time Customer Verification System");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- TOP PANEL: Search ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        topPanel.setBackground(new Color(75, 0, 130)); // Indigo Theme
        
        JLabel lbl = new JLabel("Search Customer Name:");
        lbl.setForeground(Color.WHITE);
        nameSearchField = new JTextField(25);
        JButton searchBtn = new JButton("Find Customers");

        topPanel.add(lbl);
        topPanel.add(nameSearchField);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Results Table (Step 1 Verification) ---
        String[] cols = {"Acc No", "Full Name", "Phone", "Email", "Address"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(model);
        resultsTable.setRowHeight(30);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        // --- Event Listeners ---
        searchBtn.addActionListener(e -> performSearch());
        
        // STEP 2: DOUBLE CLICK ACTION
        resultsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double click detected
                    int row = resultsTable.getSelectedRow();
                    String accNo = resultsTable.getValueAt(row, 0).toString();
                    showFullProfile(accNo);
                }
            }
        });
    }

    private void performSearch() {
        String name = nameSearchField.getText().trim();
        model.setRowCount(0);
        List<Account> customers = accountDAO.getAccountByName(name);
        for (Account a : customers) {
            model.addRow(new Object[]{a.getAccountNumber(), a.getFullName(), a.getMobileNo(), a.getEmail(), a.getStreetAddress()});
        }
    }

    // Step 2: Full Verification Popup
    private void showFullProfile(String accNo) {
        Account acc = accountDAO.getAccountByNumber(accNo);
        if (acc == null) return;

        JDialog verifyDialog = new JDialog(this, "Identity Confirmation", true);
        verifyDialog.setSize(700, 480);
        verifyDialog.setLayout(new BorderLayout(15, 15));
        verifyDialog.setLocationRelativeTo(this);

        // Left Side: Photo
        JLabel lblPhoto = new JLabel("PHOTO MISSING", SwingConstants.CENTER);
        lblPhoto.setPreferredSize(new Dimension(250, 250));
        lblPhoto.setBorder(BorderFactory.createTitledBorder("Customer Profile"));

        if (acc.getPhotoPath() != null) {
            String cleanPath = acc.getPhotoPath().replace("\\", "/");
            if (new java.io.File(cleanPath).exists()) {
                ImageIcon img = new ImageIcon(new ImageIcon(cleanPath).getImage()
                    .getScaledInstance(250, 250, Image.SCALE_SMOOTH));
                lblPhoto.setIcon(img);
                lblPhoto.setText("");
            }
        }

        // Center: Details
        JTextArea txtDetails = new JTextArea();
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.BOLD, 13));
        txtDetails.setText(
            "  === ACCOUNT IDENTITY ===\n" +
            "  Name       : " + acc.getFullName() + "\n" +
            "  Acc Number : " + acc.getAccountNumber() + "\n" +
            "  Status     : " + acc.getStatus() + "\n" +
            "  Balance    : Rs. " + acc.getBalance() + "\n\n" +
            "  === LOAN DETAILS ===\n" +
            "  Type       : " + (acc.getloanType() != null ? acc.getloanType() : "N/A") + "\n" +
            "  Amount     : Rs. " + acc.getLoanAmount() + "\n" +
            "  Outstanding: Rs. " + acc.getOutstandingEmi() + "\n" +
            "  EMI        : Rs. " + acc.getEmi() + "\n" +
            "  Loan Status: " + (acc.getloanStatus() != null ? acc.getloanStatus() : "NO LOAN")
        );

        verifyDialog.add(lblPhoto, BorderLayout.WEST);
        verifyDialog.add(new JScrollPane(txtDetails), BorderLayout.CENTER);

        JButton btnConfirm = new JButton("VERIFIED - PROCEED");
        btnConfirm.addActionListener(x -> verifyDialog.dispose());
        verifyDialog.add(btnConfirm, BorderLayout.SOUTH);

        verifyDialog.setVisible(true);
    }
    
}
