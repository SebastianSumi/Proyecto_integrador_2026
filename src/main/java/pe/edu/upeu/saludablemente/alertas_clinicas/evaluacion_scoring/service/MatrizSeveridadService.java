package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.ScoreDetalleDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MatrizSeveridadService {

    private static final int PUNTOS_DESVIACION_LEVE = 1;
    private static final int PUNTOS_DESVIACION_MODERADA = 2;
    private static final int PUNTOS_DESVIACION_GRAVE = 3;
    private static final int PUNTOS_DESVIACION_CRITICA = 4;
    private static final int PUNTOS_SINDROME_METABOLICO = 10;
    private static final int PUNTOS_SINDROME_CARDIOVASCULAR = 8;

    public List<ScoreDetalleDTO> calcularScore(
            List<IndicadorEvaluadoDTO> indicadoresAlterados,
            List<ScoreDetalleDTO> detallesExistentes) {

        List<ScoreDetalleDTO> detalles = new ArrayList<>();
        int scoreTotal = 0;

        if (indicadoresAlterados != null) {
            for (IndicadorEvaluadoDTO indicador : indicadoresAlterados) {
                if (indicador == null || !Boolean.TRUE.equals(indicador.getEsAlterado())) {
                    continue;
                }

                BigDecimal desviacion = indicador.getPorcentajeDesviacion() != null ?
                        indicador.getPorcentajeDesviacion() : BigDecimal.ZERO;

                int puntosBase = calcularPuntosBase(desviacion);
                BigDecimal multiplicador = indicador.getMultiplicadorReincidencia() != null ?
                        indicador.getMultiplicadorReincidencia() : BigDecimal.ONE;

                int puntosFinales = (int) Math.round(puntosBase * multiplicador.doubleValue());

                String justificacion = generarJustificacion(indicador.getTipoIndicador(), desviacion, puntosBase);

                ScoreDetalleDTO detalle = ScoreDetalleDTO.builder()
                        .tipoIndicador(indicador.getTipoIndicador())
                        .valorMedido(indicador.getValorMedido())
                        .porcentajeDesviacion(desviacion)
                        .puntosBase(puntosBase)
                        .multiplicadorReincidencia(multiplicador)
                        .puntosFinales(puntosFinales)
                        .justificacion(justificacion)
                        .build();

                detalles.add(detalle);
                scoreTotal += puntosFinales;
                log.debug("Score para {}: {} puntos (base: {}, multi: {})",
                        indicador.getTipoIndicador(), puntosFinales, puntosBase, multiplicador);
            }
        }

        if (detallesExistentes != null) {
            detalles.addAll(detallesExistentes);
            scoreTotal += detallesExistentes.stream()
                    .mapToInt(ScoreDetalleDTO::getPuntosFinales)
                    .sum();
        }

        log.debug("Score total calculado: {}", scoreTotal);
        return detalles;
    }

    public NivelSeveridad determinarSeveridad(int scoreTotal) {
        if (scoreTotal >= 15) {
            return NivelSeveridad.CRITICO;
        } else if (scoreTotal >= 8) {
            return NivelSeveridad.MODERADO;
        } else {
            return NivelSeveridad.LEVE;
        }
    }

    public String obtenerNivelSeveridadConPuntaje(int scoreTotal) {
        NivelSeveridad nivel = determinarSeveridad(scoreTotal);
        return nivel.name() + " (" + scoreTotal + " puntos)";
    }

    public int calcularPuntosBase(BigDecimal desviacion) {
        if (desviacion == null || desviacion.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        if (desviacion.compareTo(new BigDecimal("50")) >= 0) {
            return PUNTOS_DESVIACION_CRITICA;
        } else if (desviacion.compareTo(new BigDecimal("30")) >= 0) {
            return PUNTOS_DESVIACION_GRAVE;
        } else if (desviacion.compareTo(new BigDecimal("15")) >= 0) {
            return PUNTOS_DESVIACION_MODERADA;
        } else {
            return PUNTOS_DESVIACION_LEVE;
        }
    }

    private String generarJustificacion(TipoIndicador tipo, BigDecimal desviacion, int puntos) {
        String descripcionTipo = tipo != null ? tipo.name().replace("_", " ").toLowerCase() : "indicador";
        return String.format("%s con desviación del %.1f%%: %d puntos",
                descripcionTipo, desviacion != null ? desviacion.doubleValue() : 0.0, puntos);
    }

    public int getPuntosSindromeMetabolico() {
        return PUNTOS_SINDROME_METABOLICO;
    }

    public int getPuntosSindromeCardiovascular() {
        return PUNTOS_SINDROME_CARDIOVASCULAR;
    }
}
