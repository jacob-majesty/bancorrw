package bancorrw.dao;

import bancorrw.cliente.Cliente;
import bancorrw.conta.ContaCorrente;
import bancorrw.conta.ContaInvestimento;
import bancorrw.exception.DbIntegrityException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClienteDaoSql implements ClienteDao {
    private final Connection conn;

    private String selectAll =
            "SELECT " +
                    "id_cliente, " +
                    "nome, " +
                    "cpf, " +
                    "data_nascimento, " +
                    "cartao_credito " +
                    "FROM " +
                    "clientes ";

    private String selectByName = selectAll + " " +
            "WHERE " +
            "nome LIKE ?";

    private String insertCliente =
            "INSERT INTO " +
                    "clientes " +
                    "(nome," +
                    "cpf," +
                    "data_nascimento, " +
                    "cartao_credito) " +
                    "VALUES" +
                    "(?,?,?,?)";
    private String updateCliente =
            "UPDATE " +
                    "clientes " +
                    "SET " +
                    "nome=?, " +
                    "cpf=?, " +
                    "data_nascimento=?, " +
                    "cartao_credito=? " +
                    "WHERE id_cliente = ?";

    private String deleteById =
            "DELETE FROM " +
                    "clientes " +
                    "WHERE id_cliente = ?";

    private String deleteAll =
            "DELETE FROM " +
                    "clientes ";

    private final String ressetAIPessoas = "ALTER TABLE clientes AUTO_INCREMENT = 1";
    private final String ressetAIContas = "ALTER TABLE contas AUTO_INCREMENT = 1";

    public ClienteDaoSql(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void add(Cliente cliente) throws Exception {
        String sql = "INSERT INTO clientes (nome, cpf, data_nascimento, cartao_credito) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, cliente.getNome());
            st.setString(2, cliente.getCpf());
            st.setDate(3, java.sql.Date.valueOf(cliente.getDataNascimento()));
            st.setString(4, cliente.getCartaoCredito());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        long idGerado = rs.getLong(1);
                        cliente.setId(idGerado);
                    }
                }
            } else {
                throw new DbIntegrityException("Nenhum cliente inserido no banco de dados.");
            }
        } catch (SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        }
    }

    @Override
    public List<Cliente> getAll() throws Exception {
        String query = "SELECT cl.*, c.id_conta as id_conta_corrente " +
                "FROM clientes cl " +
                "LEFT JOIN contas c ON cl.id_cliente = c.id_cliente " +
                "LEFT JOIN contas_corrente cc ON c.id_conta = cc.id_conta";

        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                clientes.add(instanciarCliente(rs));
            }
        }
        return clientes;
    }

    @Override
    public Cliente getById(long id) throws Exception {
        String query =
                "SELECT cl.*, c.id_conta as id_conta_corrente " +
                        "FROM clientes cl " +
                        "LEFT JOIN contas c ON cl.id_cliente = c.id_cliente " +
                        "LEFT JOIN contas_corrente cc ON c.id_conta = cc.id_conta " +
                        "WHERE cl.id_cliente = ?";

        try (PreparedStatement st = conn.prepareStatement(query)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarCliente(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void update(Cliente cliente) throws Exception {
        try (PreparedStatement st = conn.prepareStatement(updateCliente)) {
            st.setString(1, cliente.getNome());
            st.setString(2, cliente.getCpf());
            st.setDate(3, java.sql.Date.valueOf(cliente.getDataNascimento()));
            st.setString(4, cliente.getCartaoCredito());
            st.setLong(5, cliente.getId());
            st.executeUpdate();
        }
    }

    @Override
    public void delete(Cliente cliente) throws Exception {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, cliente.getId());
            st.executeUpdate();
            // Adicione esta linha para zerar o ID do objeto Cliente
            cliente.setId(-1);
        } catch (SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        }
    }

    @Override
    public void deleteAll() throws Exception {
        String sqlDeleteContasInvestimento = "DELETE FROM contas_investimento";
        String sqlDeleteContasCorrente = "DELETE FROM contas_corrente";
        String sqlDeleteContas = "DELETE FROM contas";
        String sqlDeleteClientes = "DELETE FROM clientes";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement()) {

            // Exclui os dados das tabelas filhas primeiro
            st.executeUpdate(sqlDeleteContasInvestimento);
            st.executeUpdate(sqlDeleteContasCorrente);
            st.executeUpdate(sqlDeleteContas);
            st.executeUpdate(sqlDeleteClientes);

            // Reseta o contador de auto-incremento
            st.executeUpdate("ALTER TABLE contas AUTO_INCREMENT = 1");
            st.executeUpdate("ALTER TABLE clientes AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        }
    }

    private Cliente instanciarCliente(ResultSet rs) throws SQLException {
        int idCliente = rs.getInt("id_cliente");
        String nome = rs.getString("nome");
        String cpf = rs.getString("cpf");
        LocalDate dataNascimento = rs.getDate("data_nascimento").toLocalDate();
        String cartaoCredito = rs.getString("cartao_credito");
        long idContaCorrente = rs.getLong("id_conta_corrente");

        Cliente cliente = new Cliente(idCliente, nome, cpf, dataNascimento, cartaoCredito);

        if (idContaCorrente > 0) {
            try {
                ContaCorrenteDao corDao = new ContaCorrenteDaoSql(ConnectionFactory.getConnection());
                ContaCorrente contaCorrente = corDao.getById(idContaCorrente);
                // Associa a conta corrente ao cliente
                cliente.setContaCorrente(contaCorrente);
            } catch (Exception e) {
                System.err.println("Erro ao buscar conta corrente para o cliente: " + e.getMessage());
            }
        }

        try {
            ContaInvestimentoDao invDao = new ContaInvestimentoDaoSql(ConnectionFactory.getConnection());
            List<ContaInvestimento> contasInvestimento = invDao.getContasInvestimentoByCliente(cliente);
            // Associa a lista de contas de investimento ao cliente
            cliente.setContasInvestimento(contasInvestimento);
        } catch (Exception e) {
            System.err.println("Erro ao buscar contas de investimento para o cliente: " + e.getMessage());
        }

        return cliente;
    }
}