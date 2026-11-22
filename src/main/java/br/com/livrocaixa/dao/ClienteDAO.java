package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link Cliente}.
 */
public interface ClienteDAO {

    /**
     * Busca um cliente pelo identificador.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Cliente> buscarPorId(Integer id);

    /**
     * Busca um cliente pelo CPF formatado ({@code 000.000.000-00}).
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Cliente> buscarPorCpf(String cpf);

    /**
     * Lista todos os clientes cadastrados.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Cliente> listar();

    /**
     * Insere um novo cliente e preenche o identificador gerado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(Cliente cliente);

    /**
     * Atualiza os dados de um cliente existente.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizar(Cliente cliente);

    /**
     * Exclui o cliente pelo identificador.
     * Caso existam consultas vinculadas ({@code ON DELETE RESTRICT}), deve lançar
     * {@link PersistenciaException#violacaoIntegridade(String, Throwable)}.
     *
     * @throws PersistenciaException em caso de violação de integridade ou erro de banco
     */
    void excluir(Integer id);
}
