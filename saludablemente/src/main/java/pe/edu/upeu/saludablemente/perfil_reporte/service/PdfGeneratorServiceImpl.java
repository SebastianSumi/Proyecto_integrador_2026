package pe.edu.upeu.saludablemente.perfil_reporte.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.ErrorGeneracionPdfException;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PdfGenerationResultDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PdfMetadataDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    private final HtmlTemplateRendererService htmlTemplateRendererService;
    private final ChartRendererService chartRendererService;
    private final QrCodeGeneratorService qrCodeGeneratorService;
    private final PdfDigitalSignerService pdfDigitalSignerService;

    public PdfGeneratorServiceImpl(HtmlTemplateRendererService htmlTemplateRendererService,
                                   ChartRendererService chartRendererService,
                                   QrCodeGeneratorService qrCodeGeneratorService,
                                   PdfDigitalSignerService pdfDigitalSignerService) {
        this.htmlTemplateRendererService = htmlTemplateRendererService;
        this.chartRendererService = chartRendererService;
        this.qrCodeGeneratorService = qrCodeGeneratorService;
        this.pdfDigitalSignerService = pdfDigitalSignerService;
    }

    @Override
    public PdfGenerationResultDTO generarPdf(SnapshotDatosDTO snapshot, PdfMetadataDTO metadata) {
        long inicio = System.currentTimeMillis();
        log.info("Generando PDF para persona {} periodo {}", metadata.getIdPersona(), metadata.getPeriodoSemestral());

        try {
            String hashDatos = pdfDigitalSignerService.calcularHashSha256(
                    snapshot.toString() + metadata.getIdReporte());
            String firmaDigital = pdfDigitalSignerService.generarFirmaDigital(hashDatos, metadata.getIdReporte());

            String urlVerificacion = "/api/v1/perfil/reportes/verificar/" + metadata.getIdReporte()
                    + "?hash=" + firmaDigital;
            String qrBase64 = qrCodeGeneratorService.generarQrBase64(urlVerificacion, 100, 100);

            String svgGraficos = generarGraficosSvg(snapshot);

            String html = htmlTemplateRendererService.renderizarHtml(snapshot, qrBase64, firmaDigital, svgGraficos);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();

            byte[] pdfBytes = baos.toByteArray();
            String hashPdf = pdfDigitalSignerService.calcularHashSha256(pdfBytes);
            long tiempo = System.currentTimeMillis() - inicio;

            log.info("PDF generado: {} bytes en {} ms", pdfBytes.length, tiempo);

            return PdfGenerationResultDTO.builder()
                    .pdfBytes(pdfBytes)
                    .tamanoBytes((long) pdfBytes.length)
                    .hashSha256(hashPdf)
                    .totalPaginas(estimarPaginas(pdfBytes))
                    .tiempoCompilacionMs(tiempo)
                    .build();

        } catch (Exception e) {
            log.error("Error al generar PDF: {}", e.getMessage(), e);
            throw new ErrorGeneracionPdfException("Error al compilar el PDF", e);
        }
    }

    @Override
    public void escribirPdfDirecto(SnapshotDatosDTO snapshot, PdfMetadataDTO metadata, OutputStream outputStream) {
        try {
            String hashDatos = pdfDigitalSignerService.calcularHashSha256(
                    snapshot.toString() + metadata.getIdReporte());
            String firmaDigital = pdfDigitalSignerService.generarFirmaDigital(hashDatos, metadata.getIdReporte());
            String urlVerificacion = "/api/v1/perfil/reportes/verificar/" + metadata.getIdReporte()
                    + "?hash=" + firmaDigital;
            String qrBase64 = qrCodeGeneratorService.generarQrBase64(urlVerificacion, 100, 100);
            String svgGraficos = generarGraficosSvg(snapshot);
            String html = htmlTemplateRendererService.renderizarHtml(snapshot, qrBase64, firmaDigital, svgGraficos);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception e) {
            log.error("Error al escribir PDF en stream: {}", e.getMessage(), e);
            throw new ErrorGeneracionPdfException("Error al escribir PDF", e);
        }
    }

    private String generarGraficosSvg(SnapshotDatosDTO snapshot) {
        StringBuilder svg = new StringBuilder();

        if (snapshot.getAptitudFisica() != null) {
            Map<String, Object> aptitud = snapshot.getAptitudFisica();
            svg.append(chartRendererService.generarRadarChartSvg(
                    "Perfil de Aptitud Fisica",
                    List.of("Abdominales", "Planchas", "Salto", "Carrera", "Flexibilidad"),
                    List.of(
                            toDouble(aptitud.get("abdominales1Min")),
                            toDouble(aptitud.get("planchas1Min")),
                            toDouble(aptitud.get("saltoSinImpulsoCm")),
                            toDouble(aptitud.get("carrera400mSegundos")),
                            50.0
                    )));
        }

        if (snapshot.getEvaluacionActual() != null) {
            Map<String, Object> ev = snapshot.getEvaluacionActual();
            svg.append(chartRendererService.generarBarChartSvg(
                    "Composicion Corporal",
                    List.of("Grasa %", "Musculo %", "Grasa Visceral"),
                    List.of(
                            toDouble(ev.get("porcentajeGrasa")),
                            toDouble(ev.get("porcentajeMusculo")),
                            toDouble(ev.get("nivelGrasaVisceral"))
                    )));
        }

        return svg.toString();
    }

    private double toDouble(Object valor) {
        if (valor == null) return 0.0;
        if (valor instanceof Number) return ((Number) valor).doubleValue();
        try {
            return Double.parseDouble(valor.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int estimarPaginas(byte[] pdfBytes) {
        String contenido = new String(pdfBytes, StandardCharsets.ISO_8859_1);
        int count = 0;
        int idx = 0;
        while ((idx = contenido.indexOf("/Type /Page", idx)) != -1) {
            count++;
            idx += 10;
        }
        return Math.max(count, 1);
    }
}
