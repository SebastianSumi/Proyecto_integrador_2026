package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.edu.upeu.saludablemente.exportacion.config.SseEmitterConfig;
import pe.edu.upeu.saludablemente.exportacion.dto.ProgresoExportacionSseDTO;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportProgressNotifierImpl implements ExportProgressNotifier {

    private final SseEmitterConfig sseConfig;
    private final Map<UUID, SseEmitter> emisoresActivos = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> ultimoPorcentajeNotificado = new ConcurrentHashMap<>();

    @Override
    public SseEmitter crearEmitter(UUID idTarea) {
        SseEmitter emitter = new SseEmitter(sseConfig.getTimeoutMillis());

        emitter.onCompletion(() -> {
            log.debug("SSE completado para tarea {}", idTarea);
            emisoresActivos.remove(idTarea);
            ultimoPorcentajeNotificado.remove(idTarea);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout para tarea {}", idTarea);
            emisoresActivos.remove(idTarea);
            ultimoPorcentajeNotificado.remove(idTarea);
        });

        emitter.onError(e -> {
            log.warn("SSE error para tarea {}: {}", idTarea, e.getMessage());
            emisoresActivos.remove(idTarea);
            ultimoPorcentajeNotificado.remove(idTarea);
        });

        emisoresActivos.put(idTarea, emitter);
        log.debug("SSE emitter creado para tarea {}", idTarea);
        return emitter;
    }

    @Override
    public void notificarProgreso(UUID idTarea, ProgresoExportacionSseDTO progreso) {
        SseEmitter emitter = emisoresActivos.get(idTarea);
        if (emitter == null) {
            return;
        }

        int porcentajeActual = progreso.getPorcentajeAvance() != null
                ? progreso.getPorcentajeAvance() : 0;
        Integer ultimoNotificado = ultimoPorcentajeNotificado.get(idTarea);

        if (ultimoNotificado != null && porcentajeActual - ultimoNotificado < 5) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("progreso")
                    .data(progreso));
            ultimoPorcentajeNotificado.put(idTarea, porcentajeActual);
            log.debug("Progreso SSE {}% enviado para tarea {}", porcentajeActual, idTarea);
        } catch (IOException e) {
            log.warn("Error al emitir progreso SSE para tarea {}: {}", idTarea, e.getMessage());
            emisoresActivos.remove(idTarea);
        }
    }

    @Override
    public void notificarCompletado(UUID idTarea, ProgresoExportacionSseDTO progreso) {
        SseEmitter emitter = emisoresActivos.get(idTarea);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("completado").data(progreso));
            emitter.complete();
            log.debug("SSE completado enviado para tarea {}", idTarea);
        } catch (IOException e) {
            log.warn("Error al emitir completado para tarea {}: {}", idTarea, e.getMessage());
        } finally {
            emisoresActivos.remove(idTarea);
            ultimoPorcentajeNotificado.remove(idTarea);
        }
    }

    @Override
    public void notificarError(UUID idTarea, String mensajeError) {
        SseEmitter emitter = emisoresActivos.get(idTarea);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("error").data(mensajeError));
            emitter.complete();
        } catch (IOException e) {
            log.warn("Error al emitir error SSE para tarea {}: {}", idTarea, e.getMessage());
        } finally {
            emisoresActivos.remove(idTarea);
            ultimoPorcentajeNotificado.remove(idTarea);
        }
    }

    @Override
    public void cerrarEmitter(UUID idTarea) {
        SseEmitter emitter = emisoresActivos.remove(idTarea);
        ultimoPorcentajeNotificado.remove(idTarea);
        if (emitter != null) {
            emitter.complete();
        }
    }
}
