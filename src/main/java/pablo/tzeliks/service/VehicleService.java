package pablo.tzeliks.service;

import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.infra.VehicleRepository;

import java.util.List;

public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle save(Vehicle vehicle) {

        if (!validatePlate(vehicle.getLicensePlate())) {

            throw new RuntimeException("Veículo já cadastrado com esta placa!");
        }

        return repository.save(vehicle);
    }

    public Vehicle findById(int id) {

        var dbVehicle = repository.findById(id);

        if (dbVehicle == null) {

            throw new RuntimeException("ID inválido!");
        }

        return dbVehicle;
    }

    public Vehicle update(Vehicle newVehicle) {

        var dbNewVehicle = repository.update(newVehicle);

        if (dbNewVehicle == null) {

            throw new RuntimeException("Update Falhou!");
        }

        return dbNewVehicle;
    }

    public List<Vehicle> findAll() {

        return repository.findAll();
    }

    public void delete(int id) {

        repository.delete(id);
    }

    private boolean validatePlate(String licensePlate) {

        return repository.isValidPlate(licensePlate);
    }
}
