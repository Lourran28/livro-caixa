package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.FormaPagamentoDAO;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.FormaPagamento;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Dublê em memória de {@link FormaPagamentoDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaFormaPagamentoDAO implements FormaPagamentoDAO {

    private final Map<Integer, FormaPagamento> banco = new LinkedHashMap<>();
    private final Set<Integer> idsEmUso = new HashSet<>();
    private int sequencia = 1;

    public void marcarEmUso(Integer id) {
        idsEmUso.add(id);
    }

    @Override
    public Optional<FormaPagamento> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public List<FormaPagamento> listar() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public void inserir(FormaPagamento formaPagamento) {
        if (formaPagamento.getId() == null) {
            formaPagamento.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, formaPagamento.getId() + 1);
        }
        banco.put(formaPagamento.getId(), formaPagamento);
    }

    @Override
    public void atualizar(FormaPagamento formaPagamento) {
        banco.put(formaPagamento.getId(), formaPagamento);
    }

    @Override
    public void excluir(Integer id) {
        if (idsEmUso.contains(id)) {
            throw PersistenciaException.violacaoIntegridade("FK em uso", null);
        }
        banco.remove(id);
    }
}
