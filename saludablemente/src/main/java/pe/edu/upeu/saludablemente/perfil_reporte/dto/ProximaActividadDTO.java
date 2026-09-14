package pe.edu.upeu.saludablemente.perfil_reporte.dto;

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
public class ProximaActividadDTO {
    private Long idActividad;
    private String titulo;
    private LocalDateTime fecha;
    private String modalidad;
    private String ubicacion;
}
