package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.catalog.OmronReferenceCatalog;
import pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto.PersonaDTO;
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
     * Evalúa el IMC (Índice de Masa Corporal)
     */
    public IndicadorEvaluadoDTO evaluarImc(PersonaDTO persona, BigDecimal peso, BigDecimal estatura) {
        if (peso == null || estatura == null || estatura.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Datos insuficientes para calcular IMC: peso={}, estatura={}", peso, estatura);
            return null;
        }

        // Calcular IMC: peso / (estatura^2)
        BigDecimal imc = peso.divide(estatura.pow(2), 2, RoundingMode.HALF_UP);
        BigDecimal limiteReferencia = omronCatalog.getImcNormalMax();
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(imc, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.IMC)
                .valorMedido(imc)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("kg/m²");

        // Determinar diagnóstico
        if (imc.compareTo(omronCatalog.getImcNormalMax()) <= 0) {
            builder.diagnostico("NORMAL").esAlterado(false);
        } else if (imc.compareTo(omronCatalog.getImcSobrepesoMin()) >= 0 &&
                imc.compareTo(omronCatalog.getImcSobrepesoMax()) <= 0) {
            builder.diagnostico("SOBREPESO").esAlterado(true);
        } else if (imc.compareTo(omronCatalog.getImcObesidadMin()) >= 0) {
            builder.diagnostico("OBESIDAD").esAlterado(true);
        } else {
            builder.diagnostico("BAJO_PESO").esAlterado(true);
        }

        return builder.build();
    }

    /**
     * Evalúa el porcentaje de grasa visceral
     */
    public IndicadorEvaluadoDTO evaluarGrasaVisceral(PersonaDTO persona, BigDecimal grasaVisceral) {
        if (grasaVisceral == null || persona == null) {
            return null;
        }

        BigDecimal limiteReferencia = omronCatalog.getGrasaVisceralNormalMax(persona.getSexo());
        BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(grasaVisceral, limiteReferencia);

        IndicadorEvaluadoDTO.IndicadorEvaluadoDTOBuilder builder = IndicadorEvaluadoDTO.builder()
                .tipoIndicador(TipoIndicador.GRASA_VISCERAL)
                .valorMedido(grasaVisceral)
                .limiteReferencia(limiteReferencia)
                .porcentajeDesviacion(desviacion)
                .unidad("Nivel")
                .esAlterado(false)
                .diagnostico("NORMAL");

        // Determinar diagnóstico por sexo
        if (grasaVisceral.compareTo(limiteReferencia) > 0) {
            builder.esAlterado(true);
            if (grasaVisceral.compareTo(new BigDecimal("15")) >= 0 && "M".equals(persona.getSexo())) {
                builder.diagnostico("OBESIDAD_VISCERAL_ALTA");
            } else if (grasaVisceral.compareTo(new BigDecimal("12")) >= 0 && "F".equals(persona.getSexo())) {
                builder.diagnostico("OBESIDAD_VISCERAL_ALTA");
            } else {
                builder.diagnostico("EXCESO_GRASA_VISCERAL");
            }
        }

        return builder.build();
    }
}