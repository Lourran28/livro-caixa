package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.TipoConsultaDAO;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.TipoConsulta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Dublê em memória de {@link TipoConsultaDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaTipoConsultaDAO implements TipoConsultaDAO {

    private final Map<Integer, TipoConsulta> banco = new LinkedHashMap<>();
    private final Set<Integer> idsEmUso = new HashSet<>();
    private int sequencia = 1;

    public void marcarEmUso(Integer id) {
        idsEmUso.add(id);
    }

    @Override
    public Optional<TipoConsulta> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public List<TipoConsulta> listar() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public void inserir(TipoConsulta tipoConsulta) {
        if (tipoConsulta.getId() == null) {
            tipoConsulta.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, tipoConsulta.getId() + 1);
        }
        banco.put(tipoConsulta.getId(), tipoConsulta);
    }

    @Override
    public void atualizar(TipoConsulta tipoConsulta) {
        banco.put(tipoConsulta.getId(), tipoConsulta);
    }

    @Override
    public void excluir(Integer id) {
        if (idsEmUso.contains(id)) {
            throw PersistenciaException.violacaoIntegridade("FK em uso", null);
        }
        banco.remove(id);
    }
}
