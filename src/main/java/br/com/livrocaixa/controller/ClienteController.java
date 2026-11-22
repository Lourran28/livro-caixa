package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.ClienteDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Cliente;
import br.com.livrocaixa.util.CpfValidator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller para manutenção do cadastro de clientes pessoas físicas (UC002, RF02, RNF002).
 * Garante validação de integridade do CPF (via model) e unicidade no banco de dados.
 * Acesso exclusivo do perfil Administrador.
 */
public class ClienteController extends ControllerBase {

    private final ClienteDAO clienteDAO;

    public ClienteController(ClienteDAO clienteDAO) {
        this(clienteDAO, SessaoUsuario.getInstancia());
    }

    public ClienteController(ClienteDAO clienteDAO, SessaoUsuario sessao) {
        super(sessao);
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "ClienteDAO não pode ser nulo.");
    }

    /**
     * Lista todos os clientes cadastrados.
     */
    public List<Cliente> listar() {
        exigirAdministrador();
        return clienteDAO.listar();
    }

    /**
     * Busca um cliente pelo identificador.
     */
    public Optional<Cliente> buscarPorId(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o cliente");
        return clienteDAO.buscarPorId(id);
    }

    /**
     * Busca um cliente pelo CPF (aceita com ou sem pontuação, normalizando para {@code 000.000.000-00}).
     */
    public Optional<Cliente> buscarPorCpf(String cpf) {
        exigirAdministrador();
        if (cpf == null || cpf.isBlank()) {
            throw new ValidacaoException("Informe o CPF do cliente para consulta.");
        }
        return clienteDAO.buscarPorCpf(CpfValidator.formatar(cpf.trim()));
    }

    /**
     * Salva um cliente (insere se {@code id == null}, atualiza caso contrário).
     * Valida os campos obrigatórios e integridade do CPF via {@link Cliente#validar()} e
     * rejeita CPFs duplicados (ignorando o próprio cliente em caso de edição).
     *
     * @throws ValidacaoException se os dados/CPF forem inválidos ou se o CPF já existir
     */
    public Cliente salvar(Cliente cliente) {
        exigirAdministrador();
        if (cliente == null) {
            throw new ValidacaoException("Os dados do cliente devem ser informados.");
        }

        // Valida nome e formato/dígitos verificadores do CPF (RNF002)
        cliente.validar();

        // Verifica unicidade do CPF (UC002 Exceção 4b)
        Optional<Cliente> existente = clienteDAO.buscarPorCpf(cliente.getCpf());
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), cliente.getId())) {
            throw new ValidacaoException(
                    "Já existe um cliente cadastrado com o CPF " + cliente.getCpf() + ".");
        }

        if (cliente.getId() == null) {
            clienteDAO.inserir(cliente);
        } else {
            clienteDAO.atualizar(cliente);
        }
        return cliente;
    }

    /**
     * Exclui um cliente pelo identificador, impedindo exclusão caso possua consultas vinculadas.
     */
    public void excluir(Integer id) {
        exigirAdministrador();
        validarIdObrigatorio(id, "o cliente");
        executarExclusao(() -> clienteDAO.excluir(id), "o cliente");
    }

}
