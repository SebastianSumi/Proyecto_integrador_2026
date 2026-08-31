package pe.edu.upeu.saludablemente.personal.persona.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaRequest;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaResponse;
import pe.edu.upeu.saludablemente.personal.persona.service.PersonaService;
import java.util.List;

@Tag(name = "Personas")
@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaController {
    private final PersonaService personaService;

    @Operation(summary = "Lista las personas registradas, opcionalmente filtradas por team")
    @GetMapping
    public ResponseEntity<List<PersonaResponse>> listar(@RequestParam(required = false) Long teamId) {
        if (teamId != null) {
            return ResponseEntity.ok(personaService.listarPorTeam(teamId));
        }
        return ResponseEntity.ok(personaService.listar());
    }

    @Operation(summary = "Consulta una persona por id")
    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtener(id));
    }

    @Operation(summary = "Registra una persona nueva")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaResponse crear(@Valid @RequestBody PersonaRequest request) {
        return personaService.crear(request);
    }

    @Operation(summary = "Actualiza una persona existente")
    @PutMapping("/{id}")
    public ResponseEntity<PersonaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.actualizar(id, request));
    }

    @Operation(summary = "Elimina una persona")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        personaService.eliminar(id);
    }
}
