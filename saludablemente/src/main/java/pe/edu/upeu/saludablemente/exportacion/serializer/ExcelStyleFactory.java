package pe.edu.upeu.saludablemente.exportacion.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExcelStyleFactory {

    public CellStyle crearEstiloCabecera(SXSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        fuente.setFontHeightInPoints((short) 11);
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }

    public CellStyle crearEstiloTitulo(SXSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 14);
        fuente.setColor(IndexedColors.DARK_GREEN.getIndex());
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    public CellStyle crearEstiloSubTitulo(SXSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 12);
        estilo.setFont(fuente);
        return estilo;
    }

    public CellStyle crearEstiloNumerico(SXSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        DataFormat formato = workbook.createDataFormat();
        estilo.setDataFormat(formato.getFormat("#,##0.00"));
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }

    public CellStyle crearEstiloNormal(SXSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setAlignment(HorizontalAlignment.LEFT);
        return estilo;
    }

    public CellStyle crearEstiloSemaforo(SXSSFWorkbook workbook, String nivel) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        switch (nivel != null ? nivel.toUpperCase() : "NORMAL") {
            case "CRITICO" -> {
                estilo.setFillForegroundColor(IndexedColors.RED.getIndex());
                fuente.setColor(IndexedColors.WHITE.getIndex());
            }
            case "MODERADO" -> {
                estilo.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                fuente.setColor(IndexedColors.BLACK.getIndex());
            }
            case "LEVE" -> {
                estilo.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                fuente.setColor(IndexedColors.BLACK.getIndex());
            }
            default -> {
                estilo.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                fuente.setColor(IndexedColors.BLACK.getIndex());
            }
        }
        estilo.setFont(fuente);
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    public CellStyle crearEstiloAlerta(SXSSFWorkbook workbook, boolean alterado) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        if (alterado) {
            fuente.setColor(IndexedColors.RED.getIndex());
            fuente.setBold(true);
        }
        estilo.setFont(fuente);
        return estilo;
    }

    public void aplicarAutoFiltro(Sheet hoja, int filaCabecera, int ultimaColumna) {
        if (hoja instanceof org.apache.poi.xssf.streaming.SXSSFSheet) {
            ((org.apache.poi.xssf.streaming.SXSSFSheet) hoja).setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            filaCabecera, filaCabecera, 0, ultimaColumna));
        }
    }

    public void congelarPrimeraFila(Sheet hoja) {
        hoja.createFreezePane(0, 1);
    }
}
