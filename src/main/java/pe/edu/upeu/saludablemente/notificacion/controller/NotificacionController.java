package pe.edu.upeu.saludablemente.notificacion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.notificacion.dto.NotificacionDto;
import pe.edu.upeu.saludablemente.notificacion.service.NotificacionService;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired 
    private NotificacionService notificacionService;

    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<List<NotificacionDto>> listarPorPersona(@PathVariable Long idPersona) {
        return ResponseEntity.ok(notificacionService.listarPorPersona(idPersona));
    }

    @GetMapping("/persona/{idPersona}/estado")
    public ResponseEntity<List<NotificacionDto>> listarPorPersonaYEstado(
            @PathVariable Long idPersona, 
            @RequestParam Boolean leido) {
        return ResponseEntity.ok(notificacionService.listarPorPersonaYEstadoLectura(idPersona, leido));
    }

    @PostMapping
    public ResponseEntity<NotificacionDto> enviar(@RequestBody NotificacionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.enviarNotificacion(dto));
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionDto> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
