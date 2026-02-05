package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.domain.VehicleStatus;

import java.util.List;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Vehicle findById(int id);

    List<Vehicle> findAll();

    Vehicle update(Vehicle newVehicle);

    void delete(int id);

    // Additional

    boolean isValidPlate(String licensePlate);
    boolean updateStatus(int id, VehicleStatus newStatus);
}