package br.com.livrocaixa.exception;

/** Violação de regra de negócio/validação. A mensagem é própria para exibir ao usuário. */
public class ValidacaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
