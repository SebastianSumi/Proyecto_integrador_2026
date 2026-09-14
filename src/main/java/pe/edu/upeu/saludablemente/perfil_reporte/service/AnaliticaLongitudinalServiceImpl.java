package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AlertaPacienteResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AnaliticaLongitudinalDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionClinicaResumenDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionHistoricoItemDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PuntoHistoricoDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AnaliticaLongitudinalServiceImpl implements AnaliticaLongitudinalService {

    private static final BigDecimal IMC_NORMAL_MIN = new BigDecimal("18.5");
    private static final BigDecimal IMC_NORMAL_MAX = new BigDecimal("24.9");
    private static final BigDecimal PRESION_SISTOLICA_NORMAL = new BigDecimal("120");
    private static final BigDecimal PRESION_DIASTOLICA_NORMAL = new BigDecimal("80");
    private static final BigDecimal GLUCOSA_NORMAL = new BigDecimal("100");

    @Override
    public AnaliticaLongitudinalDTO procesarEvolucion(
            EvaluacionClinicaResumenDTO evaluacionActual,
            List<EvaluacionHistoricoItemDTO> evaluacionHistorica) {

        if (evaluacionActual == null || evaluacionHistorica == null || evaluacionHistorica.isEmpty()) {
            return AnaliticaLongitudinalDTO.builder()
                    .tendenciaMetabolica("SIN_DATOS_SUFICIENTES")
                    .puntosHistoricos(new ArrayList<>())
                    .build();
        }

        EvaluacionHistoricoItemDTO semestreAnterior = evaluacionHistorica.get(evaluacionHistorica.size() - 1);

        BigDecimal deltaImc = calcularDelta(evaluacionActual.getImc(), semestreAnterior.getImc());
        BigDecimal deltaGrasa = calcularDelta(
                evaluacionActual.getNivelGrasaVisceral() != null
                        ? BigDecimal.valueOf(evaluacionActual.getNivelGrasaVisceral()) : null,
                semestreAnterior.getNivelGrasaVisceral() != null
                        ? BigDecimal.valueOf(semestreAnterior.getNivelGrasaVisceral()) : null);
        BigDecimal deltaGlucosa = calcularDelta(evaluacionActual.getGlucosaMgDl(), semestreAnterior.getGlucosaMgDl());

        String tendencia = determinarTendencia(deltaImc, deltaGrasa, deltaGlucosa);

        List<PuntoHistoricoDTO> puntosHistoricos = new ArrayList<>();
        for (EvaluacionHistoricoItemDTO item : evaluacionHistorica) {
            puntosHistoricos.add(PuntoHistoricoDTO.builder()
                    .periodo(item.getPeriodo())
                    .imc(item.getImc())
                    .grasaVisceral(item.getNivelGrasaVisceral())
                    .glucosa(item.getGlucosaMgDl())
                    .build());
        }

        log.debug("Analitica procesada. Tendencia: {}, deltaIMC: {}, deltaGrasa: {}, deltaGlucosa: {}",
                tendencia, deltaImc, deltaGrasa, deltaGlucosa);

        return AnaliticaLongitudinalDTO.builder()
                .deltaImcSemestreAnterior(deltaImc)
                .deltaGrasaVisceralSemestreAnterior(deltaGrasa)
                .deltaGlucosaSemestreAnterior(deltaGlucosa)
                .tendenciaMetabolica(tendencia)
                .puntosHistoricos(puntosHistoricos)
                .build();
    }

    @Override
    public String calcularSemaforoGlobal(EvaluacionClinicaResumenDTO evaluacionActual,
                                         List<AlertaPacienteResponseDTO> alertas) {

        if (alertas != null && !alertas.isEmpty()) {
            boolean tieneCritica = alertas.stream()
                    .anyMatch(a -> a.getEstadoAtencion() != null
                            && a.getEstadoAtencion().toLowerCase().contains("critica"));

            if (tieneCritica) {
                return "ATENCION_REQUERIDA";
            }
            return "OBSERVACION";
        }

        if (evaluacionActual == null) {
            return "OBSERVACION";
        }

        boolean imcNormal = evaluacionActual.getImc() != null
                && evaluacionActual.getImc().compareTo(IMC_NORMAL_MIN) >= 0
                && evaluacionActual.getImc().compareTo(IMC_NORMAL_MAX) <= 0;

        boolean presionNormal = evaluacionActual.getPresionSistolica() != null
                && evaluacionActual.getPresionSistolica() < PRESION_SISTOLICA_NORMAL.intValue()
                && evaluacionActual.getPresionDiastolica() != null
                && evaluacionActual.getPresionDiastolica() < PRESION_DIASTOLICA_NORMAL.intValue();

        boolean glucosaNormal = evaluacionActual.getGlucosaMgDl() != null
                && evaluacionActual.getGlucosaMgDl().compareTo(GLUCOSA_NORMAL) < 0;

        if (imcNormal && presionNormal && glucosaNormal) {
            return "OPTIMO";
        }

        return "OBSERVACION";
    }

    private BigDecimal calcularDelta(BigDecimal valorActual, BigDecimal valorAnterior) {
        if (valorActual == null || valorAnterior == null) {
            return null;
        }
        return valorActual.subtract(valorAnterior).setScale(2, RoundingMode.HALF_UP);
    }

    private String determinarTendencia(BigDecimal deltaImc, BigDecimal deltaGrasa, BigDecimal deltaGlucosa) {
        int mejoras = 0;
        int deterioros = 0;

        if (deltaImc != null) {
            if (deltaImc.compareTo(BigDecimal.ZERO) < 0) mejoras++;
            else if (deltaImc.compareTo(BigDecimal.ZERO) > 0) deterioros++;
        }
        if (deltaGrasa != null) {
            if (deltaGrasa.compareTo(BigDecimal.ZERO) < 0) mejoras++;
            else if (deltaGrasa.compareTo(BigDecimal.ZERO) > 0) deterioros++;
        }
        if (deltaGlucosa != null) {
            if (deltaGlucosa.compareTo(BigDecimal.ZERO) < 0) mejoras++;
            else if (deltaGlucosa.compareTo(BigDecimal.ZERO) > 0) deterioros++;
        }

        if (mejoras > deterioros) {
            return mejoras >= 3 ? "MEJORA_SIGNIFICATIVA" : "MEJORA_LEVE";
        }
        if (deterioros > mejoras) {
            return deterioros >= 3 ? "DETERIORO_SIGNIFICATIVO" : "DETERIORO_LEVE";
        }
        return "ESTABLE";
    }
}
