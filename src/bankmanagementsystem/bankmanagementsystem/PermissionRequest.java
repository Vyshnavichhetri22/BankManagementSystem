package bankmanagementsystem;

import javax.swing.*;
import java.awt.*;

public class PermissionRequest extends JFrame {
    public PermissionRequest() {
        setTitle("Request Manager Permission");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Permission Request Form", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JTextArea requestArea = new JTextArea(10, 30);
        requestArea.setBorder(BorderFactory.createTitledBorder("Describe your required access (e.g., Loan Approval for Customer XYZ):"));
        add(new JScrollPane(requestArea), BorderLayout.CENTER);

        JButton submitBtn = new JButton("Send Request to Manager");
        submitBtn.addActionListener(e -> {
            if (requestArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please describe the permission needed.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // In a full system, this would insert a request into a 'Permissions_Queue' table
                JOptionPane.showMessageDialog(this, "Request Sent Successfully! A manager will review it.", "Sent", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });
        add(submitBtn, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}