package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Vehicle;

import java.sql.SQLException;
import java.util.List;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle) throws SQLException;

    Vehicle findById(int id) throws SQLException;

    List<Vehicle> findAll() throws SQLException;

    Vehicle update(Vehicle newVehicle) throws SQLException;

    void delete(int id) throws SQLException;

    // Additional

    boolean isValidPlate(String licensePlate) throws SQLException;
}