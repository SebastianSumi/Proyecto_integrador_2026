package pe.edu.upeu.saludablemente.alertas_clinicas.motor_evaluacion_base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionNutricionalDTO {
    private Long idEvaluacion;
    private Long idPersona;
    private Map<String, BigDecimal> mediciones;

    // Métodos de conveniencia
    public BigDecimal getPeso() {
        return mediciones != null ? mediciones.get("peso") : null;
    }

    public BigDecimal getEstatura() {
        return mediciones != null ? mediciones.get("estatura") : null;
    }

    public BigDecimal getGrasaVisceral() {
        return mediciones != null ? mediciones.get("grasa_visceral") : null;
    }

    public BigDecimal getGlucosa() {
        return mediciones != null ? mediciones.get("glucosa") : null;
    }

    public BigDecimal getColesterolTotal() {
        return mediciones != null ? mediciones.get("colesterol_total") : null;
    }

    public BigDecimal getColesterolHdl() {
        return mediciones != null ? mediciones.get("colesterol_hdl") : null;
    }

    public BigDecimal getColesterolLdl() {
        return mediciones != null ? mediciones.get("colesterol_ldl") : null;
    }

    public BigDecimal getTrigliceridos() {
        return mediciones != null ? mediciones.get("trigliceridos") : null;
    }

    public BigDecimal getPresionSistolica() {
        return mediciones != null ? mediciones.get("presion_sistolica") : null;
    }

    public BigDecimal getPresionDiastolica() {
        return mediciones != null ? mediciones.get("presion_diastolica") : null;
    }
}