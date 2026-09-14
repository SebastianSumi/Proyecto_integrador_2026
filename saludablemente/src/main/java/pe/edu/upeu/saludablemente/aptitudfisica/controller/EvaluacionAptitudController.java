package pe.edu.upeu.saludablemente.aptitudfisica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudRequestDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResponseDto;
import pe.edu.upeu.saludablemente.aptitudfisica.service.EvaluacionAptitudService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Aptitud Fisica")
@RestController
@RequestMapping("/api/v1/evaluaciones-aptitud")
@RequiredArgsConstructor
public class EvaluacionAptitudController {

    private final EvaluacionAptitudService evaluacionAptitudService;

    @Operation(summary = "Lista las evaluaciones de aptitud fisica; admite filtros dinamicos (persona, sincronizacion y rango de fechas)")
    @GetMapping
    public ResponseEntity<List<EvaluacionAptitudResponseDto>> listar(
            @RequestParam(required = false) Long personaId,
            @RequestParam(required = false) Boolean sincronizado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(evaluacionAptitudService.listar(personaId, sincronizado, desde, hasta));
    }

    @Operation(summary = "Lista resumenes ligeros de evaluaciones de aptitud fisica (proyeccion DTO)")
    @GetMapping("/resumen")
    public ResponseEntity<List<EvaluacionAptitudResumenDto>> listarResumen(
            @RequestParam(required = false) Long personaId,
            @RequestParam(required = false) Boolean sincronizado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(evaluacionAptitudService.listarResumen(personaId, sincronizado, desde, hasta));
    }

    @Operation(summary = "Metricas de evaluaciones de aptitud fisica (total y suma de puntajes)")
    @GetMapping("/agregados")
    public ResponseEntity<EvaluacionAptitudAgregadoDto> agregados(
            @RequestParam(required = false) Long personaId) {
        return ResponseEntity.ok(evaluacionAptitudService.obtenerAgregados(personaId));
    }

    @Operation(summary = "Consulta una evaluacion de aptitud fisica por id")
    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionAptitudResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(evaluacionAptitudService.obtener(id));
    }

    @Operation(summary = "Registra una evaluacion (cabecera y detalle) con diagnostico automatico")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluacionAptitudResponseDto registrar(@Valid @RequestBody EvaluacionAptitudRequestDto request) {
        return evaluacionAptitudService.registrarEvaluacion(request);
    }

    @Operation(summary = "Elimina una evaluacion de aptitud fisica")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        evaluacionAptitudService.eliminar(id);
    }
}