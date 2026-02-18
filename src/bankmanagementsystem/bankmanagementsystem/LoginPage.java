package bankmanagementsystem;

import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginPage extends JFrame {

    private final Color PRIMARY_BLUE = new Color(10, 61, 98);     
    private final Color SECONDARY_BLUE = new Color(30, 96, 145);  
    private final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private final Color INPUT_BG = new Color(241, 245, 249);

    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginBtn;

    public LoginPage() {
        setTitle("Nepal Bank - Secure Login");
        setSize(900, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
               
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new GridBagLayout());
      
        JPanel loginCard = new JPanel();
        loginCard.setPreferredSize(new Dimension(400, 520));
        loginCard.setBackground(Color.WHITE);
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
      
        JPanel blueHeader = new JPanel();
        blueHeader.setBackground(PRIMARY_BLUE);
        blueHeader.setMaximumSize(new Dimension(400, 80));
        JLabel title = new JLabel("NEPAL BANK");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        blueHeader.add(title);
       
        JPanel formContent = new JPanel();
        formContent.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        formContent.setBackground(Color.WHITE);
        formContent.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel welcomeMsg = new JLabel("Welcome back! Please login.");
        welcomeMsg.setForeground(new Color(100, 116, 139));
        welcomeMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        idField = createStyledTextField();
        passwordField = createStyledPasswordField();
        
        loginBtn = createStyledButton("LOGIN", SECONDARY_BLUE);

        JLabel footerNote = new JLabel("Contact Nepal Bank for account registration.");
        footerNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerNote.setForeground(new Color(100, 116, 139));
        
        formContent.add(welcomeMsg);
        formContent.add(createLabel("Identification Number (Staff/User ID)"));
        formContent.add(idField);
        formContent.add(createLabel("Secret PIN / Password"));
        formContent.add(passwordField);
        formContent.add(Box.createRigidArea(new Dimension(0, 20)));
        formContent.add(loginBtn);
        formContent.add(Box.createRigidArea(new Dimension(0, 20)));
        formContent.add(footerNote);
       
        loginCard.add(blueHeader);
        loginCard.add(formContent);
        add(loginCard); 
            
        loginBtn.addActionListener(e -> attemptLogin());
        setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(320, 40));
        tf.setBackground(INPUT_BG);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(320, 40));
        pf.setBackground(INPUT_BG);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return pf;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setPreferredSize(new Dimension(320, 20));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(320, 45));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void attemptLogin() {
        String inputId = idField.getText();
        String inputPassword = new String(passwordField.getPassword());

        if (inputId.isEmpty() || inputPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String actualRole = null;
        String accountNo = null;       
        String query = "SELECT role, account_number FROM Users WHERE user_id = ? AND (pin = ? OR password = ?)";

        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(query)) {
            
            pst.setString(1, inputId);
            pst.setString(2, inputPassword);
            pst.setString(3, inputPassword);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    actualRole = rs.getString("role");
                    accountNo = rs.getString("account_number");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            return;
        }

        if (actualRole == null) {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose(); 
           
        switch (actualRole.toUpperCase()) {
            case "MANAGER":
            case "ADMIN":
                new AdminManagerDashboard(actualRole, inputId).setVisible(true);
                break;
                
            case "EMPLOYEE":
            case "TELLER":
                new EmployeeDashboard(actualRole, inputId).setVisible(true);
                break;
                
            case "CUSTOMER":
                if (accountNo == null || accountNo.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No account linked to this user.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    new CustomerDashboard(accountNo).setVisible(true);
                }
                break;
                
            default:
                JOptionPane.showMessageDialog(null, "Account role configuration error.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
