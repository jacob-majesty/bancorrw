package bancorrw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Koete Jacob
 */
public class TestaAmbiente {
    private final String URL = "jdbc:mysql://localhost/bancorrw";
    private final String USER = "root";
    private final String PWD = "root";

    public TestaAmbiente() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Testa se o JUnit está funcionando
     */
    @Test
    public void testaJunit() {
        // Assert true for a successful test
        assertTrue(true);
    }

    /**
     * Testa se o driver JDBC e a conexão com o banco de dados funcionam.
     */
    @Test
    public void testaDriverJDBCeConexao() throws SQLException {
        DriverManager.getConnection(URL, USER, PWD);
    }

    /**
     * Testa se as tabelas do BD foram criadas.
     * @throws java.sql.SQLException
     */
    @Test
    public void testaExisteTabelasBD() throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PWD);
             PreparedStatement stmt = con.prepareStatement("SHOW TABLES");
             ResultSet rs = stmt.executeQuery()) {

            // Pula para o primeiro resultado e verifica o nome da tabela
            rs.next();
            assertEquals("clientes", rs.getString(1));

            // Pula para o segundo resultado e verifica o nome da tabela
            rs.next();
            assertEquals("contas", rs.getString(1));

            // Pula para o terceiro resultado e verifica o nome da tabela
            rs.next();
            assertEquals("contas_corrente", rs.getString(1));

            // Pula para o quarto resultado e verifica o nome da tabela
            rs.next();
            assertEquals("contas_investimento", rs.getString(1));
        }
    }
}