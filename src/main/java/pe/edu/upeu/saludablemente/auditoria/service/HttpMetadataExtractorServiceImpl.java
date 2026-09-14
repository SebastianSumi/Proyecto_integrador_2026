package pe.edu.upeu.saludablemente.auditoria.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.dto.HttpAuditMetadataDTO;

@Slf4j
@Service
public class HttpMetadataExtractorServiceImpl implements HttpMetadataExtractorService {

    @Override
    public HttpAuditMetadataDTO extraerMetadata(HttpServletRequest request) {
        return HttpAuditMetadataDTO.builder()
                .direccionIp(extraerIpCliente(request))
                .puertoRemoto(String.valueOf(request.getRemotePort()))
                .userAgent(request.getHeader("User-Agent"))
                .uri(request.getRequestURI())
                .metodoHttp(request.getMethod())
                .build();
    }

    @Override
    public String extraerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
