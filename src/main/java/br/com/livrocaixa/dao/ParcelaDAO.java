package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Pagamento;
import br.com.livrocaixa.model.Parcela;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link Parcela} e suas operações de baixa,
 * estorno e cancelamento.
 * <p>
 * <strong>Regra crítica do contrato:</strong> todo método de leitura ({@code buscarPorId},
 * {@code listarPorCliente}, {@code listarAtrasadas}) deve obrigatoriamente carregar os
 * pagamentos já registrados para cada parcela e anexá-los via
 * {@link Parcela#vincularPagamento(Pagamento)}, garantindo que {@code getSaldo()} e as
 * regras de transição de situação funcionem corretamente.
 */
public interface ParcelaDAO {

    /**
     * Lista todas as parcelas associadas às consultas/faturas de um cliente,
     * com os pagamentos de cada parcela devidamente carregados via {@code vincularPagamento}.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Parcela> listarPorCliente(Integer idCliente);

    /**
     * Lista parcelas vencidas e ainda não quitadas até {@code dataReferencia}, excluindo
     * parcelas pagas e canceladas, com seus pagamentos carregados via {@code vincularPagamento}.
     *
     * @param dataReferencia    data usada como referência para verificação de atraso
     * @param idClienteOpcional se informado, filtra pelo cliente; se {@code null}, retorna de todos
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    List<Parcela> listarAtrasadas(LocalDate dataReferencia, Integer idClienteOpcional);

    /**
     * Busca uma parcela pelo identificador, com todos os seus pagamentos já vinculados
     * via {@code vincularPagamento}.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Parcela> buscarPorId(Integer idParcela);

    /**
     * Atualiza o valor nominal de uma parcela já persistida.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    void atualizarValor(Parcela parcela);

    /**
     * Operação <strong>transacional</strong>: insere o novo {@link Pagamento} (atribuindo o
     * identificador gerado) e atualiza a {@code situacaoParcela} da parcela na mesma transação.
     *
     * @throws PersistenciaException em caso de falha na persistência transacional
     */
    void registrarBaixa(Parcela parcela, Pagamento pagamento);

    /**
     * Operação <strong>transacional</strong>: exclui o {@link Pagamento} estornado e atualiza
     * a {@code situacaoParcela} da parcela na mesma transação.
     *
     * @throws PersistenciaException em caso de falha na persistência transacional
     */
    void desfazerBaixa(Parcela parcela, Pagamento pagamento);

    /**
     * Operação <strong>transacional</strong>: exclui todos os pagamentos estornados da parcela
     * e atualiza sua {@code situacaoParcela} para Cancelada na mesma transação.
     *
     * @throws PersistenciaException em caso de falha na persistência transacional
     */
    void cancelar(Parcela parcela, List<Pagamento> pagamentosEstornados);
}
