package bankmanagementsystem.dao;

import bankmanagementsystem.ConnectionProvider;
import bankmanagementsystem.entities.Teller;
import java.sql.*;

public class TellerDAO {

    private static final String AUTH_QUERY = 
        "SELECT teller_id, full_name, hashed_pin, status, date_hired FROM Tellers WHERE teller_id = ?";

    /**
     * Retrieves essential teller details for authentication.
     * @param tellerId The ID to look up.
     * @return A Teller object containing the ID, hashed PIN, and status, or null if not found.
     */
    public Teller getTellerForAuth(String tellerId) {
        // NOTE: Ensure your Tellers table has these column names: 
        // teller_id, full_name, hashed_pin, status
        String query = "SELECT teller_id, full_name, hashed_pin, status, date_hired FROM Tellers WHERE teller_id = ?";
        Teller teller = null;

        // This uses its own connection and closes it, as it's an isolated read.
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, tellerId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    
                    // 1. Retrieve the java.sql.Date object first.
                        java.sql.Date dateHired = rs.getDate(5);
                        String dateHiredString = null;

                        // 2. Convert the Date object to a String, handling the case where it might be null
                        if (dateHired != null) {
                            // This calls the default toString() format, which is usually "yyyy-MM-dd"
                            dateHiredString = dateHired.toString(); 
                        }
                    // Create the Teller object using the data retrieved
                    teller = new Teller(
                    // Index 1: teller_id
                    rs.getString(1),
                    // Index 2: full_name
                    rs.getString(2),
                    // Index 3: hashed_pin
                    rs.getString(3),
                    // Index 4: status (The fix for the NULL error)
                    rs.getString(4),
                    // Index 5: date_hired (Retrieving as a String or java.sql.Date)
                    dateHiredString // Use rs.getDate() for the date field
                  );
                }
            }

        } catch (SQLException e) {
            System.err.println("DB Error fetching teller for auth: " + e.getMessage());
            e.printStackTrace();
            // Return null on error so TransactionService can fail gracefully
        }
        return teller;
    }
}

