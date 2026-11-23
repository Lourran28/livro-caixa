package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.FuncionarioDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Funcionario;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller para manutenção do cadastro de funcionários (UC002, RF02).
 * Acesso exclusivo do perfil Administrador.
 */
public class FuncionarioController extends ControllerBase {

    private final FuncionarioDAO funcionarioDAO;

    public FuncionarioController(FuncionarioDAO funcionarioDAO) {
        this(funcionarioDAO, SessaoUsuario.getInstancia());
    }

    public FuncionarioController(FuncionarioDAO funcionarioDAO, SessaoUsuario sessao) {
        super(sessao);
        this.funcionarioDAO = Objects.requireNonNull(
                funcionarioDAO, "FuncionarioDAO não pode ser nulo.");
    }

    /**
     * Lista todos os funcionários cadastrados.
     */
    public List<Funcionario> listar() {
        exigirAdministrador();
        return funcionarioDAO.listar();
    }

    /**
     * Busca um funcionário pelo identificador.
     */
    public Optional<Funcionario> buscarPorId(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o funcionário");
        return funcionarioDAO.buscarPorId(id);
    }

    /**
     * Salva um funcionário (insere se {@code id == null}, atualiza caso contrário)
     * após validar os dados obrigatórios no model.
     */
    public Funcionario salvar(Funcionario funcionario) {
        exigirAdministrador();
        if (funcionario == null) {
            throw new ValidacaoException("Os dados do funcionário devem ser informados.");
        }
        funcionario.validar();
        if (funcionario.getId() == null) {
            funcionarioDAO.inserir(funcionario);
        } else {
            funcionarioDAO.atualizar(funcionario);
        }
        return funcionario;
    }

    /**
     * Exclui um funcionário pelo identificador. Impede a exclusão do próprio usuário
     * autenticado na sessão e de funcionários com movimentações vinculadas.
     */
    public void excluir(Integer id) {
        Funcionario logado = exigirAdministrador();
        validarIdObrigatorio(id, "o funcionário");
        if (Objects.equals(logado.getId(), id)) {
            throw new ValidacaoException("Não é possível excluir o funcionário atualmente logado.");
        }
        executarExclusao(() -> funcionarioDAO.excluir(id), "o funcionário");
    }
}
