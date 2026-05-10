package br.com.dev.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public class DatabaseService {

    private static final HikariDataSource dataSource;

    static {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/test");
        config.setUsername("test");
        config.setPassword("test");

        config.setMaximumPoolSize(100); // gargalo real (altere aqui para as medições de contenções)

        dataSource = new HikariDataSource(config);
    }

    public static String query() {
        boolean deterministic = true; //Você pode testar de forma deterministica ou real, direto em uma tabela
        String sql = deterministic
                ? "SELECT pg_sleep(0.2)"
                : "SELECT * FROM carro";

        try (Connection conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {

            var rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return "OK_DB";
            }

        } catch (Exception e) {
            return "ERROR";
        }

        IO.println("Active: " + dataSource.getHikariPoolMXBean().getActiveConnections());
        IO.println("Idle: " + dataSource.getHikariPoolMXBean().getIdleConnections());

        return "OK_DB";
    }
}