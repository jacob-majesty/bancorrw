package bancorrw.dao;

import bancorrw.cliente.Cliente;
import bancorrw.conta.ContaCorrente;
import bancorrw.exception.DbIntegrityException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContaCorrenteDaoSql implements ContaCorrenteDao {
    private Connection conn;

    // Construtor
    public ContaCorrenteDaoSql(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void add(ContaCorrente contaCorrente) throws Exception {
        String sqlConta = "INSERT INTO contas (id_cliente, saldo) VALUES (?, ?)";
        String sqlContaCorrente = "INSERT INTO contas_corrente (id_conta, limite, taxa_juros_limite) VALUES (?, ?, ?)";
        try (PreparedStatement stConta = conn.prepareStatement(sqlConta, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stContaCorrente = conn.prepareStatement(sqlContaCorrente)) {

            conn.setAutoCommit(false);

            stConta.setLong(1, contaCorrente.getCliente().getId());
            stConta.setDouble(2, contaCorrente.getSaldo());
            int rowsAffected = stConta.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stConta.getGeneratedKeys()) {
                    if (rs.next()) {
                        long idConta = rs.getLong(1);
                        contaCorrente.setId(idConta);
                        stContaCorrente.setLong(1, idConta);
                        stContaCorrente.setDouble(2, contaCorrente.getLimite());
                        stContaCorrente.setDouble(3, contaCorrente.getTaxaJurosLimite());
                        stContaCorrente.executeUpdate();
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
    public List<ContaCorrente> getAll() throws Exception {
        String sql = "SELECT c.id_conta, c.saldo, cc.limite, cc.taxa_juros_limite, cl.id_cliente, cl.nome, cl.cpf, cl.data_nascimento, cl.cartao_credito FROM contas c INNER JOIN contas_corrente cc ON c.id_conta = cc.id_conta INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente";
        List<ContaCorrente> contas = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                contas.add(instanciarContaCorrente(rs));
            }
        }
        return contas;
    }

    @Override
    public ContaCorrente getById(long id) throws Exception {
        String sql = "SELECT c.id_conta, c.saldo, cc.limite, cc.taxa_juros_limite, cl.id_cliente, cl.nome, cl.cpf, cl.data_nascimento, cl.cartao_credito FROM contas c INNER JOIN contas_corrente cc ON c.id_conta = cc.id_conta INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente WHERE c.id_conta = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarContaCorrente(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void update(ContaCorrente contaCorrente) throws Exception {
        ContaCorrente contaAtual = getById(contaCorrente.getId());

        if (contaAtual != null &&
                contaAtual.getCliente().getId() != contaCorrente.getCliente().getId()) {

            Cliente clienteAntigo = contaAtual.getCliente();

            if (clienteAntigo.getContaCorrente() != null &&
                    clienteAntigo.getContaCorrente().getId() == contaAtual.getId()) {
                clienteAntigo.setContaCorrente(null);
            }
        }

        String sql = "UPDATE contas c JOIN contas_corrente cc ON c.id_conta = cc.id_conta " +
                "SET c.id_cliente = ?, c.saldo = ?, cc.limite = ?, cc.taxa_juros_limite = ? " +
                "WHERE c.id_conta = ?";

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, contaCorrente.getCliente().getId());
            st.setDouble(2, contaCorrente.getSaldo());
            st.setDouble(3, contaCorrente.getLimite());
            st.setDouble(4, contaCorrente.getTaxaJurosLimite());
            st.setLong(5, contaCorrente.getId());
            st.executeUpdate();

            contaCorrente.getCliente().setContaCorrente(contaCorrente);
        }
    }

    @Override
    public void delete(ContaCorrente contaCorrente) throws Exception {
        String sql = "DELETE FROM contas WHERE id_conta = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, contaCorrente.getId());
            st.executeUpdate();
            // Adicione esta linha
            contaCorrente.setId(-1);
        }
    }

    @Override
    public void deleteAll() throws Exception {
        String sqlDeleteContasCorrente = "DELETE FROM contas_corrente";
        String sqlDeleteContas = "DELETE FROM contas WHERE id_conta IN (SELECT id_conta FROM contas_corrente)";
        String sqlResetAutoIncrement = "ALTER TABLE contas AUTO_INCREMENT = 1";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sqlDeleteContasCorrente);
            st.executeUpdate(sqlDeleteContas);
            st.executeUpdate(sqlResetAutoIncrement);
        } catch (SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        }
    }

    @Override
    public ContaCorrente getContaCorrenteByCliente(Cliente cliente) throws Exception {
        String sql = "SELECT c.id_conta, c.saldo, cc.limite, cc.taxa_juros_limite, cl.id_cliente, cl.nome, cl.cpf, cl.data_nascimento, cl.cartao_credito FROM contas c INNER JOIN contas_corrente cc ON c.id_conta = cc.id_conta INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente WHERE c.id_cliente = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, cliente.getId());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarContaCorrente(rs);
                }
            }
        }
        return null;
    }

    // Código em ContaCorrenteDaoSql.java
    private ContaCorrente instanciarContaCorrente(ResultSet rs) throws Exception {
        long id = rs.getLong("id_conta");
        double saldo = rs.getDouble("saldo");
        double limite = rs.getDouble("limite");
        double taxaJuros = rs.getDouble("taxa_juros_limite");

        int idCliente = rs.getInt("id_cliente");
        String nomeCliente = rs.getString("nome");
        String cpfCliente = rs.getString("cpf");
        LocalDate dataNascimento = rs.getDate("data_nascimento").toLocalDate();
        String cartaoCredito = rs.getString("cartao_credito");

        // Cria o objeto Cliente
        Cliente cliente = new Cliente(idCliente, nomeCliente, cpfCliente, dataNascimento, cartaoCredito);

        // Cria o objeto ContaCorrente
        ContaCorrente contaCorrente = new ContaCorrente(limite, taxaJuros, id, cliente, saldo);

        // Adiciona a conta ao cliente
        cliente.setContaCorrente(contaCorrente);

        return contaCorrente;
    }
}