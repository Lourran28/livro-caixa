package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dto.MovimentacoesFuncionario;
import br.com.livrocaixa.dao.PagamentoDAO;
import br.com.livrocaixa.dao.ParcelaDAO;
import br.com.livrocaixa.dao.SaidaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Cliente;
import br.com.livrocaixa.model.FormaPagamento;
import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.model.Pagamento;
import br.com.livrocaixa.model.Parcela;
import br.com.livrocaixa.model.Saida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Controller responsável pelo acompanhamento de histórico e parcelamentos (UC005: RF08, RF09, RF10)
 * e pelas operações de baixa integral/parcial, estorno e cancelamento de parcelas (UC007: RF12, RF13).
 * <p>
 * Acesso <strong>exclusivo</strong> do perfil Administrador (bloqueio total para Recepcionista).
 */
public class ParcelamentoController extends ControllerBase {

    private final ParcelaDAO parcelaDAO;
    private final PagamentoDAO pagamentoDAO;
    private final SaidaDAO saidaDAO;

    public ParcelamentoController(
            ParcelaDAO parcelaDAO,
            PagamentoDAO pagamentoDAO,
            SaidaDAO saidaDAO) {
        this(parcelaDAO, pagamentoDAO, saidaDAO, SessaoUsuario.getInstancia());
    }

    public ParcelamentoController(
            ParcelaDAO parcelaDAO,
            PagamentoDAO pagamentoDAO,
            SaidaDAO saidaDAO,
            SessaoUsuario sessao) {
        super(sessao);
        this.parcelaDAO = Objects.requireNonNull(parcelaDAO, "ParcelaDAO não pode ser nulo.");
        this.pagamentoDAO = Objects.requireNonNull(pagamentoDAO, "PagamentoDAO não pode ser nulo.");
        this.saidaDAO = Objects.requireNonNull(saidaDAO, "SaidaDAO não pode ser nulo.");
    }

    /**
     * Lista todas as parcelas de um cliente (pagas, parciais, em aberto ou canceladas) (RF08).
     * Se a lista retornar vazia, a View deve informar que não há dados monetários vinculados.
     */
    public List<Parcela> listarParcelasDoCliente(Cliente cliente) {
        exigirAdministrador();
        if (cliente == null) {
            throw new ValidacaoException("Informe o cliente para consultar o histórico de parcelas.");
        }
        validarIdObrigatorio(cliente.getId(), "o cliente");
        return parcelaDAO.listarPorCliente(cliente.getId());
    }

    /**
     * Lista as parcelas atrasadas na data atual (RF09), excluindo parcelas pagas e canceladas.
     *
     * @param clienteOpcional cliente para filtro opcional, ou {@code null} para listar todos
     */
    public List<Parcela> listarParcelasAtrasadas(Cliente clienteOpcional) {
        return listarParcelasAtrasadas(clienteOpcional, LocalDate.now());
    }

    /**
     * Sobrecarga que permite informar explicitamente a data de referência para atraso
     * (útil para consultas retroativas e testes determinísticos).
     */
    public List<Parcela> listarParcelasAtrasadas(Cliente clienteOpcional, LocalDate dataReferencia) {
        exigirAdministrador();
        if (dataReferencia == null) {
            throw new ValidacaoException("A data de referência para verificação de atraso é obrigatória.");
        }
        Integer idCliente = null;
        if (clienteOpcional != null) {
            validarIdObrigatorio(clienteOpcional.getId(), "o cliente");
            idCliente = clienteOpcional.getId();
        }
        // Filtra defensivamente pelo model para garantir que canceladas/pagas nunca apareçam
        return parcelaDAO.listarAtrasadas(dataReferencia, idCliente).stream()
                .filter(parcela -> parcela.isAtrasada(dataReferencia))
                .toList();
    }

    /**
     * Consulta todas as entradas (pagamentos) e saídas registradas por um funcionário (RF10).
     */
    public MovimentacoesFuncionario listarMovimentacoesDoFuncionario(Funcionario funcionario) {
        exigirAdministrador();
        if (funcionario == null) {
            throw new ValidacaoException("Informe o funcionário para consultar suas movimentações.");
        }
        validarIdObrigatorio(funcionario.getId(), "o funcionário");

        List<Pagamento> entradas = pagamentoDAO.listarPorFuncionario(funcionario.getId());
        List<Saida> saidas = saidaDAO.listarPorFuncionario(funcionario.getId());

        BigDecimal totalEntradas = entradas.stream()
                .map(Pagamento::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = saidas.stream()
                .map(Saida::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MovimentacoesFuncionario(funcionario, entradas, saidas, totalEntradas, totalSaidas);
    }

    /**
     * Registra o recebimento integral ou parcial de uma parcela (UC007, RF12).
     * As validações de valor (≤ 0 ou > saldo) e situação (paga/cancelada) são delegadas ao model
     * {@link Parcela#baixar(BigDecimal, FormaPagamento, Funcionario, LocalDate)}.
     *
     * @param idParcela      identificador da parcela
     * @param valor          valor recebido
     * @param formaPagamento forma de pagamento utilizada (Espécie, PIX, RPV)
     * @param data           data do pagamento
     * @return pagamento gerado e persistido
     */
    public Pagamento baixarParcela(
            Integer idParcela,
            BigDecimal valor,
            FormaPagamento formaPagamento,
            LocalDate data) {
        Funcionario usuarioLogado = exigirAdministrador();
        validarIdObrigatorio(idParcela, "a parcela");

        Parcela parcela = carregarParcelaObrigatoria(idParcela);
        Pagamento pagamento = parcela.baixar(valor, formaPagamento, usuarioLogado, data);

        parcelaDAO.registrarBaixa(parcela, pagamento);
        return pagamento;
    }

    /**
     * Desfaz uma baixa específica realizada indevidamente em uma parcela (UC007 3b, RF13).
     * Restaura o saldo da parcela, reverte sua situação para Em Aberto ou Parcial e remove
     * o respectivo pagamento do fluxo de caixa em uma única transação.
     *
     * @param idParcela   identificador da parcela
     * @param idPagamento identificador do pagamento a estornar
     */
    public void desfazerBaixa(Integer idParcela, Integer idPagamento) {
        exigirAdministrador();
        validarIdObrigatorio(idParcela, "a parcela");
        validarIdObrigatorio(idPagamento, "o pagamento");

        Parcela parcela = carregarParcelaObrigatoria(idParcela);
        Pagamento pagamentoAlvo = parcela.getPagamentos().stream()
                .filter(p -> Objects.equals(p.getId(), idPagamento))
                .findFirst()
                .orElseThrow(() -> new ValidacaoException(
                        "Pagamento informado não pertence à parcela selecionada."));

        parcela.desfazerBaixa(pagamentoAlvo);
        parcelaDAO.desfazerBaixa(parcela, pagamentoAlvo);
    }

    /**
     * Cancela definitivamente uma parcela (UC007 3c, RF13), bloqueando futuras baixas
     * e estornando do fluxo de caixa eventuais pagamentos já registrados.
     *
     * @param idParcela identificador da parcela a cancelar
     * @return lista de pagamentos estornados (vazia caso a parcela não tivesse pagamentos)
     */
    public List<Pagamento> cancelarParcela(Integer idParcela) {
        exigirAdministrador();
        validarIdObrigatorio(idParcela, "a parcela");

        Parcela parcela = carregarParcelaObrigatoria(idParcela);
        List<Pagamento> pagamentosEstornados = parcela.cancelar();

        parcelaDAO.cancelar(parcela, pagamentosEstornados);
        return pagamentosEstornados;
    }

    private Parcela carregarParcelaObrigatoria(Integer idParcela) {
        return parcelaDAO.buscarPorId(idParcela)
                .orElseThrow(() -> new ValidacaoException(
                        "Parcela não encontrada para o identificador informado."));
    }
}
