package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Pagamento;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de leitura para a entidade {@link Pagamento} (entradas efetivas do livro caixa).
 * As operações de escrita de pagamentos ocorrem transacionalmente via {@link ParcelaDAO}.
 */
public interface PagamentoDAO {

    /**
     * Lista todos os pagamentos cuja data esteja no intervalo fechado {@code [inicio, fim]}.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Pagamento> listarPorPeriodo(LocalDate inicio, LocalDate fim);

    /**
     * Lista todos os pagamentos registrados por um determinado funcionário (RF10).
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Pagamento> listarPorFuncionario(Integer idFuncionario);
}
