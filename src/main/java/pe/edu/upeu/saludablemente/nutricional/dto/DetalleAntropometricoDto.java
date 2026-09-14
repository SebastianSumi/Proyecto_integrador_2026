package pe.edu.upeu.saludablemente.nutricional.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAntropometricoDto {

    private Long idAntropometrico;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal estaturaCm;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pesoKg;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal perimetroAbdominalCm;

    private BigDecimal imc;

    private BigDecimal porcentajeGrasa;
    private BigDecimal porcentajeMasaMuscular;
    private BigDecimal porcentajeGrasaVisceral;

    private String dxImc;
    private String dxPerimetroAbdominal;
    private String dxGrasa;
    private String dxMasaMuscular;
    private String dxGrasaVisceral;
}