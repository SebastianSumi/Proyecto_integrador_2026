package pe.edu.upeu.saludablemente.aptitudfisica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CatalogoPruebaDto {

    private Long idPrueba;

    @NotBlank
    @Size(max = 100)
    private String nombrePrueba;

    @NotBlank
    @Size(max = 20)
    private String unidadMedida;

    @Size(max = 250)
    private String descripcion;

    private Boolean activo;
}