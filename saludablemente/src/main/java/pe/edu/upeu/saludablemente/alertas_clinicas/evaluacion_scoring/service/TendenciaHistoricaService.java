package pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TendenciaHistoricaService {

    public BigDecimal analizarTendencia(IndicadorEvaluadoDTO indicadorActual, List<IndicadorEvaluadoDTO> historico) {
        if (indicadorActual == null || historico == null || historico.isEmpty()) {
            return BigDecimal.ONE;
        }

        List<IndicadorEvaluadoDTO> historicoDelMismoTipo = historico.stream()
                .filter(i -> i.getTipoIndicador() == indicadorActual.getTipoIndicador())
                .collect(Collectors.toList());

        if (historicoDelMismoTipo.size() < 2) {
            return BigDecimal.ONE;
        }

        int size = historicoDelMismoTipo.size();
        IndicadorEvaluadoDTO anterior = historicoDelMismoTipo.get(size - 2);
        IndicadorEvaluadoDTO actual = historicoDelMismoTipo.get(size - 1);

        BigDecimal valorAnterior = anterior.getValorMedido();
        BigDecimal valorActual = actual.getValorMedido();

        if (valorAnterior == null || valorActual == null || valorAnterior.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }

        BigDecimal incremento = valorActual.subtract(valorAnterior)
                .divide(valorAnterior, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        if (incremento.compareTo(new BigDecimal("20")) >= 0 && Boolean.TRUE.equals(actual.getEsAlterado())) {
            if (!Boolean.TRUE.equals(anterior.getEsAlterado())) {
                log.warn("Degradación acelerada detectada para {}: incremento del {}%",
                        actual.getTipoIndicador(), incremento);
                return new BigDecimal("1.8");
            }
            if (Boolean.TRUE.equals(anterior.getEsAlterado())) {
                log.warn("Empeoramiento significativo detectado para {}: incremento del {}%",
                        actual.getTipoIndicador(), incremento);
                return new BigDecimal("1.5");
            }
        }

        if (incremento.compareTo(new BigDecimal("15")) >= 0 && !Boolean.TRUE.equals(actual.getEsAlterado())) {
            if (historicoDelMismoTipo.size() >= 3) {
                BigDecimal valorHaceDos = historicoDelMismoTipo.get(size - 3).getValorMedido();
                if (valorHaceDos != null && valorHaceDos.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal incrementoDos = valorAnterior.subtract(valorHaceDos)
                            .divide(valorHaceDos, 2, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    if (incrementoDos.compareTo(new BigDecimal("10")) >= 0 &&
                            incremento.compareTo(new BigDecimal("15")) >= 0) {
                        log.warn("Degradación silenciosa detectada para {}: tendencia al alza sostenida",
                                actual.getTipoIndicador());
                        return new BigDecimal("1.3");
                    }
                }
            }
        }

        return BigDecimal.ONE;
    }

    public List<IndicadorEvaluadoDTO> obtenerHistorial(Long idPersona, TipoIndicador tipoIndicador) {
        return List.of();
    }
}
