package pe.edu.upeu.saludablemente.auditoria.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ExportacionForenseRequestDTO {

    @NotNull(message = "Los filtros son obligatorios")
    private FiltroAuditoriaForenseRequestDTO filtros;

    @NotBlank(message = "El formato es obligatorio (CSV o JSON)")
    private String formato;

    @NotBlank(message = "La justificacion legal es obligatoria")
    private String justificacionLegal;

    private String solicitanteIdentificador;
}
