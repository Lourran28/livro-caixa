package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.ClienteDAO;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Cliente;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Dublê em memória de {@link ClienteDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaClienteDAO implements ClienteDAO {

    private final Map<Integer, Cliente> banco = new LinkedHashMap<>();
    private final Set<Integer> idsEmUso = new HashSet<>();
    private int sequencia = 1;

    public void marcarEmUso(Integer id) {
        idsEmUso.add(id);
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        return banco.values().stream()
                .filter(c -> Objects.equals(cpf, c.getCpf()))
                .findFirst();
    }

    @Override
    public List<Cliente> listar() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public void inserir(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, cliente.getId() + 1);
        }
        banco.put(cliente.getId(), cliente);
    }

    @Override
    public void atualizar(Cliente cliente) {
        banco.put(cliente.getId(), cliente);
    }

    @Override
    public void excluir(Integer id) {
        if (idsEmUso.contains(id)) {
            throw PersistenciaException.violacaoIntegridade("FK em uso", null);
        }
        banco.remove(id);
    }
}
