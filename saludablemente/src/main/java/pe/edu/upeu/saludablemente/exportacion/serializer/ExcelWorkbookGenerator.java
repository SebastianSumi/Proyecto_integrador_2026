package pe.edu.upeu.saludablemente.exportacion.serializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.ErrorSerializacionDatasetException;
import pe.edu.upeu.saludablemente.exportacion.dto.*;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelWorkbookGenerator implements SerializadorFormato {

    private static final int VENTANA_MEMORIA_FILAS = 100;
    private final ExcelStyleFactory styleFactory;

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream out) {
        long inicio = System.currentTimeMillis();
        int totalRegistros = 0;

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(VENTANA_MEMORIA_FILAS)) {
            workbook.setCompressTempFiles(true);

            Sheet hojaDatos = workbook.createSheet("Datos Poblacionales");
            Sheet hojaResumen = workbook.createSheet("Resumen Ejecutivo");
            Sheet hojaAlertas = workbook.createSheet("Alertas Activas");

            String[] cabecerasDatos = construirCabecerasDatos();
            Row filaCabecera = hojaDatos.createRow(0);
            for (int i = 0; i < cabecerasDatos.length; i++) {
                Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(cabecerasDatos[i]);
                celda.setCellStyle(styleFactory.crearEstiloCabecera(workbook));
            }

            Row filaCabeceraAlertas = hojaAlertas.createRow(0);
            String[] cabecerasAlertas = {"Codigo Colaborador", "Nombre", "Indicador",
                    "Severidad", "Estado", "Fecha Generacion", "Mensaje"};
            for (int i = 0; i < cabecerasAlertas.length; i++) {
                Cell celda = filaCabeceraAlertas.createCell(i);
                celda.setCellValue(cabecerasAlertas[i]);
                celda.setCellStyle(styleFactory.crearEstiloCabecera(workbook));
            }

            int filaDatosActual = 1;
            int filaAlertasActual = 1;

            List<CohorteClinicaDTO> bufferResumen = new ArrayList<>();
            int maxBufferResumen = 200;

            Iterator<CohorteClinicaDTO> iterator = datos.iterator();
            while (iterator.hasNext()) {
                CohorteClinicaDTO cohorte = iterator.next();

                escribirFilaDatos(hojaDatos.createRow(filaDatosActual++), cohorte, workbook);

                if (cohorte.getAlertas() != null && !cohorte.getAlertas().isEmpty()) {
                    for (AlertaActivaDTO alerta : cohorte.getAlertas()) {
                        escribirFilaAlerta(hojaAlertas.createRow(filaAlertasActual++),
                                cohorte, alerta, workbook);
                    }
                }

                if (bufferResumen.size() < maxBufferResumen) {
                    bufferResumen.add(cohorte);
                }

                totalRegistros++;
                if (totalRegistros % 1000 == 0) {
                    log.debug("Excel: {} registros procesados", totalRegistros);
                }
            }

            hojaDatos.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                    0, 0, 0, cabecerasDatos.length - 1));
            hojaDatos.createFreezePane(0, 1);

            escribirHojaResumen(hojaResumen, bufferResumen, workbook, opciones);

            workbook.write(out);
            out.flush();
            workbook.dispose();

        } catch (Exception e) {
            log.error("Error al generar Excel: {}", e.getMessage(), e);
            throw new ErrorSerializacionDatasetException("MS_EXCEL_XLSX",
                    "Error al escribir el libro Excel", e);
        }

        long tiempoMs = System.currentTimeMillis() - inicio;
        log.info("Excel generado: {} registros en {} ms", totalRegistros, tiempoMs);

        return ResultadoSerializacionHelper.construir(
                FormatoSalida.MS_EXCEL_XLSX, totalRegistros, tiempoMs, null);
    }

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.MS_EXCEL_XLSX;
    }

    private void escribirFilaDatos(Row fila, CohorteClinicaDTO c, SXSSFWorkbook wb) {
        int col = 0;

        if (c.getFiliacion() != null) {
            FiliacionDTO f = c.getFiliacion();
            fila.createCell(col++).setCellValue(f.getCodigoColaborador() != null ? f.getCodigoColaborador() : "");
            fila.createCell(col++).setCellValue(f.getNombreCompleto() != null ? f.getNombreCompleto() : "");
            fila.createCell(col++).setCellValue(f.getAreaTrabajo() != null ? f.getAreaTrabajo() : "");
            fila.createCell(col++).setCellValue(f.getSede() != null ? f.getSede() : "");
            fila.createCell(col++).setCellValue(f.getEdad() != null ? f.getEdad() : 0);
            fila.createCell(col++).setCellValue(f.getSexo() != null ? f.getSexo() : "");
        } else {
            for (int i = 0; i < 6; i++) fila.createCell(col++).setCellValue("");
        }

        if (c.getAntropometria() != null) {
            AntropometriaDTO a = c.getAntropometria();
            escribirCeldaNumerica(fila, col++, a.getPesoKg(), wb);
            escribirCeldaNumerica(fila, col++, a.getTallaCm(), wb);
            escribirCeldaNumerica(fila, col++, a.getImc(), wb);
            fila.createCell(col++).setCellValue(a.getDiagnosticoImc() != null ? a.getDiagnosticoImc() : "");
            escribirCeldaNumerica(fila, col++, a.getPerimetroAbdominalCm(), wb);
            fila.createCell(col++).setCellValue(a.getDiagnosticoPerimetro() != null ? a.getDiagnosticoPerimetro() : "");
        } else {
            for (int i = 0; i < 6; i++) fila.createCell(col++).setCellValue("");
        }

        if (c.getComposicionCorporal() != null) {
            ComposicionCorporalDTO co = c.getComposicionCorporal();
            escribirCeldaNumerica(fila, col++, co.getPorcentajeGrasa(), wb);
            fila.createCell(col++).setCellValue(co.getDiagnosticoGrasa() != null ? co.getDiagnosticoGrasa() : "");
            escribirCeldaNumerica(fila, col++, co.getPorcentajeMusculo(), wb);
            escribirCeldaNumerica(fila, col++, co.getNivelGrasaVisceral(), wb);
            fila.createCell(col++).setCellValue(co.getDiagnosticoGrasaVisceral() != null ? co.getDiagnosticoGrasaVisceral() : "");
        } else {
            for (int i = 0; i < 5; i++) fila.createCell(col++).setCellValue("");
        }

        if (c.getBioquimica() != null) {
            BioquimicaHemodinamicaDTO b = c.getBioquimica();
            escribirCeldaNumerica(fila, col++, b.getGlucosaMgDl(), wb);
            escribirCeldaNumerica(fila, col++, b.getColesterolTotalMgDl(), wb);
            escribirCeldaNumerica(fila, col++, b.getTrigliceridosMgDl(), wb);
            fila.createCell(col++).setCellValue(b.getPresionSistolica() != null ? b.getPresionSistolica() : 0);
            fila.createCell(col++).setCellValue(b.getPresionDiastolica() != null ? b.getPresionDiastolica() : 0);
        } else {
            for (int i = 0; i < 5; i++) fila.createCell(col++).setCellValue("");
        }
    }

    private void escribirFilaAlerta(Row fila, CohorteClinicaDTO c,
                                    AlertaActivaDTO alerta, SXSSFWorkbook wb) {
        String codigo = c.getFiliacion() != null && c.getFiliacion().getCodigoColaborador() != null
                ? c.getFiliacion().getCodigoColaborador() : "";
        String nombre = c.getFiliacion() != null && c.getFiliacion().getNombreCompleto() != null
                ? c.getFiliacion().getNombreCompleto() : "";

        fila.createCell(0).setCellValue(codigo);
        fila.createCell(1).setCellValue(nombre);
        fila.createCell(2).setCellValue(alerta.getTipoIndicador() != null ? alerta.getTipoIndicador() : "");
        Cell celdaSeveridad = fila.createCell(3);
        celdaSeveridad.setCellValue(alerta.getSeveridad() != null ? alerta.getSeveridad() : "");
        celdaSeveridad.setCellStyle(styleFactory.crearEstiloSemaforo(wb, alerta.getSeveridad()));
        fila.createCell(4).setCellValue(alerta.getEstado() != null ? alerta.getEstado() : "");
        fila.createCell(5).setCellValue(alerta.getFechaGeneracion() != null ? alerta.getFechaGeneracion().toString() : "");
        fila.createCell(6).setCellValue(alerta.getMensajeAmigable() != null ? alerta.getMensajeAmigable() : "");
    }

    private void escribirCeldaNumerica(Row fila, int col, Double valor, SXSSFWorkbook wb) {
        Cell celda = fila.createCell(col);
        if (valor != null) {
            celda.setCellValue(valor);
            celda.setCellStyle(styleFactory.crearEstiloNumerico(wb));
        } else {
            celda.setCellValue("");
        }
    }

    private void escribirHojaResumen(Sheet hoja, List<CohorteClinicaDTO> muestra,
                                     SXSSFWorkbook wb, OpcionesExportacionDTO opciones) {
        int fila = 0;

        Row filaTitulo = hoja.createRow(fila++);
        Cell celdaTitulo = filaTitulo.createCell(0);
        celdaTitulo.setCellValue("RESUMEN EJECUTIVO");
        celdaTitulo.setCellStyle(styleFactory.crearEstiloTitulo(wb));

        fila++;
        Row filaFecha = hoja.createRow(fila++);
        filaFecha.createCell(0).setCellValue("Fecha de generacion:");
        filaFecha.createCell(1).setCellValue(java.time.LocalDateTime.now().toString());

        fila++;
        Row filaSubTitulo = hoja.createRow(fila++);
        Cell celdaSub = filaSubTitulo.createCell(0);
        celdaSub.setCellValue("Total registros en muestra: " + muestra.size());
        celdaSub.setCellStyle(styleFactory.crearEstiloSubTitulo(wb));
    }

    private String[] construirCabecerasDatos() {
        return new String[]{
                "Codigo", "Nombre Completo", "Area", "Sede", "Edad", "Sexo",
                "Peso (kg)", "Talla (cm)", "IMC", "Dx IMC", "Perimetro Abd (cm)", "Dx Perimetro",
                "Grasa (%)", "Dx Grasa", "Musculo (%)", "Grasa Visceral", "Dx Grasa Visceral",
                "Glucosa (mg/dL)", "Colesterol (mg/dL)", "Trigliceridos (mg/dL)",
                "Presion Sistolica", "Presion Diastolica"
        };
    }
}
