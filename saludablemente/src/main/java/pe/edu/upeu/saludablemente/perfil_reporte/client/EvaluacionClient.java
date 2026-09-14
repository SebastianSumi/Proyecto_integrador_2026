package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionClinicaResumenDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionHistoricoItemDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EvaluacionClient {

    public EvaluacionClinicaResumenDTO obtenerUltimaEvaluacion(Long idPersona) {
        log.debug("Obteniendo ultima evaluacion para persona {}", idPersona);
        return EvaluacionClinicaResumenDTO.builder()
                .idEvaluacion(1L)
                .periodo("2026-2")
                .fecha(LocalDateTime.now())
                .pesoKg(new BigDecimal("82.5"))
                .tallaCm(new BigDecimal("170.0"))
                .imc(new BigDecimal("28.55"))
                .diagnosticoImc("Sobrepeso")
                .perimetroAbdominalCm(new BigDecimal("94.0"))
                .porcentajeGrasa(new BigDecimal("27.2"))
                .nivelGrasaVisceral(11)
                .porcentajeMusculo(new BigDecimal("32.1"))
                .glucosaMgDl(new BigDecimal("108.0"))
                .colesterolTotalMgDl(new BigDecimal("215.0"))
                .colesterolHdlMgDl(new BigDecimal("45.0"))
                .colesterolLdlMgDl(new BigDecimal("140.0"))
                .trigliceridosMgDl(new BigDecimal("180.0"))
                .presionSistolica(125)
                .presionDiastolica(85)
                .presionArterial("125/85")
                .build();
    }

    public List<EvaluacionHistoricoItemDTO> obtenerHistorial(Long idPersona) {
        log.debug("Obteniendo historial de evaluaciones para persona {}", idPersona);
        List<EvaluacionHistoricoItemDTO> historial = new ArrayList<>();
        historial.add(EvaluacionHistoricoItemDTO.builder()
                .periodo("2025-2")
                .imc(new BigDecimal("29.8"))
                .nivelGrasaVisceral(13)
                .glucosaMgDl(new BigDecimal("115.0"))
                .build());
        historial.add(EvaluacionHistoricoItemDTO.builder()
                .periodo("2026-1")
                .imc(new BigDecimal("29.0"))
                .nivelGrasaVisceral(12)
                .glucosaMgDl(new BigDecimal("110.0"))
                .build());
        historial.add(EvaluacionHistoricoItemDTO.builder()
                .periodo("2026-2")
                .imc(new BigDecimal("28.55"))
                .nivelGrasaVisceral(11)
                .glucosaMgDl(new BigDecimal("108.0"))
                .build());
        return historial;
    }
}
