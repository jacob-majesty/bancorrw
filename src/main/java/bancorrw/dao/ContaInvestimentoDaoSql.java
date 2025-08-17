package bancorrw.dao;

import bancorrw.cliente.Cliente;
import bancorrw.conta.ContaInvestimento;
import bancorrw.exception.DbIntegrityException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContaInvestimentoDaoSql implements ContaInvestimentoDao {
    private Connection conn;

    // Construtor
    public ContaInvestimentoDaoSql(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void add(ContaInvestimento conta) throws Exception {
        String sqlConta = "INSERT INTO contas (id_cliente, saldo) VALUES (?, ?)";
        String sqlContaInvestimento = "INSERT INTO contas_investimento (id_conta, taxa_remuneracao_investimento, montante_minimo, deposito_minimo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stConta = conn.prepareStatement(sqlConta, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stContaInvestimento = conn.prepareStatement(sqlContaInvestimento)) {

            conn.setAutoCommit(false);

            stConta.setLong(1, conta.getCliente().getId());
            stConta.setDouble(2, conta.getSaldo());
            int rowsAffected = stConta.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stConta.getGeneratedKeys()) {
                    if (rs.next()) {
                        long idConta = rs.getLong(1);
                        conta.setId(idConta);

                        stContaInvestimento.setLong(1, idConta);
                        stContaInvestimento.setDouble(2, conta.getTaxaRemuneracaoInvestimento());
                        stContaInvestimento.setDouble(3, conta.getMontanteMinimo());
                        stContaInvestimento.setDouble(4, conta.getDepositoMinimo());
                        stContaInvestimento.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new DbIntegrityException(e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public List<ContaInvestimento> getAll() throws Exception {
        String sql = "SELECT c.id_conta, c.saldo, ci.taxa_remuneracao_investimento, ci.montante_minimo, ci.deposito_minimo, cl.id_cliente, cl.nome, cl.cpf, cl.data_nascimento, cl.cartao_credito FROM contas c INNER JOIN contas_investimento ci ON c.id_conta = ci.id_conta INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente";
        List<ContaInvestimento> contas = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                contas.add(instanciarContaInvestimento(rs));
            }
        }
        return contas;
    }

    @Override
    public ContaInvestimento getById(long id) throws Exception {
        String sql = "SELECT c.id_conta, c.saldo, ci.taxa_remuneracao_investimento, ci.montante_minimo, ci.deposito_minimo, cl.id_cliente, cl.nome, cl.cpf, cl.data_nascimento, cl.cartao_credito FROM contas c INNER JOIN contas_investimento ci ON c.id_conta = ci.id_conta INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente WHERE c.id_conta = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarContaInvestimento(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void update(ContaInvestimento contaInvestimento) throws Exception {
        String sql = "UPDATE contas c JOIN contas_investimento ci ON c.id_conta = ci.id_conta SET c.saldo = ?, ci.taxa_remuneracao_investimento = ?, ci.montante_minimo = ?, ci.deposito_minimo = ? WHERE c.id_conta = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setDouble(1, contaInvestimento.getSaldo());
            st.setDouble(2, contaInvestimento.getTaxaRemuneracaoInvestimento());
            st.setDouble(3, contaInvestimento.getMontanteMinimo());
            st.setDouble(4, contaInvestimento.getDepositoMinimo());
            st.setLong(5, contaInvestimento.getId());
            st.executeUpdate();
        }
    }

    @Override
    public void delete(ContaInvestimento contaInvestimento) throws Exception {
        String sql = "DELETE FROM contas WHERE id_conta = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, contaInvestimento.getId());
            st.executeUpdate();
        }
    }

    @Override
    public void deleteAll() throws Exception {
        String sqlDeleteContasInvestimento = "DELETE FROM contas_investimento";
        String sqlDeleteContas = "DELETE FROM contas WHERE id_conta IN (SELECT id_conta FROM contas_investimento)";
        String sqlResetAutoIncrement = "ALTER TABLE contas AUTO_INCREMENT = 1";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sqlDeleteContasInvestimento);
            st.executeUpdate(sqlDeleteContas);
            st.executeUpdate(sqlResetAutoIncrement);
        } catch (SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        }
    }

    @Override
    public List<ContaInvestimento> getContasInvestimentoByCliente(Cliente cliente) throws Exception {
        String sql = "SELECT c.id_conta, c.saldo, ci.taxa_remuneracao_investimento, ci.montante_minimo, ci.deposito_minimo, cl.id_cliente, cl.nome, cl.cpf, cl.data_nascimento, cl.cartao_credito FROM contas c INNER JOIN contas_investimento ci ON c.id_conta = ci.id_conta INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente WHERE c.id_cliente = ?";
        List<ContaInvestimento> contas = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, cliente.getId());
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    contas.add(instanciarContaInvestimento(rs));
                }
            }
        }
        return contas;
    }

    private ContaInvestimento instanciarContaInvestimento(ResultSet rs) throws SQLException {
        long id = rs.getLong("id_conta");
        double saldo = rs.getDouble("saldo");
        double taxaRemuneracao = rs.getDouble("taxa_remuneracao_investimento");
        double montanteMinimo = rs.getDouble("montante_minimo");
        double depositoMinimo = rs.getDouble("deposito_minimo");

        int idCliente = rs.getInt("id_cliente");
        String nomeCliente = rs.getString("nome");
        String cpfCliente = rs.getString("cpf");
        LocalDate dataNascimento = rs.getDate("data_nascimento").toLocalDate();
        String cartaoCredito = rs.getString("cartao_credito");

        Cliente cliente = new Cliente(idCliente, nomeCliente, cpfCliente, dataNascimento, cartaoCredito);

        return new ContaInvestimento(taxaRemuneracao, montanteMinimo, depositoMinimo, saldo, id, cliente);
    }
}