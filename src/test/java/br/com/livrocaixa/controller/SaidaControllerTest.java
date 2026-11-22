package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dubles.DadosTesteFactory;
import br.com.livrocaixa.controller.dubles.MemoriaSaidaDAO;
import br.com.livrocaixa.controller.dubles.MemoriaTipoSaidaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Saida;
import br.com.livrocaixa.model.TipoSaida;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SaidaControllerTest {

    private MemoriaSaidaDAO saidaDAO;
    private MemoriaTipoSaidaDAO tipoSaidaDAO;
    private TipoSaidaController tipoSaidaController;
    private SaidaController saidaController;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().iniciar(DadosTesteFactory.recepcionista());
        saidaDAO = new MemoriaSaidaDAO();
        tipoSaidaDAO = new MemoriaTipoSaidaDAO();
        tipoSaidaController = new TipoSaidaController(tipoSaidaDAO);
        saidaController = new SaidaController(saidaDAO, tipoSaidaController);
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void deveRegistrarSaidaValidaComFuncionarioLogado() {
        TipoSaida tipo = new TipoSaida();
        tipo.setDescricao("Correios");
        tipoSaidaController.salvar(tipo);

        Saida salva = saidaController.registrarSaida(
                LocalDate.of(2026, 11, 22),
                new BigDecimal("35.50"),
                tipo,
                "Envio de documentos");

        assertNotNull(salva.getId());
        assertEquals(DadosTesteFactory.recepcionista().getId(), salva.getFuncionario().getId());
        assertEquals(1, saidaDAO.listarPorPeriodo(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)).size());
    }

    @Test
    void deveRejeitarSaidaComValorMenorOuIgualAZeroOuCamposObrigatoriosAusentes() {
        TipoSaida tipo = DadosTesteFactory.tipoSaidaPadrao();

        assertThrows(ValidacaoException.class, () -> saidaController.registrarSaida(
                LocalDate.of(2026, 11, 22), BigDecimal.ZERO, tipo, "Valor zero"));
        assertThrows(ValidacaoException.class, () -> saidaController.registrarSaida(
                LocalDate.of(2026, 11, 22), new BigDecimal("-10.00"), tipo, "Valor negativo"));
        assertThrows(ValidacaoException.class, () -> saidaController.registrarSaida(
                null, new BigDecimal("20.00"), tipo, "Sem data"));
        assertThrows(ValidacaoException.class, () -> saidaController.registrarSaida(
                LocalDate.of(2026, 11, 22), new BigDecimal("20.00"), null, "Sem tipo"));
    }

    @Test
    void devePermitirCadastrarTipoSaidaDuranteRegistro() {
        TipoSaida novo = new TipoSaida();
        novo.setDescricao("Limpeza");

        TipoSaida salvo = saidaController.cadastrarTipoSaidaDuranteRegistro(novo);
        assertNotNull(salvo.getId());
        assertEquals(1, tipoSaidaController.listar().size());
    }
}
