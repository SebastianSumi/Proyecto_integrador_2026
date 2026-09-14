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
public class AnalisisHemodinamicoService {

    private final OmronReferenceCatalog omronCatalog;
    private final CalculadoraDesviacionService calculadoraDesviacion;

    /**
     * Evalúa presión arterial sistólica
     */
    public IndicadorEvaluadoDTO evaluarPresionSistolica(BigDecimal presionSistolica) {
        if (presionSistolica == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getPresionSistolicaNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(presionSistolica, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.PRESION_SISTOLICA)
                .valorMedido(presionSistolica)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("mmHg")
                .esAlterado(false)
                .diagnostico("NORMAL");

        if (presionSistolica.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            if (presionSistolica.compareTo(new BigDecimal("180")) >= 0) {
                builder.diagnostico("HIPERTENSION_GRADO_3");
            } else if (presionSistolica.compareTo(new BigDecimal("160")) >= 0) {
                builder.diagnostico("HIPERTENSION_GRADO_2");
            } else if (presionSistolica.compareTo(new BigDecimal("140")) >= 0) {
                builder.diagnostico("HIPERTENSION_GRADO_1");
            } else if (presionSistolica.compareTo(new BigDecimal("130")) >= 0) {
                builder.diagnostico("PREHIPERTENSION");
            } else {
                builder.diagnostico("PRESION_ELEVADA");
            }
        } else if (presionSistolica.compareTo(new BigDecimal("90")) <= 0) {
            builder.esAlterado(true);
            builder.diagnostico("HIPOTENSION");
        }

        return builder.build();
    }

    /**
     * Evalúa presión arterial diastólica
     */
    public IndicadorEvaluadoDTO evaluarPresionDiastolica(BigDecimal presionDiastolica) {
        if (presionDiastolica == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getPresionDiastolicaNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(presionDiastolica, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.PRESION_DIASTOLICA)
                .valorMedido(presionDiastolica)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("mmHg")
                .esAlterado(false)
                .diagnostico("NORMAL");

        if (presionDiastolica.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            if (presionDiastolica.compareTo(new BigDecimal("120")) >= 0) {
                builder.diagnostico("HIPERTENSION_GRADO_3");
            } else if (presionDiastolica.compareTo(new BigDecimal("100")) >= 0) {
                builder.diagnostico("HIPERTENSION_GRADO_2");
            } else if (presionDiastolica.compareTo(new BigDecimal("90")) >= 0) {
                builder.diagnostico("HIPERTENSION_GRADO_1");
            } else if (presionDiastolica.compareTo(new BigDecimal("85")) >= 0) {
                builder.diagnostico("PREHIPERTENSION");
            } else {
                builder.diagnostico("PRESION_ELEVADA");
            }
        } else if (presionDiastolica.compareTo(new BigDecimal("60")) <= 0) {
            builder.esAlterado(true);
            builder.diagnostico("HIPOTENSION");
        }

        return builder.build();
    }
}
