package pe.edu.upeu.saludablemente.perfil_reporte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDashboardResponseDTO {
    private ColaboradorFiliacionDTO filiacion;
    private EvaluacionClinicaResumenDTO evaluacionActual;
    private AnaliticaLongitudinalDTO analiticaHistorica;
    private String semaforoBienestarGlobal;
    private AptitudFisicaResumenDTO aptitudFisica;
    private List<AlertaPacienteResponseDTO> alertasActivas;
    private RecomendacionVigenteDTO recomendacionIA;
    private List<MetaBienestarItemDTO> metas;
    private List<ProximaActividadDTO> agendaActividades;
}
