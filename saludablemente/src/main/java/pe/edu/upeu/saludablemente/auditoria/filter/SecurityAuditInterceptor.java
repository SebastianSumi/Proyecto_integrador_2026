package pe.edu.upeu.saludablemente.auditoria.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.edu.upeu.saludablemente.auditoria.context.AuditContextHolder;
import pe.edu.upeu.saludablemente.auditoria.dto.AuditContextDTO;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAuditInterceptor implements HandlerInterceptor {

    private static final Set<String> METODOS_MUTABLES = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String metodo = request.getMethod();

        if (!METODOS_MUTABLES.contains(metodo)) {
            return true;
        }

        try {
            AuditContextDTO context = construirContexto(request);
            AuditContextHolder.set(context);

            request.setAttribute("auditStartTime", System.currentTimeMillis());

        } catch (Exception e) {
            log.warn("Error al construir contexto de auditoria: {}", e.getMessage());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Object startAttr = request.getAttribute("auditStartTime");
            if (startAttr instanceof Long startTime) {
                long tiempoTotal = System.currentTimeMillis() - startTime;
                log.debug("Peticion {} {} finalizada con codigo {} en {} ms",
                        request.getMethod(), request.getRequestURI(),
                        response.getStatus(), tiempoTotal);
            }
        } finally {
            AuditContextHolder.clear();
        }
    }

    private AuditContextDTO construirContexto(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String usuario = "anonimo";
        String roles = "";
        String tenantId = "";

        if (auth != null && auth.isAuthenticated()) {
            usuario = auth.getName();
            roles = auth.getAuthorities().stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
        }

        return AuditContextDTO.builder()
                .usuario(usuario)
                .roles(roles)
                .tenantId(tenantId)
                .sessionId(request.getSession(false) != null ? request.getSession(false).getId() : null)
                .direccionIp(extraerIp(request))
                .puertoRemoto(String.valueOf(request.getRemotePort()))
                .userAgent(request.getHeader("User-Agent"))
                .uri(request.getRequestURI())
                .metodoHttp(request.getMethod())
                .build();
    }

    private String extraerIp(HttpServletRequest request) {
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
