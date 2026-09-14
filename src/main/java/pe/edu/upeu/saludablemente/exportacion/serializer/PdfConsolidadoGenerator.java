package pe.edu.upeu.saludablemente.exportacion.serializer;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.exception.ErrorSerializacionDatasetException;
import pe.edu.upeu.saludablemente.exportacion.dto.CohorteClinicaDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.FiliacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.OpcionesExportacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class PdfConsolidadoGenerator implements SerializadorFormato {

    @Override
    public ResultadoSerializacionDTO serializar(Stream<CohorteClinicaDTO> datos,
                                                 OpcionesExportacionDTO opciones,
                                                 OutputStream out) {
        long inicio = System.currentTimeMillis();
        int totalRegistros = 0;

        try {
            // Buffer initial records for the PDF summary/table
            List<CohorteClinicaDTO> lista = datos.limit(500).toList();
            totalRegistros = lista.size();

            String html = construirHtmlReporte(lista, opciones);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            out.flush();

        } catch (Exception e) {
            log.error("Error al generar PDF Consolidado: {}", e.getMessage(), e);
            throw new ErrorSerializacionDatasetException("PDF_CONSOLIDADO",
                    "Error al generar PDF Consolidado", e);
        }

        long tiempoMs = System.currentTimeMillis() - inicio;
        log.info("PDF Consolidado generado: {} registros en {} ms", totalRegistros, tiempoMs);

        return ResultadoSerializacionHelper.construir(
                FormatoSalida.PDF_CONSOLIDADO, totalRegistros, tiempoMs, 1);
    }

    @Override
    public FormatoSalida getFormatoSoportado() {
        return FormatoSalida.PDF_CONSOLIDADO;
    }

    public byte[] generarBytes(Stream<CohorteClinicaDTO> datos, OpcionesExportacionDTO opciones) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializar(datos, opciones, baos);
        return baos.toByteArray();
    }

    private String construirHtmlReporte(List<CohorteClinicaDTO> datos, OpcionesExportacionDTO opciones) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<style>");
        sb.append("@page { size: A4 landscape; margin: 15mm; }");
        sb.append("body { font-family: sans-serif; font-size: 10px; color: #333; }");
        sb.append("h1 { color: #1e5631; font-size: 18px; margin-bottom: 4px; }");
        sb.append(".subtitle { color: #555; margin-bottom: 12px; font-size: 11px; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        sb.append("th { background: #1e5631; color: white; padding: 6px 4px; text-align: left; font-size: 9px; }");
        sb.append("td { border-bottom: 1px solid #ddd; padding: 5px 4px; font-size: 9px; }");
        sb.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        sb.append(".badge { padding: 2px 5px; border-radius: 3px; font-weight: bold; font-size: 8px; }");
        sb.append(".normal { background: #e8f5e9; color: #2e7d32; }");
        sb.append(".alerta { background: #ffebee; color: #c62828; }");
        sb.append("</style></head><body>");

        sb.append("<h1>Reporte Consolidado de Población - Saludablemente</h1>");
        sb.append("<div class='subtitle'>Fecha de emisión: ").append(LocalDateTime.now())
          .append(" | Registros en muestra: ").append(datos.size()).append("</div>");

        sb.append("<table><thead><tr>");
        sb.append("<th>Código</th><th>Nombre</th><th>Área</th><th>Sede</th>");
        sb.append("<th>Edad</th><th>IMC</th><th>Glucosa</th><th>Colesterol</th><th>Grasa Visc.</th>");
        sb.append("</tr></thead><tbody>");

        for (CohorteClinicaDTO c : datos) {
            FiliacionDTO f = c.getFiliacion();
            sb.append("<tr>");
            sb.append("<td>").append(f != null && f.getCodigoColaborador() != null ? f.getCodigoColaborador() : "-").append("</td>");
            sb.append("<td>").append(f != null && f.getNombreCompleto() != null ? f.getNombreCompleto() : "-").append("</td>");
            sb.append("<td>").append(f != null && f.getAreaTrabajo() != null ? f.getAreaTrabajo() : "-").append("</td>");
            sb.append("<td>").append(f != null && f.getSede() != null ? f.getSede() : "-").append("</td>");
            sb.append("<td>").append(f != null && f.getEdad() != null ? f.getEdad() : "-").append("</td>");

            Double imc = c.getAntropometria() != null ? c.getAntropometria().getImc() : null;
            sb.append("<td>").append(imc != null ? String.format("%.1f", imc) : "-").append("</td>");

            Double glucosa = c.getBioquimica() != null ? c.getBioquimica().getGlucosaMgDl() : null;
            sb.append("<td>").append(glucosa != null ? String.format("%.0f", glucosa) : "-").append("</td>");

            Double col = c.getBioquimica() != null ? c.getBioquimica().getColesterolTotalMgDl() : null;
            sb.append("<td>").append(col != null ? String.format("%.0f", col) : "-").append("</td>");

            Double gv = c.getComposicionCorporal() != null ? c.getComposicionCorporal().getNivelGrasaVisceral() : null;
            sb.append("<td>").append(gv != null ? String.format("%.1f", gv) : "-").append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");
        sb.append("</body></html>");

        return sb.toString();
    }
}
