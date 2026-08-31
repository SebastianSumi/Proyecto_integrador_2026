package pe.edu.upeu.saludablemente.personal.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.personal.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.personal.team.service.TeamService;
import java.util.List;

@Tag(name = "Teams")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "Lista los teams registrados")
    @GetMapping
    public ResponseEntity<List<TeamResponse>> listar() {
        return ResponseEntity.ok(teamService.listar());
    }

    @Operation(summary = "Consulta un team por id")
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.obtener(id));
    }

    @Operation(summary = "Registra un team nuevo")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse crear(@Valid @RequestBody TeamRequest request) {
        return teamService.crear(request);
    }

    @Operation(summary = "Actualiza un team existente")
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> actualizar(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(teamService.actualizar(id, request));
    }

    @Operation(summary = "Elimina un team")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        teamService.eliminar(id);
    }
}
