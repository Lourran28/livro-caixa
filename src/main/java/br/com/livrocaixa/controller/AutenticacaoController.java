package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.FuncionarioDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Funcionario;

import java.util.Objects;

/**
 * Controller responsável pela autenticação e encerramento de sessão dos usuários
 * (UC001, RF01, RNF001).
 */
public class AutenticacaoController extends ControllerBase {

    private static final String MSG_CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    private final FuncionarioDAO funcionarioDAO;

    public AutenticacaoController(FuncionarioDAO funcionarioDAO) {
        this(funcionarioDAO, SessaoUsuario.getInstancia());
    }

    public AutenticacaoController(FuncionarioDAO funcionarioDAO, SessaoUsuario sessao) {
        super(sessao);
        this.funcionarioDAO = Objects.requireNonNull(funcionarioDAO, "FuncionarioDAO não pode ser nulo.");
    }

    /**
     * Valida as credenciais informadas e inicia a sessão do usuário autenticado.
     * Sempre retorna mensagem genérica em caso de falha para não expor qual campo falhou.
     *
     * @param nome  nome do funcionário cadastrado
     * @param senha senha de acesso
     * @return funcionário autenticado
     * @throws ValidacaoException se as credenciais forem inválidas
     */
    public Funcionario login(String nome, String senha) {
        if (nome == null || nome.isBlank() || senha == null || senha.isEmpty()) {
            throw new ValidacaoException(MSG_CREDENCIAIS_INVALIDAS);
        }

        Funcionario funcionario = funcionarioDAO.buscarPorNome(nome.trim())
                .orElseThrow(() -> new ValidacaoException(MSG_CREDENCIAIS_INVALIDAS));

        if (!funcionario.senhaConfere(senha) || funcionario.getPerfil() == null) {
            throw new ValidacaoException(MSG_CREDENCIAIS_INVALIDAS);
        }

        sessao.iniciar(funcionario);
        return funcionario;
    }

    /**
     * Encerra a sessão do usuário atualmente autenticado.
     */
    public void logout() {
        exigirLogin();
        sessao.encerrar();
    }

    /**
     * Retorna o usuário atualmente logado na sessão.
     */
    public Funcionario getUsuarioLogado() {
        return exigirLogin();
    }
}
