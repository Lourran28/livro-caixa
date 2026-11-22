package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.TipoSaida;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link TipoSaida}.
 */
public interface TipoSaidaDAO {

    /**
     * Busca um tipo de saída pelo identificador.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<TipoSaida> buscarPorId(Integer id);

    /**
     * Lista todos os tipos de saída cadastrados.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<TipoSaida> listar();

    /**
     * Insere um novo tipo de saída e preenche o identificador gerado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(TipoSaida tipoSaida);

    /**
     * Atualiza um tipo de saída existente.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizar(TipoSaida tipoSaida);

    /**
     * Exclui o tipo de saída pelo identificador.
     * Caso existam saídas vinculadas ({@code ON DELETE RESTRICT}), deve lançar
     * {@link PersistenciaException#violacaoIntegridade(String, Throwable)}.
     *
     * @throws PersistenciaException em caso de violação de integridade ou erro de banco
     */
    void excluir(Integer id);
}
