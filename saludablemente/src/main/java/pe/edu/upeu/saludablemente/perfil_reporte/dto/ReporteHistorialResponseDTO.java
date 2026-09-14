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
public class ReporteHistorialResponseDTO {
    private UUID idReporte;
    private String periodoSemestral;
    private Integer versionReporte;
    private LocalDateTime fechaGeneracion;
    private String tamanoFormateado;
    private Boolean disponibleParaDescarga;
    private Boolean vigente;
}
