package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.util.Validacao;

/** Fatura de uma consulta, dividida em parcelas (RF06/RF07). */
public class Fatura {
    private Integer id;
    private BigDecimal valorTotal;
    private LocalDate dataInicial;
    private Consulta consulta;
    private final List<Parcela> parcelas = new ArrayList<>();

    public Fatura() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public LocalDate getDataInicial() { return dataInicial; }
    public void setDataInicial(LocalDate dataInicial) { this.dataInicial = dataInicial; }
    public Consulta getConsulta() { return consulta; }
    public void setConsulta(Consulta consulta) { this.consulta = consulta; }
    public List<Parcela> getParcelas() { return Collections.unmodifiableList(parcelas); }

    /** Usado pelo DAO ao carregar parcelas do banco (não recalcula nada). */
    public void adicionarParcela(Parcela p) {
        p.setFatura(this);
        parcelas.add(p);
    }

    /** Pagamento à vista: uma única parcela com o valor total, vencendo na data inicial. */
    public List<Parcela> gerarParcelasAVista() {
        exigirBase();
        parcelas.clear();
        adicionarParcela(novaParcela(valorTotal, dataInicial, 1, 1, TipoParcela.aVista()));
        return getParcelas();
    }

    /**
     * Pagamento a prazo. Divide (valorTotal - entrada) em {@code quantidade} parcelas mensais a partir de
     * {@code primeiroVencimento}; a última absorve a diferença de arredondamento. Se houver entrada, ela vira a
     * primeira parcela (tipo Entrada, vence na data inicial). Os valores podem ser ajustados depois
     * com {@link Parcela#alterarValor(BigDecimal)}, conferindo com {@link #validar()}.
     */
    public List<Parcela> gerarParcelasAPrazo(int quantidade, LocalDate primeiroVencimento, BigDecimal valorEntrada) {
        exigirBase();
        Validacao.obrigatorio(primeiroVencimento, "Primeiro vencimento");
        if (quantidade < 1) {
            throw new ValidacaoException("A quantidade de parcelas deve ser de pelo menos 1.");
        }
        BigDecimal entrada = valorEntrada == null ? BigDecimal.ZERO : valorEntrada;
        if (entrada.signum() < 0 || entrada.compareTo(valorTotal) >= 0) {
            throw new ValidacaoException("A entrada deve ser menor que o valor total da fatura.");
        }
        boolean temEntrada = entrada.signum() > 0;
        BigDecimal restante = valorTotal.subtract(entrada);
        int total = quantidade + (temEntrada ? 1 : 0);
        int posicao = 1;

        parcelas.clear();
        if (temEntrada) {
            adicionarParcela(novaParcela(entrada, dataInicial, total, posicao++, TipoParcela.entrada()));
        }
        BigDecimal base = restante.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.DOWN);
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 0; i < quantidade; i++) {
            BigDecimal valor = (i == quantidade - 1) ? restante.subtract(acumulado) : base;
            acumulado = acumulado.add(valor);
            adicionarParcela(novaParcela(valor, primeiroVencimento.plusMonths(i), total, posicao++, TipoParcela.parcela()));
        }
        return getParcelas();
    }

    public BigDecimal getSomaParcelas() {
        return parcelas.stream().map(Parcela::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** A soma das parcelas (inclusive editadas manualmente) deve fechar com o valor total. */
    public void validar() {
        Validacao.positivo(valorTotal, "Valor total");
        Validacao.obrigatorio(dataInicial, "Data inicial");
        if (parcelas.isEmpty()) {
            throw new ValidacaoException("A fatura precisa ter ao menos uma parcela.");
        }
        if (getSomaParcelas().compareTo(valorTotal) != 0) {
            throw new ValidacaoException("A soma das parcelas (" + getSomaParcelas()
                    + ") difere do valor total da fatura (" + valorTotal + ").");
        }
    }

    private void exigirBase() {
        Validacao.positivo(valorTotal, "Valor total");
        Validacao.obrigatorio(dataInicial, "Data inicial");
    }

    private Parcela novaParcela(BigDecimal valor, LocalDate venc, int total, int pos, TipoParcela tipo) {
        Parcela p = new Parcela();
        p.setValor(valor);
        p.setDataVencimento(venc);
        p.setNumParcelas(total);
        p.setPosicaoParcela(pos);
        p.setTipoParcela(tipo);
        p.setSituacaoParcela(SituacaoParcela.emAberto());
        return p;
    }
}
