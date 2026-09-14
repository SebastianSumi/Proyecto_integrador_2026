package pe.edu.upeu.saludablemente.exportacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;
import pe.edu.upeu.saludablemente.exportacion.enums.ModoPrivacidad;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarExportacionRequestDTO {

    @NotBlank
    @Size(max = 150)
    private String tituloLote;

    @NotNull
    private FormatoSalida formato;

    @NotNull
    private ModoPrivacidad modoPrivacidad;

    @Builder.Default
    private boolean cifrarConContrasena = false;

    @Size(min = 12, max = 64)
    private String contrasenaArchivo;

    @NotNull
    @Valid
    private FiltroPoblacionalParams filtros;
}
