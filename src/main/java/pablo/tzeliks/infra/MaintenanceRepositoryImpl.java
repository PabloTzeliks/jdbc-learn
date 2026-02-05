package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Maintenance;
import pablo.tzeliks.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;

public class MaintenanceRepositoryImpl implements MaintenanceRepository {

    @Override
    public Maintenance save(int idVehicle, Maintenance maintenance) {

        String query = """
                INSERT INTO maintenance (vehicle_id, description, cost, date)
                VALUES (?, ?, ?, ?);
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idVehicle);
            ps.setString(2, maintenance.getDescription());
            ps.setBigDecimal(3, maintenance.getCost());
            ps.setDate(4, Date.valueOf(maintenance.getDate()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    maintenance.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("An error Occured: " + e.getMessage());
        }

        return maintenance;
    }

    @Override
    public BigDecimal calculateTotalCostsFromOneVehicle(int idVehicle) {

        String query = """
                SELECT COALESCE(SUM(cost), 0) as total
                FROM maintenance
                WHERE vehicle_id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idVehicle);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {

                    return rs.getBigDecimal("total");
                }

                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }
    }
}
