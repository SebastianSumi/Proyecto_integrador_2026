package pe.edu.upeu.saludablemente.perfil_reporte.service;

import pe.edu.upeu.saludablemente.perfil_reporte.dto.GenerarReporteRequestDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteGeneradoResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ReporteHistorialResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ReportePersonalService {

    ReporteGeneradoResponseDTO generarReporte(GenerarReporteRequestDTO request, Long idUsuarioSolicitante);

    List<ReporteHistorialResponseDTO> obtenerHistorial(Long idPersona);

    void registrarDescarga(UUID idReporte, Long idPersonaSolicitante, String ip, String userAgent);

    boolean verificarIntegridad(UUID idReporte);
}
