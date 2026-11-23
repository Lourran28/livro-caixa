package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Perfil;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link Perfil}.
 */
public interface PerfilDAO {

    /**
     * Busca um perfil pelo identificador.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Perfil> buscarPorId(Integer id);

    /**
     * Lista todos os perfis cadastrados.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Perfil> listar();

    /**
     * Insere um novo perfil e atribui o identificador gerado ao objeto recebido.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(Perfil perfil);

    /**
     * Atualiza os dados de um perfil existente.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizar(Perfil perfil);

    /**
     * Exclui o perfil pelo identificador.
     * Caso existam funcionários vinculados ({@code ON DELETE RESTRICT}), deve lançar
     * {@link PersistenciaException#violacaoIntegridade(String, Throwable)}.
     *
     * @throws PersistenciaException em caso de violação de integridade ou erro de banco
     */
    void excluir(Integer id);
}
