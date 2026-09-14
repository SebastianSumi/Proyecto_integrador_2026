package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionClinicaResumenDTO {
    private Long idEvaluacion;
    private String periodo;
    private LocalDateTime fecha;

    private BigDecimal pesoKg;
    private BigDecimal tallaCm;
    private BigDecimal imc;
    private String diagnosticoImc;
    private BigDecimal perimetroAbdominalCm;

    private BigDecimal porcentajeGrasa;
    private Integer nivelGrasaVisceral;
    private BigDecimal porcentajeMusculo;

    private BigDecimal glucosaMgDl;
    private BigDecimal colesterolTotalMgDl;
    private BigDecimal colesterolHdlMgDl;
    private BigDecimal colesterolLdlMgDl;
    private BigDecimal trigliceridosMgDl;
    private String presionArterial;
    private Integer presionSistolica;
    private Integer presionDiastolica;
}
