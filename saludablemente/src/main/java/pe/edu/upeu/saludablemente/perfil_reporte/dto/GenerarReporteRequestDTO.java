package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarReporteRequestDTO {

    @NotNull(message = "El idPersona es obligatorio")
    private Long idPersona;

    @NotNull(message = "El periodo semestral es obligatorio")
    private String periodoSemestral;

    @Builder.Default
    private Boolean forzarRegeneracion = false;
}
