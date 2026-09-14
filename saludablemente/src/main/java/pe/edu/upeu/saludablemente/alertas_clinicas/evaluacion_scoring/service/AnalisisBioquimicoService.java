package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.catalog.OmronReferenceCatalog;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalisisBioquimicoService {

    private final OmronReferenceCatalog omronCatalog;
    private final CalculadoraDesviacionService calculadoraDesviacion;

    /**
     * Evalúa niveles de glucosa
     */
    public IndicadorEvaluadoDTO evaluarGlucosa(BigDecimal glucosa) {
        if (glucosa == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getGlucosaNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(glucosa, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.GLUCOSA)
                .valorMedido(glucosa)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("mg/dL")
                .esAlterado(false)
                .diagnostico("NORMAL");

        if (glucosa.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            if (glucosa.compareTo(new BigDecimal("126")) >= 0) {
                builder.diagnostico("DIABETES");
            } else if (glucosa.compareTo(new BigDecimal("100")) >= 0) {
                builder.diagnostico("PRE_DIABETES");
            } else {
                builder.diagnostico("GLUCOSA_ALTERADA");
            }
        }

        return builder.build();
    }

    /**
     * Evalúa colesterol total
     */
    public IndicadorEvaluadoDTO evaluarColesterolTotal(BigDecimal colesterol) {
        if (colesterol == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getColesterolTotalNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(colesterol, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.COLESTEROL_TOTAL)
                .valorMedido(colesterol)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("mg/dL")
                .esAlterado(false)
                .diagnostico("NORMAL");

        if (colesterol.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            if (colesterol.compareTo(new BigDecimal("240")) >= 0) {
                builder.diagnostico("COLESTEROL_ALTO_RIESGO");
            } else {
                builder.diagnostico("COLESTEROL_LIMITE_ALTO");
            }
        }

        return builder.build();
    }

    /**
     * Evalúa triglicéridos
     */
    public IndicadorEvaluadoDTO evaluarTrigliceridos(BigDecimal trigliceridos) {
        if (trigliceridos == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getTrigliceridosNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(trigliceridos, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.TRIGLICERIDOS)
                .valorMedido(trigliceridos)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("mg/dL")
                .esAlterado(false)
                .diagnostico("NORMAL");

        if (trigliceridos.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            if (trigliceridos.compareTo(new BigDecimal("500")) >= 0) {
                builder.diagnostico("TRIGLICERIDOS_MUY_ALTOS");
            } else if (trigliceridos.compareTo(new BigDecimal("200")) >= 0) {
                builder.diagnostico("TRIGLICERIDOS_ALTOS");
            } else {
                builder.diagnostico("TRIGLICERIDOS_LIMITE_ALTO");
            }
        }

        return builder.build();
    }
}
