package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.perfil_reporte.client.ActividadesClient;
import pe.edu.upeu.saludablemente.perfil_reporte.client.AlertasClient;
import pe.edu.upeu.saludablemente.perfil_reporte.client.AptitudFisicaClient;
import pe.edu.upeu.saludablemente.perfil_reporte.client.EvaluacionClient;
import pe.edu.upeu.saludablemente.perfil_reporte.client.MetasClient;
import pe.edu.upeu.saludablemente.perfil_reporte.client.PersonalClient;
import pe.edu.upeu.saludablemente.perfil_reporte.client.RecomendacionClient;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AlertaPacienteResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AnaliticaLongitudinalDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.AptitudFisicaResumenDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ColaboradorFiliacionDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionClinicaResumenDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.EvaluacionHistoricoItemDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.MetaBienestarItemDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PerfilDashboardResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.ProximaActividadDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.RecomendacionVigenteDTO;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilDashboardServiceImpl implements PerfilDashboardService {

    private final PersonalClient personalClient;
    private final EvaluacionClient evaluacionClient;
    private final AptitudFisicaClient aptitudFisicaClient;
    private final AlertasClient alertasClient;
    private final RecomendacionClient recomendacionClient;
    private final MetasClient metasClient;
    private final ActividadesClient actividadesClient;
    private final AnaliticaLongitudinalService analiticaService;

    @Override
    @Transactional(readOnly = true)
    public PerfilDashboardResponseDTO consolidarDashboard(Long idPersona) {
        log.info("Consolidando dashboard para persona {}", idPersona);

        ColaboradorFiliacionDTO filiacion = personalClient.obtenerFiliacion(idPersona);
        EvaluacionClinicaResumenDTO evaluacionActual = evaluacionClient.obtenerUltimaEvaluacion(idPersona);
        List<EvaluacionHistoricoItemDTO> historial = evaluacionClient.obtenerHistorial(idPersona);
        AptitudFisicaResumenDTO aptitud = aptitudFisicaClient.obtenerUltimoRegistro(idPersona);
        List<AlertaPacienteResponseDTO> alertas = alertasClient.obtenerAlertasActivas(idPersona);
        RecomendacionVigenteDTO recomendacion = recomendacionClient.obtenerVigente(idPersona);
        List<MetaBienestarItemDTO> metas = metasClient.obtenerMetasActivas(idPersona);
        List<ProximaActividadDTO> actividades = actividadesClient.obtenerProximasActividades(idPersona);

        AnaliticaLongitudinalDTO analitica = analiticaService.procesarEvolucion(evaluacionActual, historial);
        String semaforo = analiticaService.calcularSemaforoGlobal(evaluacionActual, alertas);

        return PerfilDashboardResponseDTO.builder()
                .filiacion(filiacion)
                .evaluacionActual(evaluacionActual)
                .analiticaHistorica(analitica)
                .semaforoBienestarGlobal(semaforo)
                .aptitudFisica(aptitud)
                .alertasActivas(alertas)
                .recomendacionIA(recomendacion)
                .metas(metas)
                .agendaActividades(actividades)
                .build();
    }
}
