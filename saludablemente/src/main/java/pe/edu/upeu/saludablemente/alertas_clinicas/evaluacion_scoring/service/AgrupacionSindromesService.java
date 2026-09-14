package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.SindromeResultadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgrupacionSindromesService {

    private final MatrizSeveridadService matrizSeveridadService;

    public SindromeResultadoDTO detectarSindrome(List<IndicadorEvaluadoDTO> indicadoresAlterados) {
        if (indicadoresAlterados == null || indicadoresAlterados.isEmpty()) {
            return null;
        }

        boolean tieneGlucosa = tieneIndicador(indicadoresAlterados, TipoIndicador.GLUCOSA);
        boolean tieneTrigliceridos = tieneIndicador(indicadoresAlterados, TipoIndicador.TRIGLICERIDOS);
        boolean tienePresionSistolica = tieneIndicador(indicadoresAlterados, TipoIndicador.PRESION_SISTOLICA);
        boolean tienePresionDiastolica = tieneIndicador(indicadoresAlterados, TipoIndicador.PRESION_DIASTOLICA);
        boolean tieneColesterol = tieneIndicador(indicadoresAlterados, TipoIndicador.COLESTEROL_TOTAL);
        boolean tieneImc = tieneIndicador(indicadoresAlterados, TipoIndicador.IMC);

        // Síndrome Metabólico
        if (tieneGlucosa && tieneTrigliceridos && (tienePresionSistolica || tienePresionDiastolica)) {
            log.info("Síndrome Metabólico detectado: Glucosa + Triglicéridos + Presión Arterial");
            return SindromeResultadoDTO.builder()
                    .codigo("RIESGO_SINDROME_METABOLICO")
                    .descripcion("Síndrome Metabólico: alteración combinada de glucosa, triglicéridos y presión arterial")
                    .puntosExtra(matrizSeveridadService.getPuntosSindromeMetabolico())
                    .build();
        }

        // Riesgo Cardiovascular Compuesto
        if (tieneColesterol && (tienePresionSistolica || tienePresionDiastolica) && tieneImc) {
            log.info("Riesgo Cardiovascular detectado: Colesterol + Presión Arterial + IMC");
            return SindromeResultadoDTO.builder()
                    .codigo("RIESGO_CARDIOVASCULAR_COMPUESTO")
                    .descripcion("Riesgo Cardiovascular: alteración combinada de colesterol, presión arterial e IMC")
                    .puntosExtra(matrizSeveridadService.getPuntosSindromeCardiovascular())
                    .build();
        }

        // Riesgo Metabólico Leve
        if (tieneGlucosa && tieneTrigliceridos) {
            log.info("Riesgo Metabólico Leve: Glucosa + Triglicéridos");
            return SindromeResultadoDTO.builder()
                    .codigo("RIESGO_METABOLICO_LEVE")
                    .descripcion("Riesgo Metabólico Leve: alteración combinada de glucosa y triglicéridos")
                    .puntosExtra(5)
                    .build();
        }

        // Riesgo Cardiovascular Leve
        if (tieneColesterol && tieneImc) {
            log.info("Riesgo Cardiovascular Leve: Colesterol + IMC");
            return SindromeResultadoDTO.builder()
                    .codigo("RIESGO_CARDIOVASCULAR_LEVE")
                    .descripcion("Riesgo Cardiovascular Leve: alteración combinada de colesterol e IMC")
                    .puntosExtra(4)
                    .build();
        }

        return null;
    }

    private boolean tieneIndicador(List<IndicadorEvaluadoDTO> indicadores, TipoIndicador tipo) {
        return indicadores.stream()
                .anyMatch(i -> i.getTipoIndicador() == tipo && Boolean.TRUE.equals(i.getEsAlterado()));
    }
}
