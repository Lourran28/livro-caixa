package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.util.Validacao;

/**
 * Parcela de uma fatura. O saldo é calculado: valor - soma dos pagamentos. Assim "desfazer baixa" é só
 * remover o pagamento, e a situação é sempre derivada dos pagamentos existentes (RF12/RF13).
 *
 * ATENÇÃO (DAO): para que saldo e situação fiquem corretos, carregue os pagamentos da parcela
 * com {@link #vincularPagamento(Pagamento)}. Após baixar/desfazer/cancelar, persista a nova
 * situação e os pagamentos criados/removidos numa mesma transação.
 */
public class Parcela {
    private Integer id;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private int numParcelas;
    private int posicaoParcela;
    private Fatura fatura;
    private TipoParcela tipoParcela;
    private SituacaoParcela situacaoParcela = SituacaoParcela.emAberto();
    private final List<Pagamento> pagamentos = new ArrayList<>();

    public Parcela() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public int getNumParcelas() { return numParcelas; }
    public void setNumParcelas(int numParcelas) { this.numParcelas = numParcelas; }
    public int getPosicaoParcela() { return posicaoParcela; }
    public void setPosicaoParcela(int posicaoParcela) { this.posicaoParcela = posicaoParcela; }
    public Fatura getFatura() { return fatura; }
    public void setFatura(Fatura fatura) { this.fatura = fatura; }
    public TipoParcela getTipoParcela() { return tipoParcela; }
    public void setTipoParcela(TipoParcela tipoParcela) { this.tipoParcela = tipoParcela; }
    public SituacaoParcela getSituacaoParcela() { return situacaoParcela; }
    public void setSituacaoParcela(SituacaoParcela situacaoParcela) { this.situacaoParcela = situacaoParcela; }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }

    /** Para o DAO: anexa um pagamento já existente no banco, sem mexer na situação. */
    public void vincularPagamento(Pagamento p) {
        p.setParcela(this);
        pagamentos.add(p);
    }

    public BigDecimal getValorPago() {
        return pagamentos.stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Quanto ainda falta receber. Parcela cancelada não tem saldo. */
    public BigDecimal getSaldo() {
        return isCancelada() ? BigDecimal.ZERO : valor.subtract(getValorPago());
    }

    public boolean isCancelada() { return situacaoParcela != null && situacaoParcela.isCancelada(); }
    public boolean isPaga() { return situacaoParcela != null && situacaoParcela.isPaga(); }

    /** RF09: vencida e ainda não quitada. Canceladas nunca contam como atrasadas. */
    public boolean isAtrasada(LocalDate referencia) {
        return !isCancelada() && !isPaga() && dataVencimento != null && dataVencimento.isBefore(referencia);
    }

    /** Altera o valor (RF06). Não pode ficar abaixo do que já foi pago. */
    public void alterarValor(BigDecimal novoValor) {
        if (isCancelada()) {
            throw new ValidacaoException("Parcela cancelada não pode ser alterada.");
        }
        Validacao.positivo(novoValor, "Valor da parcela");
        if (novoValor.compareTo(getValorPago()) < 0) {
            throw new ValidacaoException("O novo valor é menor que o total já pago (" + getValorPago() + ").");
        }
        this.valor = novoValor;
        atualizarSituacao();
    }

    /** Baixa total ou parcial (RF12). Retorna o Pagamento criado, que o DAO deve gravar. */
    public Pagamento baixar(BigDecimal valorRecebido, FormaPagamento forma, Funcionario func, LocalDate data) {
        if (isCancelada()) {
            throw new ValidacaoException("Parcela cancelada não pode receber baixas.");
        }
        if (isPaga()) {
            throw new ValidacaoException("A parcela já está quitada.");
        }
        Validacao.positivo(valorRecebido, "Valor recebido");
        if (valorRecebido.compareTo(getSaldo()) > 0) {
            throw new ValidacaoException("O valor informado é superior ao saldo da parcela (" + getSaldo() + ").");
        }
        Validacao.obrigatorio(forma, "Forma de pagamento");
        Validacao.obrigatorio(func, "Funcionário");
        Validacao.obrigatorio(data, "Data do pagamento");

        Pagamento p = new Pagamento();
        p.setValor(valorRecebido);
        p.setFormaPagamento(forma);
        p.setFuncionario(func);
        p.setData(data);
        vincularPagamento(p);
        atualizarSituacao();
        return p;
    }

    /** Desfaz uma baixa (RF13): remove o pagamento e recalcula a situação. O DAO deve excluí-lo do banco. */
    public void desfazerBaixa(Pagamento pagamento) {
        if (isCancelada()) {
            throw new ValidacaoException("Parcela cancelada não possui baixas ativas.");
        }
        if (!pagamentos.remove(pagamento)) {
            throw new ValidacaoException("O pagamento informado não pertence a esta parcela.");
        }
        atualizarSituacao();
    }

    /**
     * Cancela definitivamente (RF13). Os pagamentos existentes são estornados do fluxo de caixa;
     * retorna a lista deles para o DAO excluir.
     */
    public List<Pagamento> cancelar() {
        if (isCancelada()) {
            throw new ValidacaoException("A parcela já está cancelada.");
        }
        List<Pagamento> estornados = new ArrayList<>(pagamentos);
        pagamentos.clear();
        situacaoParcela = SituacaoParcela.cancelada();
        return estornados;
    }

    public void validar() {
        Validacao.positivo(valor, "Valor da parcela");
        Validacao.obrigatorio(dataVencimento, "Data de vencimento");
        Validacao.obrigatorio(tipoParcela, "Tipo de parcela");
        Validacao.obrigatorio(situacaoParcela, "Situação da parcela");
        if (posicaoParcela < 1 || numParcelas < posicaoParcela) {
            throw new ValidacaoException("Posição/quantidade de parcelas inválida.");
        }
    }

    private void atualizarSituacao() {
        BigDecimal pago = getValorPago();
        if (pago.signum() == 0) {
            situacaoParcela = SituacaoParcela.emAberto();
        } else if (valor.subtract(pago).signum() == 0) {
            situacaoParcela = SituacaoParcela.paga();
        } else {
            situacaoParcela = SituacaoParcela.parcial();
        }
    }
}
