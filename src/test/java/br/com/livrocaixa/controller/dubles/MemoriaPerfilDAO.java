package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.PerfilDAO;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Perfil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Dublê em memória de {@link PerfilDAO} exclusivo para testes unitários dos controllers.
 */
public class MemoriaPerfilDAO implements PerfilDAO {

    private final Map<Integer, Perfil> banco = new LinkedHashMap<>();
    private final Set<Integer> idsEmUso = new HashSet<>();
    private int sequencia = 1;

    public void marcarEmUso(Integer id) {
        idsEmUso.add(id);
    }

    @Override
    public Optional<Perfil> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }

    @Override
    public List<Perfil> listar() {
        return new ArrayList<>(banco.values());
    }

    @Override
    public void inserir(Perfil perfil) {
        if (perfil.getId() == null) {
            perfil.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, perfil.getId() + 1);
        }
        banco.put(perfil.getId(), perfil);
    }

    @Override
    public void atualizar(Perfil perfil) {
        banco.put(perfil.getId(), perfil);
    }

    @Override
    public void excluir(Integer id) {
        if (idsEmUso.contains(id)) {
            throw PersistenciaException.violacaoIntegridade("FK em uso", null);
        }
        banco.remove(id);
    }
}
