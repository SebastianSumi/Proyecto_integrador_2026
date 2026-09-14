package pe.edu.upeu.saludablemente.exportacion.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
public class SseEmitterConfig {

    @Value("${exportacion.sse.timeout-minutos:30}")
    private long timeoutMinutos;

    public long getTimeoutMillis() {
        return timeoutMinutos * 60_000L;
    }
}
