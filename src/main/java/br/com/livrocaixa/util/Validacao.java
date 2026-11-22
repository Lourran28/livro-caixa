package br.com.livrocaixa.util;

import java.math.BigDecimal;

import br.com.livrocaixa.exception.ValidacaoException;

/** Validações reutilizadas pelos models. */
public final class Validacao {
    private Validacao() {}

    public static void obrigatorio(Object valor, String campo) {
        if (valor == null) {
            throw new ValidacaoException("O campo \"" + campo + "\" é obrigatório.");
        }
    }

    public static void texto(String valor, String campo, int max, boolean obrigatorio) {
        boolean vazio = valor == null || valor.isBlank();
        if (vazio && obrigatorio) {
            throw new ValidacaoException("O campo \"" + campo + "\" é obrigatório.");
        }
        if (!vazio && valor.length() > max) {
            throw new ValidacaoException("O campo \"" + campo + "\" aceita no máximo " + max + " caracteres.");
        }
    }

    public static void positivo(BigDecimal valor, String campo) {
        obrigatorio(valor, campo);
        if (valor.signum() <= 0) {
            throw new ValidacaoException("O campo \"" + campo + "\" deve ser maior que zero.");
        }
    }
}
