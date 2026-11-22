package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.PerfilDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Perfil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller para manutenção de perfis de usuário (UC002, RF02).
 * Acesso exclusivo do perfil Administrador.
 */
public class PerfilController extends ControllerBase {

    private final PerfilDAO perfilDAO;

    public PerfilController(PerfilDAO perfilDAO) {
        this(perfilDAO, SessaoUsuario.getInstancia());
    }

    public PerfilController(PerfilDAO perfilDAO, SessaoUsuario sessao) {
        super(sessao);
        this.perfilDAO = Objects.requireNonNull(perfilDAO, "PerfilDAO não pode ser nulo.");
    }

    /**
     * Lista todos os perfis cadastrados.
     */
    public List<Perfil> listar() {
        exigirAdministrador();
        return perfilDAO.listar();
    }

    /**
     * Busca um perfil pelo identificador.
     */
    public Optional<Perfil> buscarPorId(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o perfil");
        return perfilDAO.buscarPorId(id);
    }

    /**
     * Salva um perfil (insere se {@code id == null}, atualiza caso contrário) após validação.
     */
    public Perfil salvar(Perfil perfil) {
        exigirAdministrador();
        if (perfil == null) {
            throw new ValidacaoException("Os dados do perfil devem ser informados.");
        }
        perfil.validar();
        if (perfil.getId() == null) {
            perfilDAO.inserir(perfil);
        } else {
            perfilDAO.atualizar(perfil);
        }
        return perfil;
    }

    /**
     * Exclui um perfil pelo identificador, impedindo exclusão se estiver em uso.
     */
    public void excluir(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o perfil");
        executarExclusao(() -> perfilDAO.excluir(id), "o perfil");
    }
}
