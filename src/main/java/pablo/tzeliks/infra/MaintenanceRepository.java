package pablo.tzeliks.infra;

import pablo.tzeliks.domain.Maintenance;

public interface MaintenanceRepository {

    Maintenance save(int idVehicle, Maintenance maintenance);
}
