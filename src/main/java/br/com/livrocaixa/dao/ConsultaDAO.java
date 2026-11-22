package br.com.livrocaixa.dao;

import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.model.Consulta;

import java.util.Optional;

/**
 * Contrato de persistência para a entidade {@link Consulta} e seu agregado
 * ({@code Fatura} e {@code Parcela}).
 */
public interface ConsultaDAO {

    /**
     * Operação <strong>transacional</strong>: persiste a consulta, sua fatura e todas as
     * parcelas geradas na mesma transação, preenchendo os identificadores gerados nos objetos.
     * Em caso de falha em qualquer etapa, deve realizar rollback completo.
     *
     * @throws PersistenciaException em caso de falha na persistência transacional
     */
    void inserirComFatura(Consulta consulta);

    /**
     * Busca uma consulta pelo identificador, carregando cliente, tipo de consulta,
     * funcionário, fatura e respectivas parcelas.
     *
     * @throws PersistenciaException em caso de falha no acesso ao banco de dados
     */
    Optional<Consulta> buscarPorId(Integer id);
}
