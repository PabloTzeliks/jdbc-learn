package pablo.tzeliks.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Vehicle {

    private int id;
    private String licensePlate;
    private String model;
    private LocalDate manufacturingDate;
    private VehicleStatus status;

    public Vehicle(int id, String licensePlate, String model, LocalDate manufacturingDate, VehicleStatus status) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.model = model;
        this.manufacturingDate = manufacturingDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    // Additional
    public static Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getDate(4).toLocalDate(),
                VehicleStatus.valueOf(rs.getString(5))
        );
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", model='" + model + '\'' +
                ", manufacturingDate=" + manufacturingDate +
                ", status=" + status +
                '}';
    }
}
