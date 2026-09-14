package pe.edu.upeu.saludablemente.auditoria.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.edu.upeu.saludablemente.auditoria.filter.SecurityAuditInterceptor;

@Configuration
@RequiredArgsConstructor
public class AuditoriaWebMvcConfig implements WebMvcConfigurer {

    private final SecurityAuditInterceptor securityAuditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityAuditInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auditoria/health",
                        "/api/auditoria/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
