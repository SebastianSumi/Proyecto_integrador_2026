package pe.edu.upeu.saludablemente.exportacion.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.edu.upeu.saludablemente.exportacion.dto.ProgresoExportacionSseDTO;

import java.util.UUID;

public interface ExportProgressNotifier {

    SseEmitter crearEmitter(UUID idTarea);

    void notificarProgreso(UUID idTarea, ProgresoExportacionSseDTO progreso);

    void notificarCompletado(UUID idTarea, ProgresoExportacionSseDTO progreso);

    void notificarError(UUID idTarea, String mensajeError);

    void cerrarEmitter(UUID idTarea);
}
