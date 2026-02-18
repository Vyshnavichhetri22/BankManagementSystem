package bankmanagementsystem;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class DailyLogsForm extends JFrame {
    private String tellerId;
    private JTable logTable;
    private DefaultTableModel model;
    
    // Search Components
    private JTextField startDateField, endDateField;
    private JButton btnSearch, btnReset;

    public DailyLogsForm(String employeeId) {
        this.tellerId = employeeId;
        setTitle("Operational Logs - " + tellerId);
        setSize(1100, 650); 
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        header.setPreferredSize(new Dimension(0, 60));
        
        JLabel title = new JLabel("  TRANSACTION HISTORY");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        // --- NEW: Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        searchPanel.setOpaque(false);

        JLabel lblFrom = new JLabel("From (YYYY-MM-DD):");
        lblFrom.setForeground(Color.WHITE);
        startDateField = new JTextField(10);

        JLabel lblTo = new JLabel("To:");
        lblTo.setForeground(Color.WHITE);
        endDateField = new JTextField(10);

        btnSearch = new JButton("🔍 Search");
        btnReset = new JButton("🔄 Reset");

        searchPanel.add(lblFrom);
        searchPanel.add(startDateField);
        searchPanel.add(lblTo);
        searchPanel.add(endDateField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);

        header.add(searchPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table Setup
        model = new DefaultTableModel(new String[]{"TIMESTAMP", "TELLER", "ACTION", "DETAILS"}, 0);
        logTable = new JTable(model);
        logTable.setRowHeight(30);
        add(new JScrollPane(logTable), BorderLayout.CENTER);

        // Styling
        styleTable();

        // --- Action Listeners ---
        btnSearch.addActionListener(e -> fetchLogsByDate());
        btnReset.addActionListener(e -> {
            startDateField.setText("");
            endDateField.setText("");
            fetchLogs(); // Show all logs
        });

        fetchLogs(); // Initial load
    }

    private void styleTable() {
        logTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(500);
        logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        logTable.setShowGrid(true);
        logTable.setGridColor(new Color(230, 230, 230));
    }

    // Original fetch (Shows all)
    private void fetchLogs() {
        model.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "SELECT log_timestamp, employee_id, action_type, details FROM daily_logs ORDER BY log_timestamp DESC";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            populateModel(rs);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- NEW: Search Logic ---    
    private void fetchLogsByDate() {
        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();

        if (start.isEmpty()) {
            JOptionPane.showMessageDialog(this, "At least provide a 'From' date!");
            return;
        }

        // If 'To' is empty, automatically set it to Today's date
        if (end.isEmpty()) {
            end = java.time.LocalDate.now().toString(); 
            endDateField.setText(end); // Show the user what happened
        }
    
        model.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            String sql;
            PreparedStatement pst;

            if (end.isEmpty()) {
                // Case: Only From date is provided -> Show everything from that day to now
                sql = "SELECT log_timestamp, employee_id, action_type, details FROM daily_logs " +
                      "WHERE DATE(log_timestamp) >= ? ORDER BY log_timestamp DESC";
                pst = con.prepareStatement(sql);
                pst.setString(1, start);
            } else {
                // Case: Both dates provided
                sql = "SELECT log_timestamp, employee_id, action_type, details FROM daily_logs " +
                      "WHERE DATE(log_timestamp) BETWEEN ? AND ? ORDER BY log_timestamp DESC";
                pst = con.prepareStatement(sql);
                pst.setString(1, start);
                pst.setString(2, end);
            }

            ResultSet rs = pst.executeQuery();
            populateModel(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateModel(ResultSet rs) throws SQLException {
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getTimestamp("log_timestamp"),
                rs.getString("employee_id"),
                rs.getString("action_type"),
                rs.getString("details")
            });
        }
    }
}