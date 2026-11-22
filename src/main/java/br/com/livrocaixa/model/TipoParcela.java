package br.com.livrocaixa.model;

/** Os ids seguem a carga inicial do script SQL (à vista=1, Parcela=2, Entrada=3). */
public class TipoParcela extends DominioSimples {
    public static final int ID_A_VISTA = 1;
    public static final int ID_PARCELA = 2;
    public static final int ID_ENTRADA = 3;

    public TipoParcela() {}
    public TipoParcela(Integer id, String descricao) { super(id, descricao); }

    public static TipoParcela aVista()  { return new TipoParcela(ID_A_VISTA, "à vista"); }
    public static TipoParcela parcela() { return new TipoParcela(ID_PARCELA, "Parcela"); }
    public static TipoParcela entrada() { return new TipoParcela(ID_ENTRADA, "Entrada"); }
}
