package pe.edu.upeu.saludablemente.nutricional.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleBioquimicoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;
import pe.edu.upeu.saludablemente.nutricional.service.EvaluacionNutricionalService;

import java.util.List;

@Tag(name = "Evaluacion Nutricional")
@RestController
@RequestMapping("/api/v1/evaluaciones-nutricionales")
@RequiredArgsConstructor
public class EvaluacionNutricionalController {

    private final EvaluacionNutricionalService evaluacionNutricionalService;

    @Operation(summary = "Lista las evaluaciones nutricionales; admite navegacion por persona")
    @GetMapping
    public ResponseEntity<List<EvaluacionNutricionalResponseDto>> listar(
            @RequestParam(required = false) Long personaId) {
        if (personaId != null) {
            return ResponseEntity.ok(evaluacionNutricionalService.listarPorPersona(personaId));
        }
        return ResponseEntity.ok(evaluacionNutricionalService.listar());
    }

    @Operation(summary = "Consulta una evaluacion nutricional por id")
    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionNutricionalResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(evaluacionNutricionalService.obtener(id));
    }

    @Operation(summary = "Registra una evaluacion nutricional con su detalle antropometrico y diagnostico IMC/OMRON")
    @PostMapping("/antropometria")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluacionNutricionalResponseDto registrarAntropometria(
            @Valid @RequestBody EvaluacionNutricionalRequestDto request) {
        return evaluacionNutricionalService.registrarAntropometria(request);
    }

    @Operation(summary = "Vincula los datos bioquimicos a una evaluacion existente y la marca como COMPLETA")
    @PostMapping("/{id}/bioquimico")
    public ResponseEntity<EvaluacionNutricionalResponseDto> vincularBioquimico(
            @PathVariable Long id, @Valid @RequestBody DetalleBioquimicoDto detalle) {
        return ResponseEntity.ok(evaluacionNutricionalService.vincularBioquimico(id, detalle));
    }

    @Operation(summary = "Elimina una evaluacion nutricional")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        evaluacionNutricionalService.eliminar(id);
    }
}