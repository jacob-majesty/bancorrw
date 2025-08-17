package bancorrw.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;

public class DaoFactory {
    private DaoFactory() {
    }

    public static ClienteDao getClienteDao(DaoType type) throws SQLException, IOException {
        if (type == DaoType.SQL) {
            return new ClienteDaoSql(ConnectionFactory.getConnection());
        }
        throw new UnsupportedOperationException("Tipo de DAO não suportado: " + type);
    }

    public static ContaCorrenteDao getContaCorrenteDao(DaoType type) throws SQLException, IOException {
        if (type == DaoType.SQL) {
            return new ContaCorrenteDaoSql(ConnectionFactory.getConnection());
        }
        throw new UnsupportedOperationException("Tipo de DAO não suportado: " + type);
    }

    public static ContaInvestimentoDao getContaInvestimentoDao(DaoType type) throws SQLException, IOException {
        if (type == DaoType.SQL) {
            return new ContaInvestimentoDaoSql(ConnectionFactory.getConnection());
        }
        throw new UnsupportedOperationException("Tipo de DAO não suportado: " + type);
    }
}