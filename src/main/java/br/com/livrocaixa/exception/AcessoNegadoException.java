package br.com.livrocaixa.exception;

/**
 * Exceção lançada quando um usuário tenta executar uma operação sem estar
 * autenticado ou sem possuir permissão compatível com o seu perfil (RNF001).
 */
public class AcessoNegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
