package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.TipoSaidaDAO;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.TipoSaida;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Dublê em memória de {@link TipoSaidaDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaTipoSaidaDAO implements TipoSaidaDAO {

    private final Map<Integer, TipoSaida> banco = new LinkedHashMap<>();
    private final Set<Integer> idsEmUso = new HashSet<>();
    private int sequencia = 1;

    public void marcarEmUso(Integer id) {
        idsEmUso.add(id);
    }

    @Override
    public Optional<TipoSaida> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public List<TipoSaida> listar() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public void inserir(TipoSaida tipoSaida) {
        if (tipoSaida.getId() == null) {
            tipoSaida.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, tipoSaida.getId() + 1);
        }
        banco.put(tipoSaida.getId(), tipoSaida);
    }

    @Override
    public void atualizar(TipoSaida tipoSaida) {
        banco.put(tipoSaida.getId(), tipoSaida);
    }

    @Override
    public void excluir(Integer id) {
        if (idsEmUso.contains(id)) {
            throw PersistenciaException.violacaoIntegridade("FK em uso", null);
        }
        banco.remove(id);
    }
}
