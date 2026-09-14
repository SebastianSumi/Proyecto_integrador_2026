package pe.edu.upeu.saludablemente.nutricional.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class DetalleBioquimicoDto {

    private Long idBioquimico;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal glucosa;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal colesterol;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal trigliceridos;

    @Min(value = 0)
    private Integer presionSistolica;

    @Min(value = 0)
    private Integer presionDiastolica;

    private String dxBioquimico;

    @Size(max = 200)
    private String archivoOrigen;

    private LocalDateTime fechaImportacion;

    private Long idUsuarioImportador;
}