package pe.edu.upeu.saludablemente.auditoria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroAuditoriaForenseRequestDTO {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    private String usuarioAutor;
    private String entidadAfectada;
    private String idEntidad;
    private String tipoOperacion;
    private Long idPersona;
    private String campoAfectado;
    private String valorBuscado;
    private String direccionIp;
    private Boolean soloFueraHorarioLaboral;

    @Builder.Default
    private Integer pagina = 0;

    @Builder.Default
    private Integer limite = 50;

    @Builder.Default
    private String ordenarPor = "secuencia";

    @Builder.Default
    private String direccion = "ASC";
}
