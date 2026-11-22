package br.com.livrocaixa.model;

import java.util.Objects;

import br.com.livrocaixa.util.Validacao;

/** Base das tabelas de domínio (id + descricao VARCHAR(45)): Perfil, TipoSaida, TipoConsulta etc. */
public abstract class DominioSimples {
    private Integer id;
    private String descricao;

    protected DominioSimples() {}

    protected DominioSimples(Integer id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public void validar() {
        Validacao.texto(descricao, "Descrição", 45, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DominioSimples outro = (DominioSimples) o;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return descricao; } // útil em combos/listas das Views
}
