package br.com.livrocaixa.model;

/** Os ids seguem a carga inicial do script SQL (Em Aberto=1, Parcial=2, Paga=3, Cancelada=4). */
public class SituacaoParcela extends DominioSimples {
    public static final int ID_EM_ABERTO = 1;
    public static final int ID_PARCIAL = 2;
    public static final int ID_PAGA = 3;
    public static final int ID_CANCELADA = 4;

    public SituacaoParcela() {}
    public SituacaoParcela(Integer id, String descricao) { super(id, descricao); }

    public static SituacaoParcela emAberto()  { return new SituacaoParcela(ID_EM_ABERTO, "Em Aberto"); }
    public static SituacaoParcela parcial()   { return new SituacaoParcela(ID_PARCIAL, "Parcial"); }
    public static SituacaoParcela paga()      { return new SituacaoParcela(ID_PAGA, "Paga"); }
    public static SituacaoParcela cancelada() { return new SituacaoParcela(ID_CANCELADA, "Cancelada"); }

    public boolean isEmAberto()  { return Integer.valueOf(ID_EM_ABERTO).equals(getId()); }
    public boolean isParcial()   { return Integer.valueOf(ID_PARCIAL).equals(getId()); }
    public boolean isPaga()      { return Integer.valueOf(ID_PAGA).equals(getId()); }
    public boolean isCancelada() { return Integer.valueOf(ID_CANCELADA).equals(getId()); }
}
