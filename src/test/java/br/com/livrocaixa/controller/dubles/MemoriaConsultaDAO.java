package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.ConsultaDAO;
import br.com.livrocaixa.model.Consulta;
import br.com.livrocaixa.model.Fatura;
import br.com.livrocaixa.model.Parcela;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dublê em memória de {@link ConsultaDAO} exclusivo para testes unitários dos controllers.
 * Ao gravar uma consulta com fatura, também registra as parcelas no {@link MemoriaParcelaDAO}.
 */
public class MemoriaConsultaDAO implements ConsultaDAO {

    private final Map<Integer, Consulta> banco = new LinkedHashMap<>();
    private final MemoriaParcelaDAO parcelaDAO;
    private int seqConsulta = 1;
    private int seqFatura = 1;

    public MemoriaConsultaDAO(MemoriaParcelaDAO parcelaDAO) {
        this.parcelaDAO = parcelaDAO;
    }

    @Override
    public void inserirComFatura(Consulta consulta) {
        if (consulta.getId() == null) {
            consulta.setId(seqConsulta++);
        }
        Fatura fatura = consulta.getFatura();
        if (fatura != null) {
            if (fatura.getId() == null) {
                fatura.setId(seqFatura++);
            }
            if (fatura.getParcelas() != null && parcelaDAO != null) {
                for (Parcela parcela : fatura.getParcelas()) {
                    parcelaDAO.armazenar(parcela);
                }
            }
        }
        banco.put(consulta.getId(), consulta);
    }

    @Override
    public Optional<Consulta> buscarPorId(Integer id) {
        return Optional.ofNullable(banco.get(id));
    }
}
