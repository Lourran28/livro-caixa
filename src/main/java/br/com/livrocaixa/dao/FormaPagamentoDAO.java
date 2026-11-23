package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.FormaPagamento;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link FormaPagamento}.
 */
public interface FormaPagamentoDAO {

    /**
     * Busca uma forma de pagamento pelo identificador.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<FormaPagamento> buscarPorId(Integer id);

    /**
     * Lista todas as formas de pagamento cadastradas.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<FormaPagamento> listar();

    /**
     * Insere uma nova forma de pagamento e preenche o identificador gerado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(FormaPagamento formaPagamento);

    /**
     * Atualiza uma forma de pagamento existente.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizar(FormaPagamento formaPagamento);

    /**
     * Exclui a forma de pagamento pelo identificador.
     * Caso existam pagamentos vinculados ({@code ON DELETE RESTRICT}), deve lançar
     * {@link PersistenciaException#violacaoIntegridade(String, Throwable)}.
     *
     * @throws PersistenciaException em caso de violação de integridade ou erro de banco
     */
    void excluir(Integer id);
}
