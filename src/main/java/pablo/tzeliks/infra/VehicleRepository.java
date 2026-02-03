package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Vehicle;

import java.util.List;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Vehicle findById(int id);

    List<Vehicle> findAll();

    Vehicle update(Vehicle newVehicle);

    Vehicle delete(int id);

}
