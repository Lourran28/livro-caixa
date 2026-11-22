package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.livrocaixa.util.Validacao;

/** Gasto do escritório; entra negativamente no livro caixa. */
public class Saida {
    private Integer id;
    private String observacao;
    private BigDecimal valor;
    private LocalDate data;
    private Funcionario funcionario;
    private TipoSaida tipoSaida;

    public Saida() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public TipoSaida getTipoSaida() { return tipoSaida; }
    public void setTipoSaida(TipoSaida tipoSaida) { this.tipoSaida = tipoSaida; }

    /** UC004: data, valor (> 0) e tipo de saída são obrigatórios. */
    public void validar() {
        Validacao.obrigatorio(data, "Data");
        Validacao.positivo(valor, "Valor");
        Validacao.obrigatorio(tipoSaida, "Tipo de saída");
        Validacao.obrigatorio(funcionario, "Funcionário");
        Validacao.texto(observacao, "Observação", 45, false);
    }
}
