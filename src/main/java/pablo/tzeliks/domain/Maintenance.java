package pablo.tzeliks.domain;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Maintenance {

    private int id;
    private int vehicleId;
    private String description;
    private BigDecimal cost;
    private LocalDate date;

    public Maintenance(int id, int vehicleId, String description, BigDecimal cost, LocalDate date) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.description = description;
        this.cost = cost;
        this.date = date;
    }

    public Maintenance() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Additional
    public static Maintenance mapRow(ResultSet rs) throws SQLException {
        return new Maintenance(
                rs.getInt("id"),
                rs.getInt("vehicle_id"),
                rs.getString("description"),
                rs.getBigDecimal("cost"),
                rs.getDate("date").toLocalDate()
        );
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                ", date=" + date +
                '}';
    }
}
