package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGeneradoResponseDTO {
    private UUID idReporte;
    private Long idPersona;
    private String periodoSemestral;
    private Integer versionReporte;
    private Long tamanoBytes;
    private String hashIntegridad;
    private LocalDateTime fechaGeneracion;
    private String urlDescarga;
}
