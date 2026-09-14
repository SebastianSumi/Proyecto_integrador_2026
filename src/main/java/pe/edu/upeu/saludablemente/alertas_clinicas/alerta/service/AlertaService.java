package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service;

import org.springframework.data.domain.Page;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.*;

import java.util.List;
import java.util.UUID;

public interface AlertaService {

    AlertaDetalleResponseDTO crearAlerta(AlertaRequestDTO request);

    Page<AlertaInboxResponseDTO> obtenerBandejaInbox(FiltroInboxRequestDTO filtro);

    AlertaDetalleResponseDTO obtenerAlertaPorId(UUID idAlerta);

    AlertaAgregadoDTO obtenerResumenMetricas();

    List<AlertaPacienteResponseDTO> obtenerAlertasPaciente(Long idPersona);
}
