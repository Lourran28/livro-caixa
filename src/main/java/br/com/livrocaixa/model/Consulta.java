package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.livrocaixa.util.Validacao;

/** Atendimento a um cliente. Cada consulta gera exatamente uma Fatura (composição 1:1). */
public class Consulta {
    private Integer id;
    private String observacao;
    private LocalDate data;
    private Cliente cliente;
    private TipoConsulta tipoConsulta;
    private Funcionario funcionario;
    private Fatura fatura;

    public Consulta() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public TipoConsulta getTipoConsulta() { return tipoConsulta; }
    public void setTipoConsulta(TipoConsulta tipoConsulta) { this.tipoConsulta = tipoConsulta; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public Fatura getFatura() { return fatura; }
    public void setFatura(Fatura fatura) { this.fatura = fatura; }

    /** Cria a fatura desta consulta (data inicial = data da consulta). Depois use as geradoras de parcelas da Fatura. */
    public Fatura gerarFatura(BigDecimal valorTotal) {
        Fatura f = new Fatura();
        f.setValorTotal(valorTotal);
        f.setDataInicial(data);
        f.setConsulta(this);
        this.fatura = f;
        return f;
    }

    public void validar() {
        Validacao.obrigatorio(data, "Data");
        Validacao.obrigatorio(cliente, "Cliente");
        Validacao.obrigatorio(tipoConsulta, "Tipo de consulta");
        Validacao.obrigatorio(funcionario, "Funcionário");
        Validacao.texto(observacao, "Observação", 45, false);
        if (fatura != null) {
            fatura.validar();
        }
    }
}
