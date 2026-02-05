package pablo.tzeliks.service;

import pablo.tzeliks.domain.Maintenance;
import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.domain.VehicleStatus;
import pablo.tzeliks.infra.MaintenanceRepository;
import pablo.tzeliks.infra.VehicleRepository;

import java.math.BigDecimal;

public class MaintenanceService {

    private MaintenanceRepository maintenanceRepository;
    private VehicleRepository vehicleRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public Maintenance addMaintenance(int idVehicle, Maintenance maintenance) {

        if (vehicleRepository.findById(idVehicle) == null) {

            throw new RuntimeException("Veículo não encontrado para adicionar manutenção!");
        }

        var dbMaintenance = maintenanceRepository.save(idVehicle, maintenance);
        vehicleRepository.updateStatus(idVehicle, VehicleStatus.IN_MAINTANENCE);

        return dbMaintenance;
    }

    public BigDecimal calculateTotalMaintenanceCost(int idVehicle) {

        return maintenanceRepository.calculateTotalCostsFromOneVehicle(idVehicle);
    }
}
