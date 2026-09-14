package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class MultiplicadorReincidenciaService {

    private static final BigDecimal MULTIPLICADOR_SIN_REINCIDENCIA = BigDecimal.ONE;
    private static final BigDecimal MULTIPLICADOR_UNA_REINCIDENCIA = new BigDecimal("1.3");
    private static final BigDecimal MULTIPLICADOR_DOS_REINCIDENCIAS = new BigDecimal("1.5");
    private static final BigDecimal MULTIPLICADOR_TRES_O_MAS_REINCIDENCIAS = new BigDecimal("2.0");

    public BigDecimal calcularMultiplicadorReincidencia(IndicadorEvaluadoDTO indicadorActual,
                                                        List<IndicadorEvaluadoDTO> historico) {
        if (indicadorActual == null || historico == null || historico.isEmpty()) {
            return MULTIPLICADOR_SIN_REINCIDENCIA;
        }

        long reincidenciasAnteriores = historico.stream()
                .filter(i -> i.getTipoIndicador() == indicadorActual.getTipoIndicador())
                .filter(i -> Boolean.TRUE.equals(i.getEsAlterado()))
                .count();

        if (reincidenciasAnteriores <= 0) {
            return MULTIPLICADOR_SIN_REINCIDENCIA;
        }

        BigDecimal multiplicador;
        if (reincidenciasAnteriores >= 3) {
            multiplicador = MULTIPLICADOR_TRES_O_MAS_REINCIDENCIAS;
        } else if (reincidenciasAnteriores == 2) {
            multiplicador = MULTIPLICADOR_DOS_REINCIDENCIAS;
        } else {
            multiplicador = MULTIPLICADOR_UNA_REINCIDENCIA;
        }

        log.warn("Reincidencia detectada para {}: {} veces previas (multiplicador: {})",
                indicadorActual.getTipoIndicador(), reincidenciasAnteriores, multiplicador);

        return multiplicador;
    }

    public void aplicarMultiplicador(IndicadorEvaluadoDTO indicador, BigDecimal multiplicador) {
        if (indicador != null && multiplicador != null) {
            BigDecimal desviacionActual = indicador.getPorcentajeDesviacion();
            if (desviacionActual != null) {
                BigDecimal nuevaDesviacion = desviacionActual.multiply(multiplicador);
                indicador.setPorcentajeDesviacion(nuevaDesviacion);
                indicador.setMultiplicadorReincidencia(multiplicador);
            }
        }
    }
}
