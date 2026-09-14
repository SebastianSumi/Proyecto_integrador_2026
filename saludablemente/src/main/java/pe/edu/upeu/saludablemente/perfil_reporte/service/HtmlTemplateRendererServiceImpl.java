package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pe.edu.upeu.saludablemente.exception.ErrorGeneracionPdfException;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;

@Slf4j
@Service
public class HtmlTemplateRendererServiceImpl implements HtmlTemplateRendererService {

    private final TemplateEngine pdfTemplateEngine;

    public HtmlTemplateRendererServiceImpl(@Qualifier("pdfTemplateEngine") TemplateEngine pdfTemplateEngine) {
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    @Override
    public String renderizarHtml(SnapshotDatosDTO snapshot, String qrBase64, String hashSha256, String svgGraficos) {
        try {
            Context context = new Context();
            context.setVariable("filiacion", snapshot.getFiliacion());
            context.setVariable("evaluacion", snapshot.getEvaluacionActual());
            context.setVariable("analitica", snapshot.getAnaliticaHistorica());
            context.setVariable("semaforo", snapshot.getSemaforoBienestarGlobal());
            context.setVariable("aptitud", snapshot.getAptitudFisica());
            context.setVariable("alertas", snapshot.getAlertasActivas());
            context.setVariable("recomendacion", snapshot.getRecomendacionIA());
            context.setVariable("metas", snapshot.getMetas());
            context.setVariable("actividades", snapshot.getAgendaActividades());
            context.setVariable("periodo", snapshot.getPeriodoSemestral());
            context.setVariable("fechaGeneracion", snapshot.getFechaGeneracion());
            context.setVariable("idReporte", snapshot.getIdReporte());
            context.setVariable("qrBase64", qrBase64);
            context.setVariable("hashSha256", hashSha256);
            context.setVariable("svgGraficos", svgGraficos);

            return pdfTemplateEngine.process("reporte_colaborador", context);
        } catch (Exception e) {
            log.error("Error al renderizar plantilla HTML: {}", e.getMessage(), e);
            throw new ErrorGeneracionPdfException("Error al renderizar plantilla HTML", e);
        }
    }
}
