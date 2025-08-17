package bancorrw.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private static final Properties properties = new Properties();

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException, IOException {
        if (properties.isEmpty()) {
            readProperties();
        }
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private static void readProperties() throws IOException {
        try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new FileNotFoundException("db.properties not found in the classpath");
            }
            properties.load(input);
        }
    }
}


