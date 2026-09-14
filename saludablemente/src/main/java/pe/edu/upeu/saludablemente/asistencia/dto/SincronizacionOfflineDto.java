package pe.edu.upeu.saludablemente.asistencia.dto;

import lombok.Data;
import java.util.List;

@Data
public class SincronizacionOfflineDto {
    private String dispositivoUuid;
    private List<AsistenciaDto> registrosOffline;
}