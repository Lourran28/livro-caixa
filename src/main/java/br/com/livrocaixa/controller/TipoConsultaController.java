package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.TipoConsultaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.TipoConsulta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller para manutenção dos tipos de consulta realizados pelo escritório (UC002, RF03).
 * Acesso exclusivo do perfil Administrador.
 */
public class TipoConsultaController extends ControllerBase {

    private final TipoConsultaDAO tipoConsultaDAO;

    public TipoConsultaController(TipoConsultaDAO tipoConsultaDAO) {
        this(tipoConsultaDAO, SessaoUsuario.getInstancia());
    }

    public TipoConsultaController(TipoConsultaDAO tipoConsultaDAO, SessaoUsuario sessao) {
        super(sessao);
        this.tipoConsultaDAO = Objects.requireNonNull(
                tipoConsultaDAO, "TipoConsultaDAO não pode ser nulo.");
    }

    /**
     * Lista todos os tipos de consulta cadastrados.
     */
    public List<TipoConsulta> listar() {
        exigirAdministrador();
        return tipoConsultaDAO.listar();
    }

    /**
     * Busca um tipo de consulta pelo identificador.
     */
    public Optional<TipoConsulta> buscarPorId(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o tipo de consulta");
        return tipoConsultaDAO.buscarPorId(id);
    }

    /**
     * Salva um tipo de consulta (insere se {@code id == null}, atualiza caso contrário).
     */
    public TipoConsulta salvar(TipoConsulta tipoConsulta) {
        exigirAdministrador();
        if (tipoConsulta == null) {
            throw new ValidacaoException("Os dados do tipo de consulta devem ser informados.");
        }
        tipoConsulta.validar();
        if (tipoConsulta.getId() == null) {
            tipoConsultaDAO.inserir(tipoConsulta);
        } else {
            tipoConsultaDAO.atualizar(tipoConsulta);
        }
        return tipoConsulta;
    }

    /**
     * Exclui um tipo de consulta pelo identificador, impedindo exclusão se estiver em uso.
     */
    public void excluir(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o tipo de consulta");
        executarExclusao(() -> tipoConsultaDAO.excluir(id), "o tipo de consulta");
    }
}
