package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Vehicle;

import java.sql.SQLException;
import java.util.List;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle) throws SQLException;

    Vehicle findById(int id);

    List<Vehicle> findAll();

    Vehicle update(Vehicle newVehicle);

    Vehicle delete(int id);
}