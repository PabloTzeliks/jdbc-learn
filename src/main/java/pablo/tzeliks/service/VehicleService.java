package pablo.tzeliks.service;

import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.infra.VehicleRepository;

import java.util.List;

public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle save(Vehicle vehicle) {

        if (!validatePlate(vehicle.getLicensePlate())) {

            throw new RuntimeException("Veículo já cadastrado com esta placa!");
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle findById(int id) {

        var dbVehicle = vehicleRepository.findById(id);

        if (dbVehicle == null) {

            throw new RuntimeException("ID inválido!");
        }

        return dbVehicle;
    }

    public Vehicle update(Vehicle newVehicle) {

        var dbNewVehicle = vehicleRepository.update(newVehicle);

        if (dbNewVehicle == null) {

            throw new RuntimeException("Update Falhou!");
        }

        return dbNewVehicle;
    }

    public List<Vehicle> findAll() {

        return vehicleRepository.findAll();
    }

    public void delete(int id) {

        vehicleRepository.delete(id);
    }

    public Vehicle findWithMaintenances(int idVeiculo) {

        return vehicleRepository.findAllMaintenances(idVeiculo);
    }

    // Additional Methods

    private boolean validatePlate(String licensePlate) {

        return vehicleRepository.isValidPlate(licensePlate);
    }
}
