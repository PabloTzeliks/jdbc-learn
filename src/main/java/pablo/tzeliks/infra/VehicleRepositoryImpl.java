package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Maintenance;
import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.domain.VehicleStatus;
import pablo.tzeliks.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleRepositoryImpl implements VehicleRepository {

    @Override
    public Vehicle save(Vehicle vehicle) {

        String query = """
        INSERT INTO vehicle
        (license_plate, model, manufacturing_date, status)
        VALUES (?, ?, ?, ?);
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vehicle.getLicensePlate());
            ps.setString(2, vehicle.getModel());
            ps.setDate(3, Date.valueOf(vehicle.getManufacturingDate()));
            ps.setString(4, String.valueOf(vehicle.getStatus()));

            ps.execute();

            try (var rs = ps.getGeneratedKeys()) {

                if (rs.next()) {

                    vehicle.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }

        return vehicle;
    }

    @Override
    public boolean isValidPlate(String licensePlate) {

        String query = """
                SELECT COUNT(*) 
                FROM vehicle 
                WHERE license_plate = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, licensePlate);

            try (var rs = ps.executeQuery()) {

                if (rs.next()) {

                    int count = rs.getInt(1);
                    return count == 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Vehicle findById(int id) {

        String query = """
                SELECT * 
                FROM vehicle 
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            try (var rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new Vehicle(
                            id,
                            rs.getString(2),
                            rs.getString(3),
                            rs.getDate(4).toLocalDate(),
                            VehicleStatus.valueOf(rs.getString(5))
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Vehicle> findAll() {

        List<Vehicle> vehicles = new ArrayList<>();

        String query = """
                SELECT * 
                FROM vehicle;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            try (var rs = ps.executeQuery()) {

                while (rs.next()) {

                    vehicles.add(
                            Vehicle.mapRow(rs)
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }

        return vehicles;
    }

    @Override
    public Vehicle update(Vehicle newVehicle) {

        String query = """
                UPDATE vehicle
                SET license_plate = ?, model = ?, manufacturing_date = ?, status = ?
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, newVehicle.getLicensePlate());
            ps.setString(2, newVehicle.getModel());
            ps.setDate(3, Date.valueOf(newVehicle.getManufacturingDate()));
            ps.setString(4, String.valueOf(newVehicle.getStatus()));

            ps.setInt(5, newVehicle.getId());

            int num = ps.executeUpdate();

            if (num > 0) {

                return newVehicle;
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void delete(int id) {
        String query = """
                DELETE FROM vehicle
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }
    }

    @Override
    public boolean updateStatus(int idVehicle, VehicleStatus newStatus) {

        String query = """
                UPDATE vehicle
                SET status = ?
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, newStatus.name());
            ps.setInt(2, idVehicle);

            int correct = ps.executeUpdate();

            return correct > 0;

        } catch (SQLException e) {
            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }
    }

    @Override
    public Vehicle findAllMaintenances(int idVehicle) {

        String query = """
                SELECT v.*,
                m.id as m_id, m.vehicle_id, m.description, m.cost, m.date
                FROM vehicle v
                LEFT JOIN maintenance m ON v.id = m.vehicle_id
                WHERE v.id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idVehicle);

            try (var rs = ps.executeQuery()) {
                Vehicle dbVehicle = null;

                while (rs.next()) {

                    if (dbVehicle == null) {

                        dbVehicle = Vehicle.mapRow(rs);
                    }

                    int idMaintenance = rs.getInt("m_id");

                    if (idMaintenance > 0) {

                        Maintenance dbMaintenance = Maintenance.mapRow(rs);

                        dbVehicle.getMaintenances().add(dbMaintenance);
                    }
                }

                return dbVehicle;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            throw new RuntimeException("An error Ocurred: " + e.getMessage());
        }
    }
}
