package pe.edu.upeu.saludablemente.auditoria.dto;

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
public class ParticionInfoDTO {
    private String nombreParticion;
    private String tablaOrigen;
    private LocalDateTime rangoFechaInicio;
    private LocalDateTime rangoFechaFin;
    private Long totalRegistros;
    private Long tamanoBytes;
    private String estado;
    private Boolean archivada;
    private Boolean purgada;
}
