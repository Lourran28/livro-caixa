package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dubles.DadosTesteFactory;
import br.com.livrocaixa.controller.dubles.MemoriaClienteDAO;
import br.com.livrocaixa.controller.dubles.MemoriaFuncionarioDAO;
import br.com.livrocaixa.controller.dubles.MemoriaTipoSaidaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Cliente;
import br.com.livrocaixa.model.TipoSaida;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CadastrosBasicosControllerTest {

    // CPFs matematicamente válidos apenas para fins de teste
    private static final String CPF_VALIDO_1 = "52998224725";
    private static final String CPF_VALIDO_2 = "11144477735";

    private MemoriaClienteDAO clienteDAO;
    private MemoriaFuncionarioDAO funcionarioDAO;
    private MemoriaTipoSaidaDAO tipoSaidaDAO;

    private ClienteController clienteController;
    private FuncionarioController funcionarioController;
    private TipoSaidaController tipoSaidaController;

    @BeforeEach
    void setUp() {
        SessaoUsuario.getInstancia().iniciar(DadosTesteFactory.admin());
        clienteDAO = new MemoriaClienteDAO();
        funcionarioDAO = new MemoriaFuncionarioDAO();
        tipoSaidaDAO = new MemoriaTipoSaidaDAO();

        clienteController = new ClienteController(clienteDAO);
        funcionarioController = new FuncionarioController(funcionarioDAO);
        tipoSaidaController = new TipoSaidaController(tipoSaidaDAO);
    }

    @AfterEach
    void tearDown() {
        SessaoUsuario.getInstancia().encerrar();
    }

    @Test
    void deveRejeitarClienteComCpfInvalido() {
        Cliente invalido = DadosTesteFactory.clienteValido(null, "João da Silva", "123.456.789-00");

        assertThrows(ValidacaoException.class, () -> clienteController.salvar(invalido));
        assertTrue(clienteController.listar().isEmpty());
    }

    @Test
    void deveRejeitarCpfDuplicadoNaInsercaoEPermitirEdicaoDoProprioCliente() {
        Cliente c1 = DadosTesteFactory.clienteValido(null, "Ana Souza", CPF_VALIDO_1);
        clienteController.salvar(c1);
        assertNotNull(c1.getId());

        // Outro cliente tentando usar o mesmo CPF -> erro de duplicidade
        Cliente duplicado = DadosTesteFactory.clienteValido(null, "Outra Pessoa", CPF_VALIDO_1);
        ValidacaoException erro = assertThrows(
                ValidacaoException.class, () -> clienteController.salvar(duplicado));
        assertTrue(erro.getMessage().toLowerCase().contains("cpf"));

        // Edição do próprio cliente mantendo seu CPF -> permitido
        c1.setNome("Ana Souza Lima");
        assertDoesNotThrow(() -> clienteController.salvar(c1));

        // Segundo cliente tenta alterar para o CPF do primeiro -> erro de duplicidade
        Cliente c2 = DadosTesteFactory.clienteValido(null, "Carlos Pereira", CPF_VALIDO_2);
        clienteController.salvar(c2);
        c2.setCpf(CPF_VALIDO_1);
        assertThrows(ValidacaoException.class, () -> clienteController.salvar(c2));
    }

    @Test
    void deveTraduzirErroDeChaveEstrangeiraAoExcluirRegistroEmUso() {
        TipoSaida tipo = new TipoSaida();
        tipo.setDescricao("Energia Elétrica");
        tipoSaidaController.salvar(tipo);
        tipoSaidaDAO.marcarEmUso(tipo.getId());

        ValidacaoException erro = assertThrows(
                ValidacaoException.class, () -> tipoSaidaController.excluir(tipo.getId()));
        assertTrue(erro.getMessage().contains("registros vinculados"));
    }

    @Test
    void deveImpedirExclusaoDoProprioFuncionarioLogado() {
        Integer idLogado = DadosTesteFactory.admin().getId();
        assertThrows(ValidacaoException.class, () -> funcionarioController.excluir(idLogado));
    }
}
