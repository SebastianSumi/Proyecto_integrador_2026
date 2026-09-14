package pe.edu.upeu.saludablemente.asistencia.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AsistenciaDto {
    private Long id;
    private Long idActividad;
    private Long idPersona;
    private LocalDateTime fechaHoraMarcado;
    private String metodo;
    private String estado;
    private Boolean esOffline;
    private String dispositivoUuid;
}