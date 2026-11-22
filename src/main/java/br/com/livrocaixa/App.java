package br.com.livrocaixa;

import java.sql.Connection;

import br.com.livrocaixa.config.ConexaoFactory;

/** Ponto de entrada. Por enquanto só testa a conexão; depois abrirá a tela de login. */
public class App {
    public static void main(String[] args) {
        try (Connection c = ConexaoFactory.getConexao()) {
            System.out.println("Conectado ao banco: " + c.getCatalog());
        } catch (Exception e) {
            System.err.println("Falha ao conectar: " + e.getMessage());
        }
    }
}
