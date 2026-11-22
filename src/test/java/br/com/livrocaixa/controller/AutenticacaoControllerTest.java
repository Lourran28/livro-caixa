package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dubles.DadosTesteFactory;
import br.com.livrocaixa.controller.dubles.MemoriaFuncionarioDAO;
import br.com.livrocaixa.exception.AcessoNegadoException;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Funcionario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacaoControllerTest {

    private MemoriaFuncionarioDAO funcionarioDAO;
    private AutenticacaoController controller;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().encerrar();
        funcionarioDAO = new MemoriaFuncionarioDAO();
        funcionarioDAO.inserir(DadosTesteFactory.admin());
        funcionarioDAO.inserir(DadosTesteFactory.recepcionista());
        controller = new AutenticacaoController(funcionarioDAO);
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidasEPreencherSessao() {
        Funcionario logado = controller.login("Dra. Josilene", "admin123");

        assertNotNull(logado);
        assertTrue(logado.isAdministrador());
        assertTrue(SessaoUsuario.getInstancia().isLogado());
        assertEquals(logado, controller.getUsuarioLogado());
    }

    @Test
    void deveFalharComMensagemGenericaQuandoSenhaIncorretaOuUsuarioInexistente() {
        ValidacaoException erroSenha = assertThrows(
                ValidacaoException.class,
                () -> controller.login("Dra. Josilene", "senhaErrada"));

        ValidacaoException erroUsuario = assertThrows(
                ValidacaoException.class,
                () -> controller.login("UsuarioInexistente", "admin123"));

        assertEquals("Usuário ou senha inválidos.", erroSenha.getMessage());
        assertEquals(erroSenha.getMessage(), erroUsuario.getMessage());
        assertFalse(SessaoUsuario.getInstancia().isLogado());
    }

    @Test
    void deveEncerrarSessaoAoFazerLogout() {
        controller.login("Dra. Josilene", "admin123");
        controller.logout();

        assertFalse(SessaoUsuario.getInstancia().isLogado());
        assertThrows(AcessoNegadoException.class, () -> controller.getUsuarioLogado());
    }
}
