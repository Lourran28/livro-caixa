package br.com.livrocaixa.controller;

import br.com.livrocaixa.controller.dto.RelatorioFluxoCaixa;

import java.nio.file.Path;

/**
 * Contrato para exportação do relatório de fluxo de caixa em formato PDF (RNF006).
 * A implementação concreta (OpenPDF, Apache PDFBox, etc.) será acoplada futuramente
 * sem impactar o {@link RelatorioController}.
 */
public interface ExportadorRelatorio {

    /**
     * Gera o arquivo PDF correspondente ao relatório informado no caminho de destino.
     *
     * @param relatorio dados consolidados do fluxo de caixa
     * @param destino   caminho do arquivo PDF a ser criado
     */
    void exportarPdf(RelatorioFluxoCaixa relatorio, Path destino);
}
