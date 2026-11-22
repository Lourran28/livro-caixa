package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dubles.*;
import br.com.livrocaixa.exception.AcessoNegadoException;
import br.com.livrocaixa.model.Saida;
import br.com.livrocaixa.model.TipoSaida;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Garante o cumprimento estrito do RNF001:
 * - Usuário não logado é bloqueado em todas as operações.
 * - Recepcionista é bloqueada em tudo exceto registro de saídas e manutenção de tipos de saída.
 */
class PermissoesAcessoTest {

    private PerfilController perfilController;
    private FormaPagamentoController formaPagamentoController;
    private TipoConsultaController tipoConsultaController;
    private TipoSaidaController tipoSaidaController;
    private FuncionarioController funcionarioController;
    private ClienteController clienteController;
    private ConsultaController consultaController;
    private SaidaController saidaController;
    private ParcelamentoController parcelamentoController;
    private RelatorioController relatorioController;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().encerrar();

        MemoriaPagamentoDAO pagamentoDAO = new MemoriaPagamentoDAO();
        MemoriaParcelaDAO parcelaDAO = new MemoriaParcelaDAO(pagamentoDAO);
        MemoriaSaidaDAO saidaDAO = new MemoriaSaidaDAO();
        MemoriaClienteDAO clienteDAO = new MemoriaClienteDAO();
        MemoriaTipoConsultaDAO tipoConsultaDAO = new MemoriaTipoConsultaDAO();
        MemoriaTipoSaidaDAO tipoSaidaDAO = new MemoriaTipoSaidaDAO();

        perfilController = new PerfilController(new MemoriaPerfilDAO());
        formaPagamentoController = new FormaPagamentoController(new MemoriaFormaPagamentoDAO());
        tipoConsultaController = new TipoConsultaController(tipoConsultaDAO);
        tipoSaidaController = new TipoSaidaController(tipoSaidaDAO);
        funcionarioController = new FuncionarioController(new MemoriaFuncionarioDAO());
        clienteController = new ClienteController(clienteDAO);
        consultaController = new ConsultaController(
                new MemoriaConsultaDAO(parcelaDAO), clienteController, tipoConsultaController);
        saidaController = new SaidaController(saidaDAO, tipoSaidaController);
        parcelamentoController = new ParcelamentoController(parcelaDAO, pagamentoDAO, saidaDAO);
        relatorioController = new RelatorioController(pagamentoDAO, saidaDAO, new ExportadorRelatorioFake());
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void deveBloquearOperacoesSemUsuarioLogado() {
        assertThrows(AcessoNegadoException.class, () -> perfilController.listar());
        assertThrows(AcessoNegadoException.class, () -> tipoSaidaController.listar());
        assertThrows(AcessoNegadoException.class, () -> saidaController.registrarSaida(
                LocalDate.now(), new BigDecimal("50.00"), DadosTesteFactory.tipoSaidaPadrao(), "Água"));
        assertThrows(AcessoNegadoException.class, () -> relatorioController.gerarFluxoCaixa(2026, null, null));
    }

    @Test
    void deveBloquearRecepcionistaEmTudoExcetoSaidaETipoSaida() {
        SessaoUsuario.getInstancia().iniciar(DadosTesteFactory.recepcionista());

        // Bloqueios obrigatórios (RNF001)
        assertThrows(AcessoNegadoException.class, () -> perfilController.listar());
        assertThrows(AcessoNegadoException.class, () -> formaPagamentoController.listar());
        assertThrows(AcessoNegadoException.class, () -> tipoConsultaController.listar());
        assertThrows(AcessoNegadoException.class, () -> funcionarioController.listar());
        assertThrows(AcessoNegadoException.class, () -> clienteController.listar());
        assertThrows(AcessoNegadoException.class, () -> consultaController.buscarPorId(1));
        assertThrows(AcessoNegadoException.class, () -> parcelamentoController.listarParcelasAtrasadas(null));
        assertThrows(AcessoNegadoException.class, () -> parcelamentoController.baixarParcela(
                1, new BigDecimal("100.00"), DadosTesteFactory.formaPix(), LocalDate.now()));
        assertThrows(AcessoNegadoException.class, () -> parcelamentoController.desfazerBaixa(1, 1));
        assertThrows(AcessoNegadoException.class, () -> parcelamentoController.cancelarParcela(1));
        assertThrows(AcessoNegadoException.class, () -> relatorioController.gerarFluxoCaixa(2026, 11, null));

        // Operações permitidas para Recepcionista
        TipoSaida tipo = new TipoSaida();
        tipo.setDescricao("Café e Copa");
        TipoSaida salvo = assertDoesNotThrow(() -> tipoSaidaController.salvar(tipo));
        assertNotNull(salvo.getId());

        Saida saida = assertDoesNotThrow(() -> saidaController.registrarSaida(
                LocalDate.of(2026, 11, 20), new BigDecimal("45.90"), salvo, "Compra de café"));
        assertNotNull(saida.getId());
        assertEquals(DadosTesteFactory.recepcionista().getId(), saida.getFuncionario().getId());
    }
}
