package pe.edu.upeu.saludablemente.auditoria.service;

import jakarta.servlet.http.HttpServletRequest;
import pe.edu.upeu.saludablemente.auditoria.dto.HttpAuditMetadataDTO;

public interface HttpMetadataExtractorService {

    HttpAuditMetadataDTO extraerMetadata(HttpServletRequest request);

    String extraerIpCliente(HttpServletRequest request);
}
