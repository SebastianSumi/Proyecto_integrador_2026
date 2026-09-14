package pe.edu.upeu.saludablemente.perfil_reporte.service;

import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;

public interface HtmlTemplateRendererService {

    String renderizarHtml(SnapshotDatosDTO snapshot, String qrBase64, String hashSha256, String svgGraficos);
}
