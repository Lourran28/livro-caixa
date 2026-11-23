package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.TipoConsulta;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link TipoConsulta}.
 */
public interface TipoConsultaDAO {

    /**
     * Busca um tipo de consulta pelo identificador.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<TipoConsulta> buscarPorId(Integer id);

    /**
     * Lista todos os tipos de consulta cadastrados.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<TipoConsulta> listar();

    /**
     * Insere um novo tipo de consulta e preenche o identificador gerado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(TipoConsulta tipoConsulta);

    /**
     * Atualiza um tipo de consulta existente.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizar(TipoConsulta tipoConsulta);

    /**
     * Exclui o tipo de consulta pelo identificador.
     * Caso existam consultas vinculadas ({@code ON DELETE RESTRICT}), deve lançar
     * {@link PersistenciaException#violacaoIntegridade(String, Throwable)}.
     *
     * @throws PersistenciaException em caso de violação de integridade ou erro de banco
     */
    void excluir(Integer id);
}
