package pablo.tzeliks;

import org.junit.jupiter.api.*;
import pablo.tzeliks.domain.Maintenance;
import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.domain.VehicleStatus;
import pablo.tzeliks.infra.VehicleRepository;
import pablo.tzeliks.infra.VehicleRepositoryImpl;
import pablo.tzeliks.service.VehicleService;
import pablo.tzeliks.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste de Integração - Vehicle (JDBC Puro)")
public class VehicleRepositoryIntegrationTest {

    VehicleService service;

    // --- Scripts DDL (Data Definition Language) ---
    private static final String CREATE_VEHICLE = """
            CREATE TABLE IF NOT EXISTS vehicle (
                id INT PRIMARY KEY AUTO_INCREMENT,
                license_plate VARCHAR(20) UNIQUE NOT NULL,
                model VARCHAR(100) NOT NULL,
                manufacturing_date DATE NOT NULL,
                status VARCHAR(20) NOT NULL
            );
            """;

    private static final String CREATE_MAINTENANCE = """
            CREATE TABLE IF NOT EXISTS maintenance (
                id INT PRIMARY KEY AUTO_INCREMENT,
                vehicle_id INT NOT NULL,
                description VARCHAR(255) NOT NULL,
                cost DECIMAL(10, 2) NOT NULL,
                date DATE NOT NULL,
                FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE
            );
            """;

    @BeforeAll
    static void setupGlobal() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Ordem importa: Dropa primeiro a filha (maintenance), depois a mãe (vehicle)
            stmt.execute("DROP TABLE IF EXISTS maintenance");
            stmt.execute("DROP TABLE IF EXISTS vehicle");

            stmt.execute(CREATE_VEHICLE);
            stmt.execute(CREATE_MAINTENANCE);

        } catch (SQLException e) {
            fail("Erro no setup global: " + e.getMessage());
        }
    }

    @BeforeEach
    void setup() {
        // Injeção de Dependência manual
        service = new VehicleService(new VehicleRepositoryImpl());

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE maintenance");
            stmt.execute("TRUNCATE TABLE vehicle");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    // Testes Simples
    @Test
    @DisplayName("Deve salvar um veículo com sucesso e validar no banco")
    void deveSalvarVeiculo() throws SQLException {

        // Cenário
        Vehicle novoVeiculo = new Vehicle(0, "ABC-1234", "Volvo FH", LocalDate.of(2022, 5, 20), VehicleStatus.AVAILABLE);

        // Ação (Descomente quando criar o DAO)
        Vehicle salvo = service.save(novoVeiculo);

        // Validação (Simulando que 'salvo' retornou com ID)
        assertNotNull(salvo.getId());
        assertTrue(salvo.getId() > 0);

        // Validação "Prova Real" (Indo direto no banco ver se gravou)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vehicle WHERE license_plate = ?")) {

            stmt.setString(1, "ABC-1234");
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "Deveria ter encontrado o veículo no banco");
            assertEquals("Volvo FH", rs.getString("model"));
            // O Banco retorna java.sql.Date, convertemos para LocalDate para comparar
            assertEquals(LocalDate.of(2022, 5, 20), rs.getDate("manufacturing_date").toLocalDate());
            assertEquals("AVAILABLE", rs.getString("status"));
        }
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar salvar placa duplicada")
    void deveFalharPlacaDuplicada() throws SQLException {
        // Cenário: Já existe um carro no banco
        inserirVeiculoSQL("XYZ-9999", "Scania", LocalDate.now(), VehicleStatus.IN_TRANSIT);

        Vehicle veiculoDuplicado = new Vehicle(0, "XYZ-9999", "Outro Modelo", LocalDate.now(), VehicleStatus.AVAILABLE);

        // Ação e Validação
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.save(veiculoDuplicado);
        });

        assertEquals("Veículo já cadastrado com esta placa!", ex.getMessage());
    }

    @Test
    @DisplayName("Deve buscar veículo por ID")
    void deveBuscarPorId() throws SQLException {
        // Cenário
        int idGerado = inserirVeiculoSQL("BUS-1010", "Mercedes Benz", LocalDate.of(2020, 1, 1), VehicleStatus.AVAILABLE);

        // Ação
        Vehicle encontrado = service.findById(idGerado);

        // Validação
        assertNotNull(encontrado);
        assertEquals("BUS-1010", encontrado.getLicensePlate());
        assertEquals(VehicleStatus.AVAILABLE, encontrado.getStatus());
    }

    @Test
    @DisplayName("Deve retornar erro ao buscar ID inexistente")
    void deveFalharBuscaIdInexistente() {
        // Ação e Validação
        assertThrows(RuntimeException.class, () -> {
            service.findById(9999);
        });
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void deveListarTodos() throws SQLException {
        // Cenário
        inserirVeiculoSQL("AAA-1111", "Truck 1", LocalDate.now(), VehicleStatus.AVAILABLE);
        inserirVeiculoSQL("BBB-2222", "Truck 2", LocalDate.now(), VehicleStatus.IN_MAINTANENCE);

        // Ação
        List<Vehicle> lista = service.findAll();

        // Validação
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Deve atualizar status e modelo do veículo")
    void deveAtualizarVeiculo() throws SQLException {
        // Cenário
        int id = inserirVeiculoSQL("UPT-2020", "Modelo Antigo", LocalDate.now(), VehicleStatus.AVAILABLE);

        // Objeto com novos dados
        Vehicle paraAtualizar = new Vehicle(id, "UPT-2020", "Modelo Novo", LocalDate.now(), VehicleStatus.IN_MAINTANENCE);

        // Ação
        service.update(paraAtualizar);

        // Validação via SQL
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT model, status FROM vehicle WHERE id = " + id)) {

            assertTrue(rs.next());
            assertEquals("Modelo Novo", rs.getString("model"));
            assertEquals("IN_MAINTANENCE", rs.getString("status"));
        }
    }

    @Test
    @DisplayName("Deve deletar veículo por ID")
    void deveDeletarVeiculo() throws SQLException {
        // Cenário
        int id = inserirVeiculoSQL("DEL-0000", "Para Deletar", LocalDate.now(), VehicleStatus.AVAILABLE);

        // Ação
        service.delete(id);

        // Validação
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT count(*) FROM vehicle WHERE id = " + id)) {

            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }

    // Testes Complexos
//    @Test
//    @DisplayName("Regra de Negócio: Adicionar manutenção deve mudar status do veículo")
//    void deveRegistrarManutencaoEAtualizarStatus() throws SQLException {
//        // 1. Cenário: Tenho um carro DISPONÍVEL
//        int idVeiculo = inserirVeiculoSQL("MAN-1234", "Ford Cargo", LocalDate.now(), VehicleStatus.AVAILABLE);
//
//        Maintenance manutencao = new Maintenance();
//        manutencao.setDescription("Troca de Óleo e Filtros");
//        manutencao.setCost(new BigDecimal("1500.50"));
//        manutencao.setDate(LocalDate.now());
//
//        // 2. Ação: Chamo o serviço passando o ID do carro e o objeto manutenção
//        service.addMaintenance(idVeiculo, manutencao);
//
//        // 3. Validação A: A manutenção foi salva?
//        try (Connection conn = DatabaseConnection.getConnection();
//             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM maintenance WHERE vehicle_id = " + idVeiculo)) {
//            assertTrue(rs.next(), "Deveria ter registro na tabela maintenance");
//            assertEquals("Troca de Óleo e Filtros", rs.getString("description"));
//        }
//
//        // 4. Validação B (Regra de Negócio): O status do carro mudou automaticamente?
//        Vehicle veiculoAtualizado = service.findById(idVeiculo);
//        assertEquals(VehicleStatus.IN_MAINTANENCE, veiculoAtualizado.getStatus(),
//                "O sistema deveria ter mudado o status do carro para IN_MAINTANENCE automaticamente");
//    }
//
//    @Test
//    @DisplayName("Bad Path: Não pode adicionar manutenção em veículo inexistente")
//    void deveFalharManutencaoEmVeiculoInexistente() {
//        Maintenance m = new Maintenance();
//        m.setDescription("Reparo Fantasma");
//        m.setCost(BigDecimal.TEN);
//        m.setDate(LocalDate.now());
//
//        // Ação: Tentar adicionar no ID 9999
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
//            service.addMaintenance(9999, m);
//        });
//
//        // Validação da mensagem de erro amigável
//        assertEquals("Veículo não encontrado para adicionar manutenção!", ex.getMessage());
//    }
//
//    @Test
//    @DisplayName("Complex Query: Deve buscar Veículo trazendo sua lista de manutenções (JOIN)")
//    void deveBuscarVeiculoComHistoricoCompleto() throws SQLException {
//        // 1. Cenário: Carro com 2 manutenções
//        int idVeiculo = inserirVeiculoSQL("HIS-8888", "Scania V8", LocalDate.of(2020, 1, 1), VehicleStatus.IN_MAINTANENCE);
//        inserirManutencaoSQL(idVeiculo, "Motor", 5000.00);
//        inserirManutencaoSQL(idVeiculo, "Pneus", 2000.00);
//
//        // 2. Ação: Método especial que faz o JOIN
//        Vehicle veiculoCompleto = service.findWithMaintenances(idVeiculo);
//
//        // 3. Validações
//        assertNotNull(veiculoCompleto);
//        assertEquals("Scania V8", veiculoCompleto.getModel());
//
//        // A lista dentro do objeto Vehicle deve estar preenchida!
//        assertEquals(2, veiculoCompleto.getMaintenances().size());
//        assertEquals("Motor", veiculoCompleto.getMaintenances().get(0).getDescription());
//    }
//
//    @Test
//    @DisplayName("Relatório: Deve calcular custo total de manutenção de um veículo")
//    void deveCalcularCustoTotal() throws SQLException {
//        int idVeiculo = inserirVeiculoSQL("CASH-100", "Iveco", LocalDate.now(), VehicleStatus.AVAILABLE);
//
//        inserirManutencaoSQL(idVeiculo, "Peça A", 100.50);
//        inserirManutencaoSQL(idVeiculo, "Peça B", 200.50);
//        inserirManutencaoSQL(idVeiculo, "Mão de Obra", 100.00);
//
//        // Total esperado: 401.00
//        BigDecimal total = service.calculateTotalMaintenanceCost(idVeiculo);
//
//        assertEquals(new BigDecimal("401.00"), total);
//    }

    // --- Helpers SQL ---

    private int inserirVeiculoSQL(String plate, String model, LocalDate date, VehicleStatus status) throws SQLException {
        String sql = "INSERT INTO vehicle (license_plate, model, manufacturing_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, plate);
            stmt.setString(2, model);
            stmt.setDate(3, java.sql.Date.valueOf(date));
            stmt.setString(4, status.name());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    private void inserirManutencaoSQL(int vehicleId, String desc, double cost) throws SQLException {
        String sql = "INSERT INTO maintenance (vehicle_id, description, cost, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehicleId);
            stmt.setString(2, desc);
            stmt.setBigDecimal(3, BigDecimal.valueOf(cost));
            stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        }
    }
}