package bankmanagementsystem;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import bankmanagementsystem.dao.LoanDAO; 
import java.io.File;

public class AdminManagerDashboard extends JFrame {

    private final String adminId;
    private JLabel vaultLabel, pendingLabel, loanLabel, lblTotalAssets;
    private JPanel displayArea;
    private JButton withdrawNavBtn;
    
    private JButton btnTransactionAudit;
    private JButton btnSystemSettings;
       
    private final Color SIDEBAR_COLOR = new Color(15, 23, 42);
    private final Color ACCENT_COLOR = new Color(37, 99, 235);
    private final Color BG_COLOR = new Color(241, 245, 249);

    public AdminManagerDashboard(String role, String inputId) {
        this.adminId = inputId;
        
        setTitle("Nepal Bank | Manager/Admin Management Dashboard");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
            
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_COLOR);      
        root.add(createSidebar(), BorderLayout.WEST);
      
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setOpaque(false);
        mainWrapper.add(createTopHeader(), BorderLayout.NORTH);
        
        displayArea = new JPanel(new BorderLayout(0, 20));
        displayArea.setOpaque(false);
        displayArea.setBorder(new EmptyBorder(25, 35, 25, 35));
              
        showOverview();

        mainWrapper.add(displayArea, BorderLayout.CENTER);
        root.add(mainWrapper, BorderLayout.CENTER);

        add(root);
    
        startLiveSync();        
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logo = new JLabel("NEPAL BANK");
        logo.setFont(new Font("Inter", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));
       
        JButton btnOverview = createNavBtn("Overview", "📊");
        JButton btnLoanApprovals = createNavBtn("Loan Approvals", "💰");
        withdrawNavBtn = createNavBtn("Withdraw Requests", "🔔");
        btnTransactionAudit = createNavBtn("Transaction Audit", "🔍"); 
//        btnSystemSettings = createNavBtn("System Settings", "⚙️");     
      
        sidebar.add(btnOverview);
        sidebar.add(btnLoanApprovals);
        sidebar.add(withdrawNavBtn);
        sidebar.add(btnTransactionAudit);
//        sidebar.add(btnSystemSettings);
        
        btnTransactionAudit.addActionListener(e -> openMasterAudit());      
//        btnSystemSettings.addActionListener(e -> openSystemSettings());

        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("Secure Logout");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> { 
//            new LoginPage().setVisible(true); dispose(); });
//        sidebar.add(logout);
//        return sidebar;

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });
        sidebar.add(logout);
        return sidebar;
    }
        
    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel title = new JLabel("   MANAGER DASHBOARD");
        title.setFont(new Font("Inter", Font.BOLD, 32));
        header.add(title, BorderLayout.CENTER);

        JLabel user = new JLabel("Authorized: " + adminId + "  (Admin)   ");
        user.setForeground(Color.GRAY);
        header.add(user, BorderLayout.EAST);

        return header;
    }

    private void showOverview() {
        displayArea.removeAll();
        
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 25, 0));
        kpiRow.setOpaque(false);

        vaultLabel = new JLabel("NPR 0.00", SwingConstants.CENTER);
        pendingLabel = new JLabel("0", SwingConstants.CENTER);
        loanLabel = new JLabel("NPR 0.00", SwingConstants.CENTER);

        kpiRow.add(createKpiCard("TOTAL BANK VAULT", vaultLabel, new Color(37, 99, 235)));
        kpiRow.add(createKpiCard("PENDING APPROVALS", pendingLabel, new Color(220, 38, 38)));
        kpiRow.add(createKpiCard("TOTAL LOAN ASSETS", loanLabel, new Color(22, 163, 74)));

        displayArea.add(kpiRow, BorderLayout.NORTH);
             
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(new Color(226, 232, 240), 1));
     
        JLabel tableTitle = new JLabel("  Live System Audit Log (Recent Transactions)");
        tableTitle.setFont(new Font("Inter", Font.BOLD, 15));
        tableTitle.setBorder(new EmptyBorder(15, 10, 15, 10));
        tablePanel.add(tableTitle, BorderLayout.NORTH);
        
        String[] columns = {"Time", "Account No", "Type", "Amount", "Teller"};
        DefaultTableModel auditModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable auditTable = new JTable(auditModel);     
        auditTable.setRowHeight(35);
        auditTable.setGridColor(new Color(241, 245, 249));
        auditTable.getTableHeader().setBackground(new Color(248, 250, 252));
        auditTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));

        tablePanel.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        displayArea.add(tablePanel, BorderLayout.CENTER);
       
        startTransactionHeartbeat(auditModel); 
        
        displayArea.revalidate();
        displayArea.repaint();
    }

    private JPanel createKpiCard(String title, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        
        valLabel.setFont(new Font("Inter", Font.BOLD, 28));
        valLabel.setForeground(new Color(15, 23, 42));
        
        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("Inter", Font.BOLD, 12));
        tLabel.setForeground(Color.GRAY);
        tLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 10, 20, 10));
        p.add(tLabel);
        p.add(valLabel);

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(0, 4));
        
        card.add(bar, BorderLayout.NORTH);
        card.add(p, BorderLayout.CENTER);
        return card;
    }

    private JButton createNavBtn(String text, String icon) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setFont(new Font("Inter", Font.PLAIN, 15));
        btn.setForeground(new Color(148, 163, 184));
        btn.setBackground(SIDEBAR_COLOR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
           
            if(text.equals("Loan Approvals")) {
                loadLoanApprovalPanel(); 
            } else if(text.equals("Overview")) {
                showOverview();
            } else if(text.equals("Withdraw Requests")) {
                openWithdrawRequests();
            }
        });

        return btn;
    }
   
    private void loadLoanApprovalPanel() {
        displayArea.removeAll(); 
        displayArea.setLayout(new BorderLayout());

       
        ManagerLoanDashboard loanPanel = new ManagerLoanDashboard();
        displayArea.add(loanPanel, BorderLayout.CENTER);

        displayArea.revalidate();
        displayArea.repaint();
    }

    
    private void startLiveSync() {
        Timer timer = new Timer(5000, e -> {
            updateKpiData();
        });
        timer.start();
    }
        
    private void updateKpiData() {
        try (Connection con = ConnectionProvider.getCon()) {
           
            ResultSet rs1 = con.createStatement().executeQuery("SELECT SUM(balance) FROM accounts");
            if (rs1.next()) vaultLabel.setText("NPR " + String.format("%,.0f", rs1.getDouble(1)));

          
            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM dashboard_notifications WHERE status='PENDING' AND request_type='WITHDRAW_APPROVAL' ");
            int count = 0;
            if (rs2.next()) {
                count = rs2.getInt(1);
                pendingLabel.setText(String.valueOf(count));

                if (withdrawNavBtn != null) {
                    if (count > 0) {
                        withdrawNavBtn.setText("🔔 Withdraw Requests (" + count + ")");
                        withdrawNavBtn.setForeground(new Color(239, 68, 68)); 
                    } else {
                        withdrawNavBtn.setText("🔔 Withdraw Requests");
                        withdrawNavBtn.setForeground(new Color(148, 163, 184));
                    }
                }
            }
            
            
            ResultSet rsTotal = con.createStatement().executeQuery("SELECT COUNT(*) FROM dashboard_notifications WHERE status='PENDING'");
            if (rsTotal.next()) {
                pendingLabel.setText(String.valueOf(rsTotal.getInt(1)));
            }
            
            String sqlLoan = "SELECT COALESCE(SUM(outstanding_balance), 0) FROM loans";
            ResultSet rs3 = con.createStatement().executeQuery(sqlLoan);
            if (rs3.next()) {
                double currentAssets = rs3.getDouble(1);

              
                if (currentAssets == 0) {
                    ResultSet rsFallback = con.createStatement().executeQuery("SELECT SUM(loan_amount) FROM loans");
                    if (rsFallback.next()) currentAssets = rsFallback.getDouble(1);
                }

                loanLabel.setText("NPR " + String.format("%,.2f", currentAssets));
            }

        } catch (Exception e) {
            System.out.println("Main Sync Error: " + e.getMessage());
        }
    }
    
    private void openLoanCenter() {
        displayArea.removeAll();
        displayArea.add(new JLabel("<html><h2>Loan Approval Center</h2><p>System automatically calculates Credit Scores...</p></html>"), BorderLayout.NORTH);
        
        displayArea.revalidate();
        displayArea.repaint();
    }
        
    private void startTransactionHeartbeat(DefaultTableModel model) {
        Timer heartbeat = new Timer(5000, e -> {
            try (Connection con = ConnectionProvider.getCon()) {
                
                String query = "SELECT transaction_time, account_number, transaction_type, amount, executed_by_teller_id FROM transactions ORDER BY transaction_time DESC LIMIT 10";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(query);

                model.setRowCount(0);

                while (rs.next()) {
                    String time = rs.getString("transaction_time");
                    String acc = rs.getString("account_number");
                    String type = rs.getString("transaction_type");
                    double amt = rs.getDouble("amount");

                    String teller = rs.getString("executed_by_teller_id");

                    
                    model.addRow(new Object[]{
                        time, 
                        acc, 
                        type, 
                        "NPR " + String.format("%,.2f", amt),
                        teller 
                    });
                }
            } catch (Exception ex) {
                System.out.println("Sync Error: " + ex.getMessage());
            }
        });
        heartbeat.start();
    }
    
    private void openWithdrawRequests() {
        displayArea.removeAll();

        JLabel header = new JLabel("Pending Withdrawal Approvals");
        header.setFont(new Font("Inter", Font.BOLD, 20));
        displayArea.add(header, BorderLayout.NORTH);

      
        String[] columns = {"ID", "Teller ID", "Account No", "Amount", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(40);

       
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT id, sender_id, account_number, amount, status FROM dashboard_notifications WHERE status='PENDING' AND request_type='WITHDRAW_APPROVAL'";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("sender_id"), 
                    rs.getString("account_number"), "NPR " + rs.getString("amount"), 
                    rs.getString("status"),"APPROVE"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
       
        JButton approveBtn = new JButton("Approve Selected Request");
        approveBtn.setBackground(ACCENT_COLOR);
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Object idObj = table.getValueAt(row, 0);
                int id = Integer.parseInt(idObj.toString());
                approveRequest(id); 
                openWithdrawRequests(); 
            } else {
                JOptionPane.showMessageDialog(this, "Please select a request from the table first!");
            }
        });


        displayArea.add(new JScrollPane(table), BorderLayout.CENTER);
        displayArea.add(approveBtn, BorderLayout.SOUTH);

        displayArea.revalidate();
        displayArea.repaint();
    }
          
    private void approveRequest(int requestId) {
 
        String sql = "UPDATE dashboard_notifications SET status = 'APPROVED' WHERE id = ?";

        try (Connection con = ConnectionProvider.getCon(); 
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, requestId);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "✅ Request ID #" + requestId + " has been Approved!");

                openWithdrawRequests(); 

              
                updateKpiData(); 
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void openLoanHistory() {
        displayArea.removeAll();

        String[] columns = {"App ID", "Account", "Amount", "Status", "Reason for Rejection"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0);
        JTable historyTable = new JTable(historyModel);

        try (Connection con = ConnectionProvider.getCon()) {
          
            String sql = "SELECT application_no, account_no, amount, loan_status, rejection_reason FROM loans WHERE loan_status != 'PENDING'";
            ResultSet rs = con.createStatement().executeQuery(sql);

            while(rs.next()) {
                historyModel.addRow(new Object[]{
                    rs.getString("application_no"),
                    rs.getString("account_no"),
                    "NPR " + rs.getString("amount"),
                    rs.getString("loan_status"),
                    rs.getString("rejection_reason") 
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        displayArea.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        displayArea.revalidate();
        displayArea.repaint();
    }
    
    
    private void openTransactionAudit() {
       
        String[] columns = {"ID", "Account", "Type", "Amount", "Date"};
        DefaultTableModel auditModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(auditModel);

      
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        table.setFont(new Font("Inter", Font.PLAIN, 12));

        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
                return comp;
            }
        });

        try (Connection con = ConnectionProvider.getCon()) {
            
            String sql = "SELECT transaction_id, account_number, transaction_type, amount, transaction_time " +
                         "FROM transactions ORDER BY transaction_time DESC";

            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                auditModel.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("account_number"),
                    rs.getString("transaction_type"),
                    "NPR " + String.format("%,.2f", rs.getDouble("amount")),
                    rs.getTimestamp("transaction_time")
                });
            }

            
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(700, 400));
            JOptionPane.showMessageDialog(this, scroll, "Transaction Audit Log", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Audit Error: " + e.getMessage());
        }
    }
    
    private void openMasterAudit() {
        
        displayArea.removeAll();
        displayArea.setLayout(new BorderLayout(0, 20));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Enter Account Number...");
        JButton btnSearch = new JButton("🔍 Search");
        JButton btnReset = new JButton("🔄 Reset");

        searchPanel.add(new JLabel(" Account No: "));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);
        
        String[] columns = {"ID", "Account No", "Type", "Amount", "Date", "Teller"};
              
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
           @Override
           public boolean isCellEditable(int row, int column) {
               return false; 
           }
       };
        
        JTable table = new JTable(model);
        styleAuditTable(table);

       
        btnSearch.addActionListener(e -> refreshAuditData(model, table, searchField.getText()));
        btnReset.addActionListener(e -> {
            searchField.setText("");
            refreshAuditData(model, table, "");
        });
        
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        displayArea.add(topContainer, BorderLayout.NORTH);
        displayArea.add(new JScrollPane(table), BorderLayout.CENTER);
       
        refreshAuditData(model, table, "");

        displayArea.revalidate();
        displayArea.repaint();
    }
    
    private void refreshAuditData(DefaultTableModel model, JTable table, String filterAcc) {
   
        if (table != null && table.isEditing()) {
           table.getCellEditor().stopCellEditing();
        }
      
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);

            try (Connection con = ConnectionProvider.getCon()) {
                String sql = "SELECT * FROM transactions";
                if (!filterAcc.isEmpty()) {
                    sql += " WHERE account_number LIKE ?";
                }
                sql += " ORDER BY transaction_time DESC";

                PreparedStatement pst = con.prepareStatement(sql);
                if (!filterAcc.isEmpty()) {
                    pst.setString(1, "%" + filterAcc + "%");
                }

                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("transaction_id"),
                        rs.getString("account_number"),
                        rs.getString("transaction_type"),
                        "NPR " + String.format("%,.2f", rs.getDouble("amount")),
                        rs.getTimestamp("transaction_time"),
                        rs.getString("executed_by_teller_id")
                    });
                }
            } catch (Exception e) {
                System.err.println("Audit Refresh Error: " + e.getMessage());
            }
        });
    }
    
    private void styleAuditTable(JTable table) {
        table.setRowHeight(30); 
        table.setIntercellSpacing(new Dimension(10, 10));
        table.setGridColor(new Color(241, 245, 249));
        table.setFont(new Font("Inter", Font.PLAIN, 12));

        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }
}

