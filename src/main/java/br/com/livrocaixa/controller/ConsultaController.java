package br.com.livrocaixa.controller;

import br.com.livrocaixa.dao.ConsultaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Cliente;
import br.com.livrocaixa.model.Consulta;
import br.com.livrocaixa.model.Fatura;
import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.model.Parcela;
import br.com.livrocaixa.model.TipoConsulta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller responsável pelo registro de consultas, geração de faturas e parcelas
 * automáticas com possibilidade de ajuste manual antes da gravação (UC003, RF04, RF06, RF07).
 * Acesso exclusivo do perfil Administrador.
 */
public class ConsultaController extends ControllerBase {

    private final ConsultaDAO consultaDAO;
    private final ClienteController clienteController;
    private final TipoConsultaController tipoConsultaController;

    public ConsultaController(
            ConsultaDAO consultaDAO,
            ClienteController clienteController,
            TipoConsultaController tipoConsultaController) {
        this(consultaDAO, clienteController, tipoConsultaController, SessaoUsuario.getInstancia());
    }

    public ConsultaController(
            ConsultaDAO consultaDAO,
            ClienteController clienteController,
            TipoConsultaController tipoConsultaController,
            SessaoUsuario sessao) {
        super(sessao);
        this.consultaDAO = Objects.requireNonNull(consultaDAO, "ConsultaDAO não pode ser nulo.");
        this.clienteController = Objects.requireNonNull(
                clienteController, "ClienteController não pode ser nulo.");
        this.tipoConsultaController = Objects.requireNonNull(
                tipoConsultaController, "TipoConsultaController não pode ser nulo.");
    }

    /**
     * Monta uma {@link Consulta} em memória com sua {@link Fatura} e parcelas geradas
     * automaticamente (à vista ou a prazo), sem persistir no banco, para que a View exiba
     * os valores e permita edição manual antes da confirmação (RF06).
     *
     * @param cliente            cliente que realizou a consulta
     * @param tipoConsulta       tipo de consulta jurídica
     * @param data               data da consulta
     * @param observacao         observações opcionais
     * @param valorTotal         valor total da fatura (maior que zero)
     * @param aVista             {@code true} para pagamento à vista; {@code false} para parcelado
     * @param qtdParcelas        quantidade de parcelas quando {@code aVista == false}
     * @param primeiroVencimento data de vencimento da primeira parcela quando a prazo
     * @param entrada            valor de entrada opcional (ou {@code null}/zero quando sem entrada)
     * @return consulta pronta com fatura e parcelas geradas (ainda não salva)
     */
    public Consulta prepararConsulta(
            Cliente cliente,
            TipoConsulta tipoConsulta,
            LocalDate data,
            String observacao,
            BigDecimal valorTotal,
            boolean aVista,
            Integer qtdParcelas,
            LocalDate primeiroVencimento,
            BigDecimal entrada) {
        Funcionario usuarioLogado = exigirAdministrador();

        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O valor total da consulta deve ser maior que zero.");
        }

        Consulta consulta = new Consulta();
        consulta.setCliente(cliente);
        consulta.setTipoConsulta(tipoConsulta);
        consulta.setData(data);
        consulta.setObservacao(observacao);
        consulta.setFuncionario(usuarioLogado);

        consulta.gerarFatura(valorTotal);
        Fatura fatura = consulta.getFatura();

        if (aVista) {
            fatura.gerarParcelasAVista();
        } else {
            if (qtdParcelas == null || qtdParcelas <= 0) {
                throw new ValidacaoException(
                        "Informe a quantidade de parcelas para pagamento a prazo.");
            }
            fatura.gerarParcelasAPrazo(qtdParcelas, primeiroVencimento, entrada);
        }

        return consulta;
    }

    /**
     * Altera o valor de uma parcela específica na consulta ainda em preparação (RF06, UC003 3a).
     * A conferência da soma total das parcelas com o valor da fatura ocorre ao salvar.
     *
     * @param consulta  consulta preparada via {@link #prepararConsulta}
     * @param posicao   posição numérica da parcela (1, 2, 3...)
     * @param novoValor novo valor da parcela
     */
    public void alterarValorParcela(Consulta consulta, int posicao, BigDecimal novoValor) {
        exigirAdministrador();
        if (consulta == null || consulta.getFatura() == null) {
            throw new ValidacaoException("A consulta precisa possuir uma fatura gerada.");
        }

        List<Parcela> parcelas = consulta.getFatura().getParcelas();
        if (parcelas == null || parcelas.isEmpty()) {
            throw new ValidacaoException("A fatura não possui parcelas geradas para alteração.");
        }

        Parcela alvo = parcelas.stream()
                .filter(p -> p.getPosicaoParcela() == posicao)
                .findFirst()
                .orElseThrow(() -> new ValidacaoException(
                        "Parcela na posição " + posicao + " não encontrada na fatura."));

        alvo.alterarValor(novoValor);
    }

    /**
     * Valida integralmente a consulta, a fatura e as parcelas (incluindo se a soma das
     * parcelas corresponde ao valor total da fatura) e persiste tudo em uma única transação
     * (RF04, RF07, RNF004).
     */
    public Consulta salvarConsulta(Consulta consulta) {
        Funcionario usuarioLogado = exigirAdministrador();
        if (consulta == null) {
            throw new ValidacaoException("Os dados da consulta devem ser informados.");
        }
        if (consulta.getFatura() == null) {
            throw new ValidacaoException("A consulta deve possuir uma fatura gerada antes de ser salva.");
        }
        if (consulta.getFuncionario() == null) {
            consulta.setFuncionario(usuarioLogado);
        }

        // Valida consulta, fatura, cada parcela e igualdade entre soma das parcelas e valorTotal
        consulta.validar();
        consulta.getFatura().getParcelas().forEach(Parcela::validar);

        consultaDAO.inserirComFatura(consulta);
        return consulta;
    }

    /**
     * Busca uma consulta já registrada pelo seu identificador.
     */
    public Optional<Consulta> buscarPorId(Integer idConsulta) {
        exigirAdministrador();
        validarIdObrigatorio(idConsulta, "a consulta");
        return consultaDAO.buscarPorId(idConsulta);
    }

    /**
     * Permite cadastrar um cliente durante o fluxo de registro de consulta
     * (extensão do diagrama de casos de uso UC003 -> Manter Cliente).
     */
    public Cliente cadastrarClienteDuranteConsulta(Cliente cliente) {
        exigirAdministrador();
        return clienteController.salvar(cliente);
    }

    /**
     * Permite cadastrar um tipo de consulta durante o fluxo de registro de consulta
     * (extensão do diagrama de casos de uso UC003 -> Manter Tipo Consulta).
     */
    public TipoConsulta cadastrarTipoConsultaDuranteConsulta(TipoConsulta tipoConsulta) {
        exigirAdministrador();
        return tipoConsultaController.salvar(tipoConsulta);
    }
}
