package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AptitudFisicaResumenDTO;

import java.math.BigDecimal;

@Slf4j
@Component
public class AptitudFisicaClient {

    public AptitudFisicaResumenDTO obtenerUltimoRegistro(Long idPersona) {
        log.debug("Obteniendo aptitud fisica para persona {}", idPersona);
        return AptitudFisicaResumenDTO.builder()
                .abdominales1Min(28)
                .planchas1Min(22)
                .saltoSinImpulsoCm(new BigDecimal("35.0"))
                .saltoSogaReps(45)
                .carrera400mSegundos(new BigDecimal("92.0"))
                .nivelAptitud("INTERMEDIO")
                .build();
    }
}
