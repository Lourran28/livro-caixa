package br.com.livrocaixa.controller.dto;

import br.com.livrocaixa.model.Pagamento;
import br.com.livrocaixa.model.Saida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DTO imutável contendo os dados consolidados do relatório de fluxo de caixa
 * para um período específico (UC006, RF11).
 */
public final class RelatorioFluxoCaixa {

    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final String descricaoPeriodo;
    private final BigDecimal totalEntradas;
    private final BigDecimal totalSaidas;
    private final BigDecimal saldo;
    private final List<Pagamento> entradas;
    private final List<Saida> saidas;

    public RelatorioFluxoCaixa(
            LocalDate dataInicio,
            LocalDate dataFim,
            String descricaoPeriodo,
            BigDecimal totalEntradas,
            BigDecimal totalSaidas,
            BigDecimal saldo,
            List<Pagamento> entradas,
            List<Saida> saidas) {
        this.dataInicio = Objects.requireNonNull(dataInicio, "dataInicio não pode ser nula");
        this.dataFim = Objects.requireNonNull(dataFim, "dataFim não pode ser nula");
        this.descricaoPeriodo = Objects.requireNonNull(descricaoPeriodo, "descricaoPeriodo não pode ser nula");
        this.totalEntradas = Objects.requireNonNull(totalEntradas, "totalEntradas não pode ser nulo");
        this.totalSaidas = Objects.requireNonNull(totalSaidas, "totalSaidas não pode ser nulo");
        this.saldo = Objects.requireNonNull(saldo, "saldo não pode ser nulo");
        this.entradas = entradas == null ? List.of() : Collections.unmodifiableList(List.copyOf(entradas));
        this.saidas = saidas == null ? List.of() : Collections.unmodifiableList(List.copyOf(saidas));
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public String getDescricaoPeriodo() {
        return descricaoPeriodo;
    }

    public BigDecimal getTotalEntradas() {
        return totalEntradas;
    }

    public BigDecimal getTotalSaidas() {
        return totalSaidas;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public List<Pagamento> getEntradas() {
        return entradas;
    }

    public List<Saida> getSaidas() {
        return saidas;
    }

    /**
     * Indica se o período consultado não possui nenhuma entrada nem saída registrada.
     */
    public boolean isSemMovimentacoes() {
        return entradas.isEmpty() && saidas.isEmpty();
    }
}
