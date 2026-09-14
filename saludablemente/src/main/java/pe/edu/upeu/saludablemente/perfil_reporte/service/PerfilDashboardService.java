package pe.edu.upeu.saludablemente.perfil_reporte.service;

import pe.edu.upeu.saludablemente.perfil_reporte.dto.PerfilDashboardResponseDTO;

public interface PerfilDashboardService {

    PerfilDashboardResponseDTO consolidarDashboard(Long idPersona);
}
