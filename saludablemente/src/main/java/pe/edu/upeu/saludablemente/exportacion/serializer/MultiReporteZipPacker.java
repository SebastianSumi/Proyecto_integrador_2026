package pe.edu.upeu.saludablemente.exportacion.serializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.ErrorSerializacionDatasetException;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiReporteZipPacker implements SerializadorFormato {

    private final CsvStreamingWriter csvWriter;
    private final ExcelWorkbookGenerator excelGenerator;
    private final PdfConsolidadoGenerator pdfGenerator;
    private final CodebookGenerator codebookGenerator;

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream out) {
        long inicio = System.currentTimeMillis();
        List<CohorteClinicaDTO> cohortes = datos.toList();
        log.info("Generando ZIP con {} colaboradores", cohortes.size());

        try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(out)) {
            zip.setEncoding("UTF-8");
            zip.setUseZip64(org.apache.commons.compress.archivers.zip.Zip64Mode.AsNeeded);

            // 1. Matriz de datos Excel
            ByteArrayOutputStream excelBaos = new ByteArrayOutputStream();
            excelGenerator.serializar(cohortes.stream(), opciones, excelBaos);
            escribirEntrada(zip, "00_RESUMEN/matriz_datos.xlsx", excelBaos.toByteArray());

            // 2. Matriz de datos CSV
            ByteArrayOutputStream csvBaos = new ByteArrayOutputStream();
            csvWriter.serializar(cohortes.stream(), opciones, csvBaos);
            escribirEntrada(zip, "00_RESUMEN/matriz_datos.csv", csvBaos.toByteArray());

            // 3. Reporte PDF Consolidado
            byte[] pdfConsolidado = pdfGenerator.generarBytes(cohortes.stream(), opciones);
            if (pdfConsolidado.length > 0) {
                escribirEntrada(zip, "00_RESUMEN/reporte_consolidado.pdf", pdfConsolidado);
            }

            // 4. Diccionario de datos
            ByteArrayOutputStream codebookBaos = new ByteArrayOutputStream();
            codebookGenerator.generarCodebookHtml("Lote Exportacion", "2026", "Sistema", codebookBaos);
            escribirEntrada(zip, "00_RESUMEN/diccionario_datos.html", codebookBaos.toByteArray());

            // 5. Metadatos del paquete
            String metaJson = String.format("{\"fecha\":\"%s\",\"totalRegistros\":%d}",
                    LocalDateTime.now(), cohortes.size());
            escribirEntrada(zip, "metadata_paquete.json", metaJson.getBytes(StandardCharsets.UTF_8));

            zip.finish();
            zip.flush();

        } catch (Exception e) {
            log.error("Error al generar ZIP: {}", e.getMessage(), e);
            throw new ErrorSerializacionDatasetException("ZIP_EXPEDIENTES",
                    "Error al generar el archivo ZIP", e);
        }

        long tiempoMs = System.currentTimeMillis() - inicio;
        log.info("ZIP generado: {} registros en {} ms", cohortes.size(), tiempoMs);

        return ResultadoSerializacionHelper.construir(
                FormatoSalida.ZIP_EXPEDIENTES, cohortes.size(), tiempoMs, null);
    }

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.ZIP_EXPEDIENTES;
    }

    private void escribirEntrada(ZipArchiveOutputStream zip, String nombreEntrada, byte[] datos) throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry(nombreEntrada);
        entry.setSize(datos.length);
        zip.putArchiveEntry(entry);
        zip.write(datos);
        zip.closeArchiveEntry();
    }
}
