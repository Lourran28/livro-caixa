package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.SaidaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.model.Saida;
import br.com.livrocaixa.model.TipoSaida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Controller responsável pelo registro de saídas (gastos) do escritório (UC004, RF05, RNF005).
 * Liberado para Administrador e Recepcionista.
 */
public class SaidaController extends ControllerBase {

    private final SaidaDAO saidaDAO;
    private final TipoSaidaController tipoSaidaController;

    public SaidaController(SaidaDAO saidaDAO) {
        this(saidaDAO, null, SessaoUsuario.getInstancia());
    }

    public SaidaController(SaidaDAO saidaDAO, TipoSaidaController tipoSaidaController) {
        this(saidaDAO, tipoSaidaController, SessaoUsuario.getInstancia());
    }

    public SaidaController(
            SaidaDAO saidaDAO,
            TipoSaidaController tipoSaidaController,
            SessaoUsuario sessao) {
        super(sessao);
        this.saidaDAO = Objects.requireNonNull(saidaDAO, "SaidaDAO não pode ser nulo.");
        this.tipoSaidaController = tipoSaidaController;
    }

    /**
     * Registra uma nova saída no livro caixa associada ao funcionário logado.
     * As regras de campos obrigatórios e valor maior que zero são validadas pelo model {@link Saida}.
     *
     * @param data       data do gasto
     * @param valor      valor retirado do caixa (deve ser > 0)
     * @param tipoSaida  classificação do gasto
     * @param observacao descrição ou observação opcional
     * @return saída persistida
     */
    public Saida registrarSaida(
            LocalDate data,
            BigDecimal valor,
            TipoSaida tipoSaida,
            String observacao) {
        Funcionario usuarioLogado = exigirAdministradorOuRecepcionista();

        Saida saida = new Saida();
        saida.setData(data);
        saida.setValor(valor);
        saida.setTipoSaida(tipoSaida);
        saida.setObservacao(observacao);
        saida.setFuncionario(usuarioLogado);

        saida.validar();
        saidaDAO.inserir(saida);
        return saida;
    }

    /**
     * Permite cadastrar um novo tipo de saída durante o registro de uma saída
     * (extensão do diagrama de casos de uso UC004 -> Manter Tipos de Saída).
     */
    public TipoSaida cadastrarTipoSaidaDuranteRegistro(TipoSaida tipoSaida) {
        exigirAdministradorOuRecepcionista();
        if (tipoSaidaController == null) {
            throw new ValidacaoException(
                    "Cadastro rápido de tipo de saída não está configurado neste controller.");
        }
        return tipoSaidaController.salvar(tipoSaida);
    }
}
