package br.com.livrocaixa.model;

public class Perfil extends DominioSimples {
    public static final String ADMINISTRADOR = "Administrador";
    public static final String RECEPCIONISTA = "Recepcionista";

    public Perfil() {}
    public Perfil(Integer id, String descricao) { super(id, descricao); }

    public boolean isAdministrador() { return ADMINISTRADOR.equalsIgnoreCase(getDescricao()); }
    public boolean isRecepcionista() { return RECEPCIONISTA.equalsIgnoreCase(getDescricao()); }
}
