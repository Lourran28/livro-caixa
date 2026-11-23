package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.SaidaDAO;
import br.com.livrocaixa.model.Saida;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dublê em memória de {@link SaidaDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaSaidaDAO implements SaidaDAO {

    private final Map<Integer, Saida> banco = new LinkedHashMap<>();
    private int sequencia = 1;

    @Override
    public void inserir(Saida saida) {
        if (saida.getId() == null) {
            saida.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, saida.getId() + 1);
        }
        banco.put(saida.getId(), saida);
    }

    @Override
    public List<Saida> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return banco.values().stream()
                .filter(s -> s.getData() != null
                        && !s.getData().isBefore(inicio)
                        && !s.getData().isAfter(fim))
                .toList();
    }

    @Override
    public List<Saida> listarPorFuncionario(Integer idFuncionario) {
        return banco.values().stream()
                .filter(s -> s.getFuncionario() != null
                        && Objects.equals(s.getFuncionario().getId(), idFuncionario))
                .toList();
    }
}
