package pe.edu.upeu.saludablemente.teams.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamRequest;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamResponse;
import pe.edu.upeu.saludablemente.teams.team.dto.TeamStateRequest;
import pe.edu.upeu.saludablemente.teams.team.service.TeamService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/equipos")
@Tag(name = "Equipos", description = "Gestión de equipos")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "Listar equipos", description = "Filtra opcionalmente por estado activo.")
    public List<TeamResponse> findAll(@RequestParam(required = false, name = "activo") Boolean active) {
        return teamService.findAll(active);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un equipo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    public TeamResponse findById(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un equipo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo registrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un equipo")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return teamService.update(id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de un equipo",
            description = "Define explícitamente si el equipo queda activo o inactivo.")
    public TeamResponse updateState(@PathVariable Long id, @Valid @RequestBody TeamStateRequest request) {
        return teamService.updateState(id, request);
    }
}
