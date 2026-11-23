package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.TipoSaidaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.TipoSaida;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller para manutenção dos tipos de saída (gastos do escritório) (UC002, RF03).
 * Liberado para os perfis Administrador e Recepcionista.
 */
public class TipoSaidaController extends ControllerBase {

    private final TipoSaidaDAO tipoSaidaDAO;

    public TipoSaidaController(TipoSaidaDAO tipoSaidaDAO) {
        this(tipoSaidaDAO, SessaoUsuario.getInstancia());
    }

    public TipoSaidaController(TipoSaidaDAO tipoSaidaDAO, SessaoUsuario sessao) {
        super(sessao);
        this.tipoSaidaDAO = Objects.requireNonNull(tipoSaidaDAO, "TipoSaidaDAO não pode ser nulo.");
    }

    /**
     * Lista todos os tipos de saída cadastrados.
     */
    public List<TipoSaida> listar() {
        exigirAdministradorOuRecepcionista();
        return tipoSaidaDAO.listar();
    }

    /**
     * Busca um tipo de saída pelo identificador.
     */
    public Optional<TipoSaida> buscarPorId(Integer id) {
        exigirAdministradorOuRecepcionista();
        validarIdObrigatorio(id, "o tipo de saída");
        return tipoSaidaDAO.buscarPorId(id);
    }

    /**
     * Salva um tipo de saída (insere se {@code id == null}, atualiza caso contrário).
     */
    public TipoSaida salvar(TipoSaida tipoSaida) {
        exigirAdministradorOuRecepcionista();
        if (tipoSaida == null) {
            throw new ValidacaoException("Os dados do tipo de saída devem ser informados.");
        }
        tipoSaida.validar();
        if (tipoSaida.getId() == null) {
            tipoSaidaDAO.inserir(tipoSaida);
        } else {
            tipoSaidaDAO.atualizar(tipoSaida);
        }
        return tipoSaida;
    }

    /**
     * Exclui um tipo de saída pelo identificador, impedindo exclusão se estiver em uso.
     */
    public void excluir(Integer id) {
        exigirAdministradorOuRecepcionista();
        validarIdObrigatorio(id, "o tipo de saída");
        executarExclusao(() -> tipoSaidaDAO.excluir(id), "o tipo de saída");
    }
}
