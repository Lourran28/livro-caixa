package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dto.RelatorioFluxoCaixa;
import br.com.livrocaixa.controller.dubles.*;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Pagamento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioControllerTest {

    private MemoriaPagamentoDAO pagamentoDAO;
    private MemoriaSaidaDAO saidaDAO;
    private ExportadorRelatorioFake exportadorFake;
    private RelatorioController relatorioController;
    private SaidaController saidaController;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().iniciar(DadosTesteFactory.admin());
        pagamentoDAO = new MemoriaPagamentoDAO();
        saidaDAO = new MemoriaSaidaDAO();
        exportadorFake = new ExportadorRelatorioFake();
        relatorioController = new RelatorioController(pagamentoDAO, saidaDAO, exportadorFake);
        saidaController = new SaidaController(saidaDAO);
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void deveRetornarRelatorioZeradoQuandoPeriodoNaoPossuiMovimentacoes() {
        RelatorioFluxoCaixa relatorio = relatorioController.gerarFluxoCaixa(2025, 1, null);

        assertTrue(relatorio.isSemMovimentacoes());
        assertEquals(0, BigDecimal.ZERO.compareTo(relatorio.getTotalEntradas()));
        assertEquals(0, BigDecimal.ZERO.compareTo(relatorio.getTotalSaidas()));
        assertEquals(0, BigDecimal.ZERO.compareTo(relatorio.getSaldo()));
        assertEquals(LocalDate.of(2025, 1, 1), relatorio.getDataInicio());
        assertEquals(LocalDate.of(2025, 1, 31), relatorio.getDataFim());
    }

    @Test
    void deveConsolidarEntradasSaidasESaldoPorMesAnoEDataEspecifica() {
        Pagamento p1 = new Pagamento();
        p1.setData(LocalDate.of(2026, 11, 10));
        p1.setValor(new BigDecimal("500.00"));
        p1.setFormaPagamento(DadosTesteFactory.formaPix());
        p1.setFuncionario(DadosTesteFactory.admin());
        pagamentoDAO.salvar(p1);

        Pagamento p2 = new Pagamento();
        p2.setData(LocalDate.of(2026, 11, 15));
        p2.setValor(new BigDecimal("300.00"));
        p2.setFormaPagamento(DadosTesteFactory.formaEspecie());
        p2.setFuncionario(DadosTesteFactory.admin());
        pagamentoDAO.salvar(p2);

        saidaController.registrarSaida(
                LocalDate.of(2026, 11, 10), new BigDecimal("120.50"), DadosTesteFactory.tipoSaidaPadrao(), "Cartório");

        // Por mês
        RelatorioFluxoCaixa mensal = relatorioController.gerarFluxoCaixa(2026, 11, null);
        assertEquals(0, new BigDecimal("800.00").compareTo(mensal.getTotalEntradas()));
        assertEquals(0, new BigDecimal("120.50").compareTo(mensal.getTotalSaidas()));
        assertEquals(0, new BigDecimal("679.50").compareTo(mensal.getSaldo()));

        // Por data específica
        RelatorioFluxoCaixa diario = relatorioController.gerarFluxoCaixa(null, null, LocalDate.of(2026, 11, 10));
        assertEquals(0, new BigDecimal("500.00").compareTo(diario.getTotalEntradas()));
        assertEquals(0, new BigDecimal("120.50").compareTo(diario.getTotalSaidas()));
        assertEquals(0, new BigDecimal("379.50").compareTo(diario.getSaldo()));

        // Exportação PDF via interface
        Path destino = Path.of("relatorio-nov-2026.pdf");
        relatorioController.exportarPdf(mensal, destino);
        assertEquals(1, exportadorFake.getTotalChamadas());
        assertEquals(destino, exportadorFake.getUltimoDestino());
    }

    @Test
    void deveRejeitarFiltrosDePeriodoInvalidos() {
        assertThrows(ValidacaoException.class, () -> relatorioController.gerarFluxoCaixa(null, null, null));
        assertThrows(ValidacaoException.class, () -> relatorioController.gerarFluxoCaixa(null, 11, null));
        assertThrows(ValidacaoException.class, () -> relatorioController.gerarFluxoCaixa(2026, 13, null));
        assertThrows(ValidacaoException.class, () -> relatorioController.gerarFluxoCaixa(
                2026, 11, LocalDate.of(2026, 11, 10)));
    }
}
