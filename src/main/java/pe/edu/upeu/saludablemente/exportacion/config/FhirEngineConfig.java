package pe.edu.upeu.saludablemente.exportacion.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
public class FhirEngineConfig {

    @Value("${exportacion.fhir.enabled:true}")
    private boolean enabled;

    @Value("${exportacion.fhir.version:R4}")
    private String version;

    @Value("${exportacion.fhir.base-path:/fhir/r4}")
    private String basePath;
}
