package pablo.tzeliks.service;

import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.infra.VehicleRepository;

import java.sql.SQLException;

public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle save(Vehicle vehicle) {

        Vehicle dbVehicle;

        if (!validatePlate(vehicle.getLicensePlate())) {

            throw new RuntimeException("Veículo já cadastrado com esta placa!");
        }

        try {

            dbVehicle = repository.save(vehicle);
        } catch (SQLException ex) {

            throw new RuntimeException(ex.getMessage());
        }

        return dbVehicle;
    }

    public Vehicle findById(int id) {

        Vehicle dbVehicle;

        try {
            dbVehicle = repository.findById(id);
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        if (dbVehicle == null) {

            throw new RuntimeException("ID inválido!");
        }

        return dbVehicle;
    }

    public Vehicle update(Vehicle newVehicle) {


    }

    private boolean validatePlate(String licensePlate) {

        try {

            return repository.isValidPlate(licensePlate);
        } catch (SQLException ex) {

            throw new RuntimeException(ex.getMessage());
        }
    }
}
