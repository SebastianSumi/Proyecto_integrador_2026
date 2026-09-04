package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class CalculadoraDesviacionService {

    /**
     * Calcula el porcentaje de desviación: ((ValorMedido - Limite) / Limite) * 100
     */
    public BigDecimal calcularDesviacion(BigDecimal valorMedido, BigDecimal limiteReferencia) {
        if (valorMedido == null || limiteReferencia == null || limiteReferencia.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (valorMedido.compareTo(limiteReferencia) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal diferencia = valorMedido.subtract(limiteReferencia);
        BigDecimal desviacion = diferencia.divide(limiteReferencia, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        return desviacion.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la desviación con redondeo a 2 decimales
     */
    public BigDecimal calcularDesviacionRedondeada(BigDecimal valorMedido, BigDecimal limiteReferencia) {
        BigDecimal desviacion = calcularDesviacion(valorMedido, limiteReferencia);
        return desviacion.setScale(2, RoundingMode.HALF_UP);
    }
}