package br.com.livrocaixa.model;

import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.util.CpfValidator;
import br.com.livrocaixa.util.Validacao;

/** Cliente pessoa física (CPF único, validado conforme RNF002). */
public class Cliente {
    private Integer id;
    private String nome;
    private String cpf;

    public Cliente() {}
    public Cliente(Integer id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        setCpf(cpf);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    /** Guarda o CPF já formatado (000.000.000-00). */
    public void setCpf(String cpf) { this.cpf = CpfValidator.formatar(cpf); }

    public boolean validarCpf() { return CpfValidator.isValido(cpf); }

    /** A unicidade do CPF é verificada pelo DAO/banco (UNIQUE). */
    public void validar() {
        Validacao.texto(nome, "Nome", 45, true);
        Validacao.texto(cpf, "CPF", 14, true);
        if (!validarCpf()) {
            throw new ValidacaoException("CPF inválido.");
        }
    }

    @Override
    public String toString() { return nome; }
}
