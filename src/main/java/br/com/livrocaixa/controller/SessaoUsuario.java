package br.com.livrocaixa.controller;

import br.com.livrocaixa.model.Funcionario;

import java.util.Objects;

/**
 * Mantém o estado de autenticação do usuário logado na aplicação desktop (RF01, RNF001).
 * Implementada como singleton simples, com possibilidade de limpeza da sessão ao fazer logout.
 */
public final class SessaoUsuario {

    private static final SessaoUsuario INSTANCIA = new SessaoUsuario();

    private Funcionario usuarioLogado;

    private SessaoUsuario() {
    }

    /**
     * Retorna a instância única da sessão da aplicação.
     */
    public static SessaoUsuario getInstancia() {
        return INSTANCIA;
    }

    /**
     * Registra o funcionário autenticado na sessão atual.
     */
    public synchronized void iniciar(Funcionario funcionario) {
        this.usuarioLogado = Objects.requireNonNull(funcionario, "Funcionário não pode ser nulo ao iniciar sessão.");
    }

    /**
     * Retorna o funcionário atualmente autenticado, ou {@code null} se não houver sessão ativa.
     */
    public synchronized Funcionario getUsuarioLogado() {
        return usuarioLogado;
    }

    /**
     * Indica se existe um usuário autenticado na sessão atual.
     */
    public synchronized boolean isLogado() {
        return usuarioLogado != null;
    }

    /**
     * Encerra a sessão atual, removendo o usuário autenticado.
     */
    public synchronized void encerrar() {
        this.usuarioLogado = null;
    }
}
