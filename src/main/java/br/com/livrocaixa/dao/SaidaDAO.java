package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Saida;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de persistência para a entidade {@link Saida} (gastos do escritório).
 */
public interface SaidaDAO {

    /**
     * Insere uma nova saída no livro caixa e preenche o identificador gerado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(Saida saida);

    /**
     * Lista todas as saídas cuja data esteja no intervalo fechado {@code [inicio, fim]}.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Saida> listarPorPeriodo(LocalDate inicio, LocalDate fim);

    /**
     * Lista todas as saídas registradas por um determinado funcionário (RF10).
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Saida> listarPorFuncionario(Integer idFuncionario);
}
