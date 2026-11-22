package br.com.livrocaixa.controller.dto;

import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.model.Pagamento;
import br.com.livrocaixa.model.Saida;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DTO imutável que agrupa as entradas (pagamentos) e saídas registradas por
 * um determinado funcionário (UC005, RF10).
 */
public final class MovimentacoesFuncionario {

    private final Funcionario funcionario;
    private final List<Pagamento> entradas;
    private final List<Saida> saidas;
    private final BigDecimal totalEntradas;
    private final BigDecimal totalSaidas;

    public MovimentacoesFuncionario(
            Funcionario funcionario,
            List<Pagamento> entradas,
            List<Saida> saidas,
            BigDecimal totalEntradas,
            BigDecimal totalSaidas) {
        this.funcionario = Objects.requireNonNull(funcionario, "funcionario não pode ser nulo");
        this.entradas = entradas == null ? List.of() : Collections.unmodifiableList(List.copyOf(entradas));
        this.saidas = saidas == null ? List.of() : Collections.unmodifiableList(List.copyOf(saidas));
        this.totalEntradas = Objects.requireNonNull(totalEntradas, "totalEntradas não pode ser nulo");
        this.totalSaidas = Objects.requireNonNull(totalSaidas, "totalSaidas não pode ser nulo");
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public List<Pagamento> getEntradas() {
        return entradas;
    }

    public List<Saida> getSaidas() {
        return saidas;
    }

    public BigDecimal getTotalEntradas() {
        return totalEntradas;
    }

    public BigDecimal getTotalSaidas() {
        return totalSaidas;
    }

    public boolean isSemMovimentacoes() {
        return entradas.isEmpty() && saidas.isEmpty();
    }
}
