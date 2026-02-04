package pablo.tzeliks;

import org.junit.jupiter.api.*;
import pablo.tzeliks.domain.Vehicle;
import pablo.tzeliks.domain.VehicleStatus;
import pablo.tzeliks.infra.VehicleRepository;
import pablo.tzeliks.infra.VehicleRepositoryImpl;
import pablo.tzeliks.service.VehicleService;
import pablo.tzeliks.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teste de Integração - Vehicle (JDBC Puro)")
public class VehicleRepositoryIntegrationTest {

    VehicleRepository repository;
    VehicleService service;

    // --- Scripts DDL (Data Definition Language) ---
    // Note que ajustei os nomes das colunas para snake_case (padrão de banco)
    private static final String CREATE_VEHICLE_TABLE = """
            CREATE TABLE IF NOT EXISTS vehicle (
                id INT PRIMARY KEY AUTO_INCREMENT,
                license_plate VARCHAR(20) UNIQUE NOT NULL,
                model VARCHAR(100) NOT NULL,
                manufacturing_date DATE NOT NULL,
                status VARCHAR(20) NOT NULL
            );
            """;

    @BeforeAll
    static void setupGlobal() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS vehicle");
            stmt.execute(CREATE_VEHICLE_TABLE);

        } catch (SQLException e) {
            fail("Falha ao configurar banco de dados inicial: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDownGlobal() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS vehicle");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setup() {
        repository = new VehicleRepositoryImpl();
        service = new VehicleService(repository);

        // Limpa a tabela antes de cada teste para garantir isolamento
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("TRUNCATE TABLE vehicle");

        } catch (SQLException e) {
            fail("Erro ao limpar tabela: " + e.getMessage());
        }
    }

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
        List<Vehicle> lista = repository.findAll();

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
        repository.update(paraAtualizar);

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
        repository.delete(id);

        // Validação
        try (Connection conn = DatabaseConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT count(*) FROM vehicle WHERE id = " + id)) {

            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }

    // --- MÉTODOS AUXILIARES (Para não depender do DAO nos testes) ---

    private int inserirVeiculoSQL(String plate, String model, LocalDate date, VehicleStatus status) throws SQLException {
        String sql = "INSERT INTO vehicle (license_plate, model, manufacturing_date, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plate);
            stmt.setString(2, model);
            stmt.setDate(3, java.sql.Date.valueOf(date)); // Conversão LocalDate -> SQL Date
            stmt.setString(4, status.name());             // Conversão Enum -> String

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }
}