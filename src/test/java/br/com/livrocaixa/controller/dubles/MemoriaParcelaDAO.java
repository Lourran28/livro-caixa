package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.dao.ParcelaDAO;
import br.com.livrocaixa.model.Pagamento;
import br.com.livrocaixa.model.Parcela;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Dublê em memória de {@link ParcelaDAO} exclusivo para testes unitários dos controllers.
 * Mantém sincronia transacional com {@link MemoriaPagamentoDAO}.
 */
public class MemoriaParcelaDAO implements ParcelaDAO {

    private final Map<Integer, Parcela> banco = new LinkedHashMap<>();
    private final MemoriaPagamentoDAO pagamentoDAO;
    private int sequencia = 1;

    public MemoriaParcelaDAO(MemoriaPagamentoDAO pagamentoDAO) {
        this.pagamentoDAO = pagamentoDAO;
    }

    public void armazenar(Parcela parcela) {
        if (parcela.getId() == null) {
            parcela.setId(sequencia++);
        } else {
            sequencia = Math.max(sequencia, parcela.getId() + 1);
        }
        banco.put(parcela.getId(), parcela);
    }

    @Override
    public List<Parcela> listarPorCliente(Integer idCliente) {
        return banco.values().stream()
                .filter(p -> pertenceAoCliente(p, idCliente))
                .toList();
    }

    @Override
    public List<Parcela> listarAtrasadas(LocalDate dataReferencia, Integer idClienteOpcional) {
        return banco.values().stream()
                .filter(p -> idClienteOpcional == null || pertenceAoCliente(p, idClienteOpcional))
                .filter(p -> p.isAtrasada(dataReferencia))
                .toList();
    }

    @Override
    public Optional<Parcela> buscarPorId(Integer idParcela) {
        return Optional.ofNullable(banco.get(idParcela));
    }

    @Override
    public void atualizarValor(Parcela parcela) {
        banco.put(parcela.getId(), parcela);
    }

    @Override
    public void registrarBaixa(Parcela parcela, Pagamento pagamento) {
        pagamentoDAO.salvar(pagamento);
        banco.put(parcela.getId(), parcela);
    }

    @Override
    public void desfazerBaixa(Parcela parcela, Pagamento pagamento) {
        pagamentoDAO.remover(pagamento);
        banco.put(parcela.getId(), parcela);
    }

    @Override
    public void cancelar(Parcela parcela, List<Pagamento> pagamentosEstornados) {
        if (pagamentosEstornados != null) {
            for (Pagamento pag : pagamentosEstornados) {
                pagamentoDAO.remover(pag);
            }
        }
        banco.put(parcela.getId(), parcela);
    }

    private boolean pertenceAoCliente(Parcela parcela, Integer idCliente) {
        return parcela.getFatura() != null
                && parcela.getFatura().getConsulta() != null
                && parcela.getFatura().getConsulta().getCliente() != null
                && Objects.equals(parcela.getFatura().getConsulta().getCliente().getId(), idCliente);
    }
}
