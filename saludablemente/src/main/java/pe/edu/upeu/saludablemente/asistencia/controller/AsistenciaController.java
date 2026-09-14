package pe.edu.upeu.saludablemente.asistencia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.asistencia.dto.AsistenciaDto;
import pe.edu.upeu.saludablemente.asistencia.dto.SincronizacionOfflineDto;
import pe.edu.upeu.saludablemente.asistencia.service.AsistenciaService;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping
    public ResponseEntity<List<AsistenciaDto>> listar() {
        return ResponseEntity.ok(asistenciaService.listarTodas());
    }

    @GetMapping("/actividad/{idActividad}")
    public ResponseEntity<List<AsistenciaDto>> listarPorActividad(@PathVariable Long idActividad) {
        return ResponseEntity.ok(asistenciaService.listarPorActividad(idActividad));
    }

    @PostMapping
    public ResponseEntity<AsistenciaDto> registrar(@RequestBody AsistenciaDto asistenciaDto) {
        return ResponseEntity.ok(asistenciaService.registrarAsistencia(asistenciaDto));
    }

    @PostMapping("/sincronizar-offline")
    public ResponseEntity<List<AsistenciaDto>> sincronizarOffline(@RequestBody SincronizacionOfflineDto payload) {
        List<AsistenciaDto> resultado = asistenciaService.sincronizarAsistenciasOffline(payload.getRegistrosOffline());
        return ResponseEntity.ok(resultado);
    }
}