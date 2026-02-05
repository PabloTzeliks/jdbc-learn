package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.domain.VehicleStatus;
import pablo.tzeliks.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleRepositoryImpl implements VehicleRepository {

    @Override
    public Vehicle save(Vehicle vehicle) throws SQLException {

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
        }

        return vehicle;
    }

    @Override
    public boolean isValidPlate(String licensePlate) throws SQLException {

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
        }

        return false;
    }

    @Override
    public Vehicle findById(int id)  throws SQLException {

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
        }

        return null;
    }

    @Override
    public List<Vehicle> findAll() throws SQLException {

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
                            new Vehicle(
                                rs.getInt(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getDate(4).toLocalDate(),
                                VehicleStatus.valueOf(rs.getString(5))
                    ));
                }
            }
        }

        return vehicles;
    }

    @Override
    public Vehicle update(Vehicle newVehicle) throws SQLException {

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
        }

        return null;
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = """
                DELETE FROM vehicle
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);

            int num = ps.executeUpdate();
        }
    }
}
