package pe.edu.upeu.saludablemente.auditoria.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auditoria.retencion")
public class AuditoriaRetentionConfig {

    private int diasRetencionCaliente = 365;
    private int mesesRetencionFrio = 60;
    private int aniosRetencionLegal = 5;
    private int aniosRetencionLogsTecnicos = 1;
    private String formatoCompresion = "zip";
    private int tamanoLoteArchivado = 100000;
    private boolean purgaAutomatica = true;
}
