package bankmanagementsystem;
import java.sql.*;

public class AuditService {
    public static void log(String tellerId, String action, String details) {
        try (Connection con = ConnectionProvider.getCon()) {
            String sql = "INSERT INTO daily_logs (employee_id, action_type, details) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, tellerId);
            pst.setString(2, action);
            pst.setString(3, details);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
