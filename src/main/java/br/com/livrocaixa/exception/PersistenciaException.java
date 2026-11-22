package br.com.livrocaixa.exception;

/**
 * Exceção unchecked utilizada pela camada DAO para encapsular falhas de acesso
 * ao banco de dados, evitando vazar {@code java.sql.SQLException} para os controllers.
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final boolean violacaoIntegridade;

    public PersistenciaException(String mensagem) {
        this(mensagem, null, false);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        this(mensagem, causa, false);
    }

    public PersistenciaException(String mensagem, Throwable causa, boolean violacaoIntegridade) {
        super(mensagem, causa);
        this.violacaoIntegridade = violacaoIntegridade;
    }

    /**
     * Fábrica conveniente para sinalizar erro de integridade referencial
     * (por exemplo, tentativa de exclusão bloqueada por {@code ON DELETE RESTRICT}).
     */
    public static PersistenciaException violacaoIntegridade(String mensagem, Throwable causa) {
        return new PersistenciaException(mensagem, causa, true);
    }

    /**
     * Indica se a falha foi causada por violação de chave estrangeira ou
     * restrição de integridade no banco de dados.
     */
    public boolean isViolacaoIntegridade() {
        return violacaoIntegridade;
    }
}
