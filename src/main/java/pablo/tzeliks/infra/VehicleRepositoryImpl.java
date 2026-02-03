package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.utils.DatabaseConnection;

import java.sql.*;
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
    public Vehicle findById(int id) {
        return null;
    }

    @Override
    public List<Vehicle> findAll() {
        return List.of();
    }

    @Override
    public Vehicle update(Vehicle newVehicle) {
        return null;
    }

    @Override
    public Vehicle delete(int id) {
        return null;
    }
}
