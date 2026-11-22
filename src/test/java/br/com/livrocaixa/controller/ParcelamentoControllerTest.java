package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dto.MovimentacoesFuncionario;
import br.com.livrocaixa.controller.dubles.*;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParcelamentoControllerTest {

    private static final String CPF_VALIDO = "52998224725";

    private MemoriaPagamentoDAO pagamentoDAO;
    private MemoriaParcelaDAO parcelaDAO;
    private MemoriaSaidaDAO saidaDAO;
    private ConsultaController consultaController;
    private ParcelamentoController parcelamentoController;
    private SaidaController saidaController;

    private Cliente cliente;
    private TipoConsulta tipoConsulta;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().iniciar(DadosTesteFactory.admin());

        pagamentoDAO = new MemoriaPagamentoDAO();
        parcelaDAO = new MemoriaParcelaDAO(pagamentoDAO);
        saidaDAO = new MemoriaSaidaDAO();

        ClienteController clienteController = new ClienteController(new MemoriaClienteDAO());
        TipoConsultaController tipoConsultaController = new TipoConsultaController(new MemoriaTipoConsultaDAO());

        consultaController = new ConsultaController(
                new MemoriaConsultaDAO(parcelaDAO), clienteController, tipoConsultaController);
        parcelamentoController = new ParcelamentoController(parcelaDAO, pagamentoDAO, saidaDAO);
        saidaController = new SaidaController(saidaDAO);

        cliente = clienteController.salvar(DadosTesteFactory.clienteValido(null, "Roberto Silva", CPF_VALIDO));
        tipoConsulta = tipoConsultaController.salvar(DadosTesteFactory.tipoConsultaPadrao());
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void deveExecutarCicloCompletoBaixaParcialBaixaTotalDesfazerECancelar() {
        Consulta consulta = consultaController.prepararConsulta(
                cliente,
                tipoConsulta,
                LocalDate.of(2026, 10, 1),
                "Honorários",
                new BigDecimal("600.00"),
                false,
                2,
                LocalDate.of(2026, 10, 15),
                null);
        consultaController.salvarConsulta(consulta);

        List<Parcela> parcelas = parcelamentoController.listarParcelasDoCliente(cliente);
        assertEquals(2, parcelas.size());
        Parcela p1 = parcelas.get(0); // valor 300,00, vencimento 15/10/2026
        Parcela p2 = parcelas.get(1); // valor 300,00

        // 1. Baixa parcial de 100,00 -> situação Parcial, saldo 200,00
        Pagamento pag1 = parcelamentoController.baixarParcela(
                p1.getId(), new BigDecimal("100.00"), DadosTesteFactory.formaPix(), LocalDate.of(2026, 10, 16));
        assertNotNull(pag1.getId());
        assertTrue(p1.getSituacaoParcela().isParcial());
        assertEquals(0, new BigDecimal("200.00").compareTo(p1.getSaldo()));

        // Valor acima do saldo restante -> erro do model propagado
        assertThrows(ValidacaoException.class, () -> parcelamentoController.baixarParcela(
                p1.getId(), new BigDecimal("250.00"), DadosTesteFactory.formaPix(), LocalDate.of(2026, 10, 17)));

        // 2. Baixa total do restante (200,00) -> situação Paga, saldo 0
        Pagamento pag2 = parcelamentoController.baixarParcela(
                p1.getId(), new BigDecimal("200.00"), DadosTesteFactory.formaEspecie(), LocalDate.of(2026, 10, 18));
        assertTrue(p1.getSituacaoParcela().isPaga());
        assertEquals(0, BigDecimal.ZERO.compareTo(p1.getSaldo()));
        assertEquals(2, pagamentoDAO.listarTodos().size());

        // 3. Desfazer o segundo pagamento -> volta para Parcial com saldo 200,00 e remove entrada do caixa
        parcelamentoController.desfazerBaixa(p1.getId(), pag2.getId());
        assertTrue(p1.getSituacaoParcela().isParcial());
        assertEquals(0, new BigDecimal("200.00").compareTo(p1.getSaldo()));
        assertEquals(1, pagamentoDAO.listarTodos().size());

        // 4. Cancelar a parcela p1 -> marca Cancelada e estorna o pagamento restante do caixa
        List<Pagamento> estornados = parcelamentoController.cancelarParcela(p1.getId());
        assertEquals(1, estornados.size());
        assertTrue(p1.getSituacaoParcela().isCancelada());
        assertTrue(pagamentoDAO.listarTodos().isEmpty());

        // Parcela cancelada não pode receber baixa nem aparecer em atrasadas
        assertThrows(ValidacaoException.class, () -> parcelamentoController.baixarParcela(
                p1.getId(), new BigDecimal("50.00"), DadosTesteFactory.formaPix(), LocalDate.of(2026, 10, 20)));

        List<Parcela> atrasadas = parcelamentoController.listarParcelasAtrasadas(
                cliente, LocalDate.of(2026, 12, 31));
        assertEquals(1, atrasadas.size());
        assertEquals(p2.getId(), atrasadas.get(0).getId());
    }

    @Test
    void deveListarMovimentacoesPorFuncionario() {
        Consulta consulta = consultaController.prepararConsulta(
                cliente,
                tipoConsulta,
                LocalDate.of(2026, 11, 5),
                "Consulta avulsa",
                new BigDecimal("250.00"),
                true,
                null,
                null,
                null);
        consultaController.salvarConsulta(consulta);
        Parcela unica = parcelamentoController.listarParcelasDoCliente(cliente).get(0);

        parcelamentoController.baixarParcela(
                unica.getId(), new BigDecimal("250.00"), DadosTesteFactory.formaPix(), LocalDate.of(2026, 11, 5));
        saidaController.registrarSaida(
                LocalDate.of(2026, 11, 6), new BigDecimal("70.00"), DadosTesteFactory.tipoSaidaPadrao(), "Papel A4");

        MovimentacoesFuncionario mov = parcelamentoController.listarMovimentacoesDoFuncionario(
                DadosTesteFactory.admin());

        assertEquals(1, mov.getEntradas().size());
        assertEquals(1, mov.getSaidas().size());
        assertEquals(0, new BigDecimal("250.00").compareTo(mov.getTotalEntradas()));
        assertEquals(0, new BigDecimal("70.00").compareTo(mov.getTotalSaidas()));
    }
}
