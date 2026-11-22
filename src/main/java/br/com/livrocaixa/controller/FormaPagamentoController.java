package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.FormaPagamentoDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.FormaPagamento;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller para manutenção das formas de pagamento disponíveis (UC002, RF02).
 * Acesso exclusivo do perfil Administrador.
 */
public class FormaPagamentoController extends ControllerBase {

    private final FormaPagamentoDAO formaPagamentoDAO;

    public FormaPagamentoController(FormaPagamentoDAO formaPagamentoDAO) {
        this(formaPagamentoDAO, SessaoUsuario.getInstancia());
    }

    public FormaPagamentoController(FormaPagamentoDAO formaPagamentoDAO, SessaoUsuario sessao) {
        super(sessao);
        this.formaPagamentoDAO = Objects.requireNonNull(
                formaPagamentoDAO, "FormaPagamentoDAO não pode ser nulo.");
    }

    /**
     * Lista todas as formas de pagamento cadastradas.
     */
    public List<FormaPagamento> listar() {
        exigirAdministrador();
        return formaPagamentoDAO.listar();
    }

    /**
     * Busca uma forma de pagamento pelo identificador.
     */
    public Optional<FormaPagamento> buscarPorId(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "a forma de pagamento");
        return formaPagamentoDAO.buscarPorId(id);
    }

    /**
     * Salva uma forma de pagamento (insere se {@code id == null}, atualiza caso contrário).
     */
    public FormaPagamento salvar(FormaPagamento formaPagamento) {
        exigirAdministrador();
        if (formaPagamento == null) {
            throw new ValidacaoException("Os dados da forma de pagamento devem ser informados.");
        }
        formaPagamento.validar();
        if (formaPagamento.getId() == null) {
            formaPagamentoDAO.inserir(formaPagamento);
        } else {
            formaPagamentoDAO.atualizar(formaPagamento);
        }
        return formaPagamento;
    }

    /**
     * Exclui uma forma de pagamento pelo identificador, impedindo exclusão se estiver em uso.
     */
    public void excluir(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "a forma de pagamento");
        executarExclusao(() -> formaPagamentoDAO.excluir(id), "a forma de pagamento");
    }
}
