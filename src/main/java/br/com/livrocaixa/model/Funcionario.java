package br.com.livrocaixa.model;

import br.com.livrocaixa.util.Validacao;

public class Funcionario {
    private Integer id;
    private String nome;
    private String senha;
    private Perfil perfil;

    public Funcionario() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public boolean isAdministrador() { return perfil != null && perfil.isAdministrador(); }

    /** Comparação simples da senha. Se passar a guardar hash, só este método muda. */
    public boolean senhaConfere(String senhaInformada) {
        return senha != null && senha.equals(senhaInformada);
    }

    public void validar() {
        Validacao.texto(nome, "Nome", 45, true);
        Validacao.texto(senha, "Senha", 45, true);
        Validacao.obrigatorio(perfil, "Perfil");
    }

    @Override
    public String toString() { return nome; }
}
