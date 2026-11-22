package br.com.livrocaixa.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fornece conexões MySQL. O mesmo .jar roda no servidor e no cliente; o que muda é o
 * arquivo "db.properties" (host = localhost no servidor, IP do servidor no cliente).
 *
 * Ordem de busca do arquivo: -Dlivrocaixa.config=caminho  ->  ./db.properties  ->  classpath.
 * Os DAOs usarão: try (Connection c = ConexaoFactory.getConexao()) { ... }
 */
public final class ConexaoFactory {
    private static Properties props;

    private ConexaoFactory() {}

    public static Connection getConexao() throws SQLException {
        Properties p = carregar();
        String url = "jdbc:mysql://" + p.getProperty("db.host", "localhost") + ":" + p.getProperty("db.port", "3306")
                + "/" + p.getProperty("db.name", "livro_caixa")
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Recife&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, p.getProperty("db.user"), p.getProperty("db.password"));
    }

    private static synchronized Properties carregar() {
        if (props != null) {
            return props;
        }
        Properties p = new Properties();
        Path externo = Path.of(System.getProperty("livrocaixa.config", "db.properties"));
        try {
            if (Files.exists(externo)) {
                try (InputStream in = Files.newInputStream(externo)) {
                    p.load(in);
                }
            } else {
                try (InputStream in = ConexaoFactory.class.getResourceAsStream("/db.properties")) {
                    if (in == null) {
                        throw new IllegalStateException("Arquivo db.properties não encontrado em "
                                + externo.toAbsolutePath() + ". Copie db.properties.example e ajuste.");
                    }
                    p.load(in);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler db.properties", e);
        }
        props = p;
        return props;
    }
}
