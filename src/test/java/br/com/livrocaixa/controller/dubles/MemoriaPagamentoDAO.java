package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.PagamentoDAO;
import br.com.livrocaixa.model.Pagamento;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dublê em memória de {@link PagamentoDAO} exclusivo para testes unitários dos controllers.
 * Compartilhado com {@link MemoriaParcelaDAO} para simular o livro caixa de entradas.
 */
public class MemoriaPagamentoDAO implements PagamentoDAO {

    private final Map<Integer, Pagamento> banco = new LinkedHashMap<>();
    private int sequencia = 1;

    public void salvar(Pagamento pagamento) {
        if (pagamento.getId() == null) {
            pagamento.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, pagamento.getId() + 1);
        }
        banco.put(pagamento.getId(), pagamento);
    }

    public void remover(Pagamento pagamento) {
        if (pagamento != null && pagamento.getId() != null) {
            banco.remove(pagamento.getId());
        }
    }

    public List<Pagamento> listarTodos() {
        return List.copyOf(banco.values());
    }

    @Override
    public List<Pagamento> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return banco.values().stream()
                .filter(p -> p.getData() != null
                        && !p.getData().isBefore(inicio)
                        && !p.getData().isAfter(fim))
                .toList();
    }

    @Override
    public List<Pagamento> listarPorFuncionario(Integer idFuncionario) {
        return banco.values().stream()
                .filter(p -> p.getFuncionario() != null
                        && Objects.equals(p.getFuncionario().getId(), idFuncionario))
                .toList();
    }
}
