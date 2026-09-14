package pe.edu.upeu.saludablemente.exportacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.edu.upeu.saludablemente.exportacion.service.ExportProgressNotifier;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/exportaciones")
@RequiredArgsConstructor
@Tag(name = "Exportaciones SSE", description = "Canal de eventos en tiempo real para progreso de exportacion")
public class ExportacionSseController {

    private final ExportProgressNotifier progressNotifier;

    @GetMapping(value = "/{idTarea}/progreso-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Suscribirse al canal SSE de una tarea de exportacion")
    public SseEmitter suscribirProgreso(@PathVariable UUID idTarea) {
        log.info("Cliente suscrito al canal SSE de la tarea {}", idTarea);
        return progressNotifier.crearEmitter(idTarea);
    }
}
