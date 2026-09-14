package pe.edu.upeu.saludablemente.perfil_reporte.service;

import pe.edu.upeu.saludablemente.perfil_reporte.dto.PerfilDashboardResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;

public interface SnapshotJsonBuilderService {

    String serializarSnapshotInmutable(PerfilDashboardResponseDTO dashboard, String periodoSemestral, String idReporte);

    SnapshotDatosDTO deserializarSnapshot(String jsonSnapshot);
}
