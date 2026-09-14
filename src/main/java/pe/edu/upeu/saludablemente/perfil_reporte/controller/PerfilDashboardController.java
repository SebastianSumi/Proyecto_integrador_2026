package pe.edu.upeu.saludablemente.perfil_reporte.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PerfilDashboardResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.service.PerfilDashboardService;

@Slf4j
@Tag(name = "Perfil Dashboard", description = "Dashboard consolidado del colaborador")
@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
public class PerfilDashboardController {

    private final PerfilDashboardService perfilDashboardService;

    @Operation(summary = "Obtiene el dashboard consolidado del colaborador")
    @GetMapping("/dashboard/{idPersona}")
    public ResponseEntity<PerfilDashboardResponseDTO> obtenerDashboard(@PathVariable Long idPersona) {
        log.info("Consultando dashboard para persona {}", idPersona);
        return ResponseEntity.ok(perfilDashboardService.consolidarDashboard(idPersona));
    }
}
