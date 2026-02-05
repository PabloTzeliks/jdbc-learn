package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Maintenance;

import java.math.BigDecimal;

public interface MaintenanceRepository {

    Maintenance save(int idVehicle, Maintenance maintenance);

    BigDecimal calculateTotalCostsFromOneVehicle(int idVehicle);
}
