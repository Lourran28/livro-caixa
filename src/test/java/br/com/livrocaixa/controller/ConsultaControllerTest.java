package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dubles.*;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Cliente;
import br.com.livrocaixa.model.Consulta;
import br.com.livrocaixa.model.TipoConsulta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ConsultaControllerTest {

    private static final String CPF_VALIDO = "52998224725";

    private MemoriaParcelaDAO parcelaDAO;
    private MemoriaConsultaDAO consultaDAO;
    private ConsultaController consultaController;
    private Cliente cliente;
    private TipoConsulta tipoConsulta;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().iniciar(DadosTesteFactory.admin());

        MemoriaPagamentoDAO pagamentoDAO = new MemoriaPagamentoDAO();
        parcelaDAO = new MemoriaParcelaDAO(pagamentoDAO);
        consultaDAO = new MemoriaConsultaDAO(parcelaDAO);

        ClienteController clienteController = new ClienteController(new MemoriaClienteDAO());
        TipoConsultaController tipoConsultaController = new TipoConsultaController(new MemoriaTipoConsultaDAO());

        consultaController = new ConsultaController(consultaDAO, clienteController, tipoConsultaController);

        cliente = clienteController.salvar(DadosTesteFactory.clienteValido(null, "Maria Clara", CPF_VALIDO));
        tipoConsulta = tipoConsultaController.salvar(DadosTesteFactory.tipoConsultaPadrao());
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void devePrepararESalvarConsultaAVista() {
        Consulta consulta = consultaController.prepararConsulta(
                cliente,
                tipoConsulta,
                LocalDate.of(2026, 11, 10),
                "Consulta inicial trabalhista",
                new BigDecimal("300.00"),
                true,
                null,
                null,
                null);

        assertNotNull(consulta.getFatura());
        assertEquals(1, consulta.getFatura().getParcelas().size());

        Consulta salva = consultaController.salvarConsulta(consulta);
        assertNotNull(salva.getId());
        assertTrue(consultaDAO.buscarPorId(salva.getId()).isPresent());
        assertEquals(1, parcelaDAO.listarPorCliente(cliente.getId()).size());
    }

    @Test
    void devePermitirEditarParcelasAPrazoERejeitarQuandoSomaNaoFecha() {
        Consulta consulta = consultaController.prepararConsulta(
                cliente,
                tipoConsulta,
                LocalDate.of(2026, 11, 10),
                "Ação previdenciária",
                new BigDecimal("1000.00"),
                false,
                2,
                LocalDate.of(2026, 12, 10),
                null);

        assertEquals(2, consulta.getFatura().getParcelas().size());

        // Altera apenas a 1ª parcela -> soma fica 600 + 500 = 1100 != 1000 -> deve lançar erro ao salvar
        consultaController.alterarValorParcela(consulta, 1, new BigDecimal("600.00"));
        assertThrows(ValidacaoException.class, () -> consultaController.salvarConsulta(consulta));

        // Ajusta a 2ª parcela para fechar a soma (600 + 400 = 1000) -> deve salvar normalmente
        consultaController.alterarValorParcela(consulta, 2, new BigDecimal("400.00"));
        Consulta salva = assertDoesNotThrow(() -> consultaController.salvarConsulta(consulta));
        assertNotNull(salva.getId());
        assertEquals(2, parcelaDAO.listarPorCliente(cliente.getId()).size());
    }

    @Test
    void deveGerarParcelaDeEntradaQuandoPagamentoAPrazoPossuiEntrada() {
        Consulta consulta = consultaController.prepararConsulta(
                cliente,
                tipoConsulta,
                LocalDate.of(2026, 11, 10),
                "Ação cível",
                new BigDecimal("1000.00"),
                false,
                2,
                LocalDate.of(2026, 12, 10),
                new BigDecimal("200.00"));

        assertEquals(3, consulta.getFatura().getParcelas().size());
        assertEquals(0, new BigDecimal("200.00").compareTo(consulta.getFatura().getParcelas().get(0).getValor()));
        assertEquals(0, new BigDecimal("400.00").compareTo(consulta.getFatura().getParcelas().get(1).getValor()));
        assertEquals(0, new BigDecimal("400.00").compareTo(consulta.getFatura().getParcelas().get(2).getValor()));

        Consulta salva = consultaController.salvarConsulta(consulta);
        assertNotNull(salva.getId());
    }

    @Test
    void devePermitirCadastrarClienteETipoConsultaDuranteRegistro() {
        Cliente novoCliente = DadosTesteFactory.clienteValido(null, "João Pedro", "11144477735");
        Cliente clienteSalvo = consultaController.cadastrarClienteDuranteConsulta(novoCliente);
        assertNotNull(clienteSalvo.getId());

        TipoConsulta novoTipo = new TipoConsulta();
        novoTipo.setDescricao("Direito de Família");
        TipoConsulta tipoSalvo = consultaController.cadastrarTipoConsultaDuranteConsulta(novoTipo);
        assertNotNull(tipoSalvo.getId());
    }
}
