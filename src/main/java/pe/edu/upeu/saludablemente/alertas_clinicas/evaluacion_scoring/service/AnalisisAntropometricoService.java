package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.catalog.OmronReferenceCatalog;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalisisAntropometricoService {

    private final OmronReferenceCatalog omronCatalog;
    private final CalculadoraDesviacionService calculadoraDesviacion;

    /**
     * Evalúa el IMC (Índice de Masa Corporal = peso / estatura^2)
     */
    public IndicadorEvaluadoDTO evaluarImc(BigDecimal peso, BigDecimal estatura) {
        if (peso == null || estatura == null || estatura.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Datos insuficientes para calcular IMC: peso={}, estatura={}", peso, estatura);
            return null;
        }

        // Si estatura se envía en centímetros (ej. 175), convertir a metros (1.75)
        BigDecimal estaturaMetros = estatura;
        if (estatura.compareTo(new BigDecimal("3.0")) > 0) {
            estaturaMetros = estatura.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        BigDecimal imc = peso.divide(estaturaMetros.pow(2), 2, RoundingMode.HALF_UP);
        BigDecimal limiteReferencia = omronCatalog.getImcNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(imc, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.IMC)
                .valorMedido(imc)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("kg/m²");

        if (imc.compareTo(omronCatalog.getImcNormalMin()) < 0) {
            builder.diagnostico("BAJO_PESO").esAlterado(true);
        } else if (imc.compareTo(omronCatalog.getImcNormalMax()) <= 0) {
            builder.diagnostico("NORMAL").esAlterado(false);
        } else if (imc.compareTo(omronCatalog.getImcSobrepesoMax()) <= 0) {
            builder.diagnostico("SOBREPESO").esAlterado(true);
        } else {
            builder.diagnostico("OBESIDAD").esAlterado(true);
        }

        return builder.build();
    }

    /**
     * Evalúa el nivel de grasa visceral según sexo
     */
    public IndicadorEvaluadoDTO evaluarGrasaVisceral(String sexo, BigDecimal grasaVisceral) {
        if (grasaVisceral == null || sexo == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getGrasaVisceralNormalMax(sexo);
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(grasaVisceral, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.GRASA_VISCERAL)
                .valorMedido(grasaVisceral)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("Nivel")
                .esAlterado(false)
                .diagnostico("NORMAL");

        if (grasaVisceral.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            BigDecimal obesidadMin = omronCatalog.getGrasaVisceralObesidadMin(sexo);
            if (grasaVisceral.compareTo(obesidadMin) >= 0) {
                builder.diagnostico("OBESIDAD_VISCERAL_ALTA");
            } else {
                builder.diagnostico("EXCESO_GRASA_VISCERAL");
            }
        }

        return builder.build();
    }
}
