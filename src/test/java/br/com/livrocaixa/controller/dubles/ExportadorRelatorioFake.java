package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.controller.ExportadorRelatorio;
import br.com.livrocaixa.controller.dto.RelatorioFluxoCaixa;

import java.nio.file.Path;

/**
 * Dublê de {@link ExportadorRelatorio} para verificar chamadas de exportação em testes.
 */
public class ExportadorRelatorioFake implements ExportadorRelatorio {

    private RelatorioFluxoCaixa ultimoRelatorio;
    private Path ultimoDestino;
    private int totalChamadas;

    @Override
    public void exportarPdf(RelatorioFluxoCaixa relatorio, Path destino) {
        this.ultimoRelatorio = relatorio;
        this.ultimoDestino = destino;
        this.totalChamadas++;
    }

    public RelatorioFluxoCaixa getUltimoRelatorio() {
        return ultimoRelatorio;
    }

    public Path getUltimoDestino() {
        return ultimoDestino;
    }

    public int getTotalChamadas() {
        return totalChamadas;
    }
}
