package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Funcionario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link Funcionario}.
 * Todas as consultas devem carregar o {@code Perfil} associado ao funcionário.
 */
public interface FuncionarioDAO {

    /**
     * Busca um funcionário pelo nome (utilizado na autenticação) já com seu {@code Perfil} carregado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Funcionario> buscarPorNome(String nome);

    /**
     * Busca um funcionário pelo identificador já com seu {@code Perfil} carregado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Funcionario> buscarPorId(Integer id);

    /**
     * Lista todos os funcionários cadastrados com seus respectivos perfis.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Funcionario> listar();

    /**
     * Insere um novo funcionário e preenche o identificador gerado.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void inserir(Funcionario funcionario);

    /**
     * Atualiza os dados de um funcionário existente.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizar(Funcionario funcionario);

    /**
     * Exclui o funcionário pelo identificador.
     * Caso existam consultas, pagamentos ou saídas vinculadas ({@code ON DELETE RESTRICT}),
     * deve lançar {@link PersistenciaException#violacaoIntegridade(String, Throwable)}.
     *
     * @throws PersistenciaException em caso de violação de integridade ou erro de banco
     */
    void excluir(Integer id);
}
