package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaPacienteResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.AlertaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/paciente/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas Paciente", description = "Endpoints seguros y amigables de consulta para el colaborador.")
public class AlertaPacienteController {

    private final AlertaService alertaService;

    @GetMapping("/{idPersona}")
    @Operation(summary = "Consultar alertas de paciente", description = "Devuelve lista de alertas con lenguaje accesible para el colaborador.")
    public ResponseEntity<List<AlertaPacienteResponseDTO>> obtenerAlertasPorPaciente(@PathVariable("idPersona") Long idPersona) {
        List<AlertaPacienteResponseDTO> alertas = alertaService.obtenerAlertasPaciente(idPersona);
        return ResponseEntity.ok(alertas);
    }
}
