package pe.edu.upeu.saludablemente.perfil_reporte.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AlertaPacienteResponseDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AlertasClient {

    public List<AlertaPacienteResponseDTO> obtenerAlertasActivas(Long idPersona) {
        log.debug("Obteniendo alertas activas para persona {}", idPersona);
        List<AlertaPacienteResponseDTO> alertas = new ArrayList<>();
        alertas.add(AlertaPacienteResponseDTO.builder()
                .tipoIndicador("Grasa Visceral")
                .fechaGeneracion(LocalDateTime.now().minusDays(2))
                .mensajeAmigable("Tu nivel de grasa visceral se encuentra ligeramente elevado. "
                        + "El area de nutricion ha preparado pautas para ti.")
                .estadoAtencion("En revision por especialista")
                .build());
        return alertas;
    }
}
