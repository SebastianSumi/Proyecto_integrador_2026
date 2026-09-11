package pe.edu.upeu.saludablemente.personal.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;
import pe.edu.upeu.saludablemente.personal.service.PersonaService;

import java.util.List;

@Tag(name = "Personal")
@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @Operation(summary = "Lista el personal registrado (por defecto, solo activos)")
    @GetMapping
    public ResponseEntity<List<PersonaResponseDto>> listar(
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(personaService.listar(soloActivos));
    }

    @Operation(summary = "Consulta a una persona por id")
    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponseDto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtener(id));
    }

    @Operation(summary = "Identifica a un colaborador por el hash de su credencial QR")
    @GetMapping("/qr/{qrHash}")
    public ResponseEntity<PersonaResponseDto> identificarPorQr(@PathVariable String qrHash) {
        return ResponseEntity.ok(personaService.identificarPorQr(qrHash));
    }

    @Operation(summary = "Registra un colaborador nuevo, generando su preferencia de comunicacion y credencial QR")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaResponseDto crear(@Valid @RequestBody PersonaRequestDto request) {
        return personaService.createPersona(request);
    }

    @Operation(summary = "Actualiza los datos de un colaborador existente")
    @PutMapping("/{id}")
    public ResponseEntity<PersonaResponseDto> actualizar(
            @PathVariable Long id, @Valid @RequestBody PersonaRequestDto request) {
        return ResponseEntity.ok(personaService.actualizar(id, request));
    }

    @Operation(summary = "Desactiva y anonimiza a una persona sin eliminar su historial")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarYAnonimizar(@PathVariable Long id) {
        personaService.desactivarYAnonimizar(id);
    }
}