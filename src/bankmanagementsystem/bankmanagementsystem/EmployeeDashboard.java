package bankmanagementsystem;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class EmployeeDashboard extends JFrame {

    private String role, employeeId;
    private final Color SIDEBAR_BG = new Color(30, 39, 46);
    private final Color MAIN_BG = new Color(241, 242, 246);
    private final Color TEXT_LIGHT = new Color(241, 242, 246);
    
    private JPanel mainGrid;
    private JLabel lblTime;
    private JButton notifBtn; 

    public EmployeeDashboard(String role, String employeeId) {
        this.role = role;
        this.employeeId = employeeId;

        setTitle("NEPAL Bank System - " + employeeId);
        setSize(1200, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);       
        add(createSidebar(), BorderLayout.WEST);
       
        mainGrid = new JPanel(new GridLayout(3, 4, 20, 20)); // Adjusted for more space
        mainGrid.setBackground(MAIN_BG);
        mainGrid.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initializeCards();
        add(mainGrid, BorderLayout.CENTER);

        startClock();
        updateNotificationBadge(); 
        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

      
        JLabel userInfo = new JLabel("<html><b>User:</b> " + employeeId + " | <b>Role:</b> " + role.toUpperCase() + " | <b>Teller ID:</b> T-452</html>");
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(userInfo, BorderLayout.WEST);

      
        JLabel title = new JLabel("EMPLOYEE DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(SIDEBAR_BG);
        header.add(title, BorderLayout.CENTER);
        
       
        lblTime = new JLabel();
        lblTime.setFont(new Font("Monospaced", Font.BOLD, 14));
        lblTime.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));
        header.add(lblTime, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 15, 25, 15));

        JLabel logo = new JLabel(" NEPAL BANK");
        logo.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        notifBtn = createNavButton(" Notifications", e -> {
            new NotificationPanel(employeeId).setVisible(true);
            updateNotificationBadge(); 
        });
        sidebar.add(notifBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        
        sidebar.add(createNavButton(" Daily Logs", e -> new DailyLogsForm(employeeId).setVisible(true)));
        
        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = createNavButton(" Secure Logout", e -> {

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });
        
        logoutBtn.setBackground(new Color(235, 77, 75));
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void updateNotificationBadge() {
        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dashboard_notifications WHERE status = 'PENDING'")) {
            if (rs.next() && rs.getInt(1) > 0) {
                notifBtn.setText(" Notifications (" + rs.getInt(1) + ")");
                notifBtn.setForeground(new Color(255, 71, 87)); 
            } else {
                notifBtn.setText(" Notifications");
                notifBtn.setForeground(TEXT_LIGHT);
            }
        } catch (Exception e) { System.err.println("Badge Error"); }
    }

    private void initializeCards() {
        mainGrid.add(createActionCard("Account Opening", "New customer profile", new Color(46, 204, 113), e -> new AccountOpeningForm().setVisible(true)));
        mainGrid.add(createActionCard("Deposit Money", "Deposit money Cash/Cheque", new Color(52, 152, 219), e -> new DepositForm().setVisible(true)));
        mainGrid.add(createActionCard("Withdraw Money", "Withdraw money Cash/Cheque", new Color(230, 126, 34), e -> new WithdrawForm(employeeId).setVisible(true)));
        mainGrid.add(createActionCard("Funds Transfer", "Transfer money one account to another account", new Color(155, 89, 182), e -> new TransferForm(employeeId).setVisible(true)));
        mainGrid.add(createActionCard("Statements", "Ledger history", new Color(52, 73, 94), e -> new StatementView().setVisible(true)));
        mainGrid.add(createActionCard("Balance Enquiry", "Real-time check", new Color(26, 188, 156), e -> new BalanceEnquiry("").setVisible(true)));
        mainGrid.add(createActionCard("Loan Appplication", "Credit processing", new Color(231, 76, 60), e -> new LoanApplicationForm().setVisible(true)));
        mainGrid.add(createActionCard("Customer Lookup", "Database search", new Color(127, 140, 141), e -> new AccountSearchForm().setVisible(true)));
        mainGrid.add(createActionCard("Loan Repayment", "Process installment", new Color(39, 174, 96), e -> new LoanRepaymentForm(employeeId).setVisible(true)));
    }

    private JPanel createActionCard(String title, String subtitle, Color barColor, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JPanel topBar = new JPanel();
        topBar.setBackground(barColor);
        topBar.setPreferredSize(new Dimension(0, 4));
        card.add(topBar, BorderLayout.NORTH);
        JPanel content = new JPanel(new GridLayout(2, 1));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        content.add(lblTitle);
        content.add(lblSub);
        card.add(content, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(250, 250, 250)); card.setBorder(new LineBorder(barColor, 1)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); card.setBorder(new LineBorder(new Color(230, 230, 230), 1)); }
            public void mousePressed(MouseEvent e) { action.actionPerformed(null); }
        });
        return card;
    }

    private JButton createNavButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(TEXT_LIGHT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private void startClock() {
        new Timer(1000, e -> lblTime.setText(new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss").format(new Date()))).start();
    }
}


