package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.TimelineEventoDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TimelineReconstructionService {

    List<TimelineEventoDTO> reconstruirTimeline(String entidadAfectada, String idEntidad);

    Map<String, Object> reconstruirEstadoEnFecha(String entidadAfectada, String idEntidad,
                                                LocalDateTime fechaCorte);
}
