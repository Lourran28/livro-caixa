package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Recebimento (total ou parcial) de uma parcela. É a "entrada" contabilizada no livro caixa. */
public class Pagamento {
    private Integer id;
    private BigDecimal valor;
    private LocalDate data;
    private Parcela parcela;
    private FormaPagamento formaPagamento;
    private Funcionario funcionario;

    public Pagamento() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Parcela getParcela() { return parcela; }
    public void setParcela(Parcela parcela) { this.parcela = parcela; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pagamento)) return false;
        Pagamento outro = (Pagamento) o;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
