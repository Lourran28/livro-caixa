package br.com.livrocaixa.controller;

import br.com.livrocaixa.exception.AcessoNegadoException;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.model.Perfil;

import java.util.Locale;
import java.util.Objects;

/**
 * Classe base para os controllers do sistema. Centraliza a verificação obrigatória
 * de autenticação e permissões por perfil (RNF001) e utilitários comuns de validação
 * e tradução de erros de integridade referencial.
 */
public abstract class ControllerBase {

    protected final SessaoUsuario sessao;

    protected ControllerBase() {
        this(SessaoUsuario.getInstancia());
    }

    protected ControllerBase(SessaoUsuario sessao) {
        this.sessao = Objects.requireNonNull(sessao, "SessaoUsuario não pode ser nula.");
    }

    /**
     * Garante que há um usuário autenticado na sessão e retorna esse funcionário.
     *
     * @throws AcessoNegadoException se não houver usuário autenticado
     */
    protected Funcionario exigirLogin() {
        Funcionario usuario = sessao.getUsuarioLogado();
        if (usuario == null) {
            throw new AcessoNegadoException("Acesso negado. Faça login para utilizar o sistema.");
        }
        return usuario;
    }

    /**
     * Garante que o usuário autenticado possui perfil de Administrador.
     *
     * @throws AcessoNegadoException se não estiver logado ou não for Administrador
     */
    protected Funcionario exigirAdministrador() {
        Funcionario usuario = exigirLogin();
        if (!usuario.isAdministrador()) {
            throw new AcessoNegadoException(
                    "Acesso negado. Esta operação é permitida apenas para o perfil Administrador.");
        }
        return usuario;
    }

    /**
     * Garante que o usuário autenticado possui perfil de Administrador ou Recepcionista.
     *
     * @throws AcessoNegadoException se não estiver logado ou tiver perfil não autorizado
     */
    protected Funcionario exigirAdministradorOuRecepcionista() {
        Funcionario usuario = exigirLogin();
        Perfil perfil = usuario.getPerfil();
        boolean autorizado = usuario.isAdministrador() || (perfil != null && perfil.isRecepcionista());
        if (!autorizado) {
            throw new AcessoNegadoException(
                    "Acesso negado. Perfil de usuário sem permissão para esta operação.");
        }
        return usuario;
    }

    /**
     * Valida que um identificador obrigatório foi informado e é positivo.
     */
    protected void validarIdObrigatorio(Integer id, String nomeEntidade) {
        if (id == null || id <= 0) {
            throw new ValidacaoException("Informe um identificador válido para " + nomeEntidade + ".");
        }
    }

    /**
     * Executa uma operação de exclusão no DAO traduzindo violações de chave estrangeira
     * ({@code ON DELETE RESTRICT}) em uma {@link ValidacaoException} amigável ao usuário.
     */
    protected void executarExclusao(Runnable operacaoExclusao, String nomeEntidade) {
        try {
            operacaoExclusao.run();
        } catch (PersistenciaException e) {
            if (isErroIntegridadeReferencial(e)) {
                throw new ValidacaoException(
                        "Não é possível excluir " + nomeEntidade
                                + " porque há registros vinculados no sistema.");
            }
            throw e;
        }
    }

    private boolean isErroIntegridadeReferencial(PersistenciaException e) {
        if (e.isViolacaoIntegridade()) {
            return true;
        }
        Throwable causa = e.getCause();
        while (causa != null) {
            String nomeClasse = causa.getClass().getName();
            String msg = causa.getMessage() == null ? "" : causa.getMessage().toLowerCase(Locale.ROOT);
            if (nomeClasse.contains("SQLIntegrityConstraintViolationException")
                    || msg.contains("foreign key")
                    || msg.contains("cannot delete or update a parent row")
                    || msg.contains("restrict")) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }
}
