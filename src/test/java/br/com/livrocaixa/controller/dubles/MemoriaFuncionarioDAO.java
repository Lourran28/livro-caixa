package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.FuncionarioDAO;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Funcionario;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Dublê em memória de {@link FuncionarioDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaFuncionarioDAO implements FuncionarioDAO {

    private final Map<Integer, Funcionario> banco = new LinkedHashMap<>();
    private final Set<Integer> idsEmUso = new HashSet<>();
    private int sequencia = 1;

    public void marcarEmUso(Integer id) {
        idsEmUso.add(id);
    }

    @Override
    public Optional<Funcionario> buscarPorNome(String nome) {
        if (nome == null) {
            return Optional.empty();
        }
        return banco.values().stream()
                .filter(f -> nome.equalsIgnoreCase(f.getNome()))
                .findFirst();
    }

    @Override
    public Optional<Funcionario> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public List<Funcionario> listar() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public void inserir(Funcionario funcionario) {
        if (funcionario.getId() == null) {
            funcionario.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, funcionario.getId() + 1);
        }
        banco.put(funcionario.getId(), funcionario);
    }

    @Override
    public void atualizar(Funcionario funcionario) {
        banco.put(funcionario.getId(), funcionario);
    }

    @Override
    public void excluir(Integer id) {
        if (idsEmUso.contains(id)) {
            throw PersistenciaException.violacaoIntegridade("FK em uso", null);
        }
        banco.remove(id);
    }
}
