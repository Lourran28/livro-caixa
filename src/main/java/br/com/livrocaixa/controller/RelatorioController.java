package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dto.RelatorioFluxoCaixa;
import br.com.livrocaixa.dao.PagamentoDAO;
import br.com.livrocaixa.dao.SaidaDAO;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Pagamento;
import br.com.livrocaixa.model.Saida;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Controller responsável pela geração de relatórios consolidados de fluxo de caixa
 * (entradas, saídas e saldo) por ano, mês ou data específica e pela delegação da
 * exportação em PDF (UC006, RF11, RNF003, RNF006).
 * <p>
 * Acesso <strong>exclusivo</strong> do perfil Administrador.
 */
public class RelatorioController extends ControllerBase {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PagamentoDAO pagamentoDAO;
    private final SaidaDAO saidaDAO;
    private final ExportadorRelatorio exportadorRelatorio;

    public RelatorioController(PagamentoDAO pagamentoDAO, SaidaDAO saidaDAO) {
        this(pagamentoDAO, saidaDAO, null, SessaoUsuario.getInstancia());
    }

    public RelatorioController(
            PagamentoDAO pagamentoDAO,
            SaidaDAO saidaDAO,
            ExportadorRelatorio exportadorRelatorio) {
        this(pagamentoDAO, saidaDAO, exportadorRelatorio, SessaoUsuario.getInstancia());
    }

    public RelatorioController(
            PagamentoDAO pagamentoDAO,
            SaidaDAO saidaDAO,
            ExportadorRelatorio exportadorRelatorio,
            SessaoUsuario sessao) {
        super(sessao);
        this.pagamentoDAO = Objects.requireNonNull(pagamentoDAO, "PagamentoDAO não pode ser nulo.");
        this.saidaDAO = Objects.requireNonNull(saidaDAO, "SaidaDAO não pode ser nulo.");
        this.exportadorRelatorio = exportadorRelatorio;
    }

    /**
     * Consolida as entradas (pagamentos), saídas e saldo para o nível de período informado:
     * <ul>
     *   <li>Apenas {@code data}: relatório de uma data específica;</li>
     *   <li>{@code ano} e {@code mes}: relatório mensal completo;</li>
     *   <li>Apenas {@code ano}: relatório anual completo.</li>
     * </ul>
     * Caso o período não possua movimentações, retorna o DTO com listas vazias e valores zerados
     * (UC006 Exceção 3a).
     *
     * @param ano  ano desejado (opcional se {@code data} for informada)
     * @param mes  mês desejado de 1 a 12 (exige {@code ano}; opcional)
     * @param data data específica (mutuamente exclusiva com {@code ano}/{@code mes})
     * @return relatório consolidado de fluxo de caixa
     */
    public RelatorioFluxoCaixa gerarFluxoCaixa(Integer ano, Integer mes, LocalDate data) {
        exigirAdministrador();

        PeriodoConsultado periodo = resolverPeriodo(ano, mes, data);

        List<Pagamento> entradas = pagamentoDAO.listarPorPeriodo(periodo.inicio(), periodo.fim());
        List<Saida> saidas = saidaDAO.listarPorPeriodo(periodo.inicio(), periodo.fim());

        BigDecimal totalEntradas = entradas.stream()
                .map(Pagamento::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = saidas.stream()
                .map(Saida::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = totalEntradas.subtract(totalSaidas);

        return new RelatorioFluxoCaixa(
                periodo.inicio(),
                periodo.fim(),
                periodo.descricao(),
                totalEntradas,
                totalSaidas,
                saldo,
                entradas,
                saidas);
    }

    /**
     * Exporta um relatório já gerado para PDF delegando ao {@link ExportadorRelatorio} configurado
     * (RNF006).
     *
     * @param relatorio relatório consolidado gerado por {@link #gerarFluxoCaixa}
     * @param destino   caminho do arquivo PDF a ser salvo
     */
    public void exportarPdf(RelatorioFluxoCaixa relatorio, Path destino) {
        exigirAdministrador();
        if (relatorio == null) {
            throw new ValidacaoException("O relatório de fluxo de caixa deve ser informado para exportação.");
        }
        if (destino == null) {
            throw new ValidacaoException("Informe o caminho de destino para exportação do PDF.");
        }
        if (exportadorRelatorio == null) {
            throw new ValidacaoException("Exportador de PDF ainda não foi configurado no sistema.");
        }
        exportadorRelatorio.exportarPdf(relatorio, destino);
    }

    private PeriodoConsultado resolverPeriodo(Integer ano, Integer mes, LocalDate data) {
        if (data != null) {
            if (ano != null || mes != null) {
                throw new ValidacaoException(
                        "Informe apenas uma data específica OU ano/mês, não ambos ao mesmo tempo.");
            }
            return new PeriodoConsultado(data, data, "Dia " + data.format(FORMATO_DATA_BR));
        }

        if (mes != null && ano == null) {
            throw new ValidacaoException("Para consultar por mês, informe também o ano.");
        }

        if (ano == null) {
            throw new ValidacaoException(
                    "Informe um ano, um ano e mês, ou uma data específica para gerar o relatório.");
        }

        if (ano < 1900 || ano > 9999) {
            throw new ValidacaoException("Ano informado inválido: " + ano + ".");
        }

        if (mes != null) {
            if (mes < 1 || mes > 12) {
                throw new ValidacaoException("O mês deve estar entre 1 e 12.");
            }
            YearMonth ym = YearMonth.of(ano, mes);
            String desc = String.format("%02d/%04d", mes, ano);
            return new PeriodoConsultado(ym.atDay(1), ym.atEndOfMonth(), "Mês " + desc);
        }

        return new PeriodoConsultado(
                LocalDate.of(ano, 1, 1),
                LocalDate.of(ano, 12, 31),
                "Ano " + ano);
    }

    private record PeriodoConsultado(LocalDate inicio, LocalDate fim, String descricao) {
    }
}
