package br.com.livrocaixa.util;

/** Valida formato e dígitos verificadores do CPF (RNF002). */
public final class CpfValidator {
    private CpfValidator() {}

    public static String somenteDigitos(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    /** Formata como 000.000.000-00 (cabe no VARCHAR(14) do banco). */
    public static String formatar(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11) {
            return cpf;
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    private static int digito(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int r = (soma * 10) % 11;
        return r == 10 ? 0 : r;
    }
}
