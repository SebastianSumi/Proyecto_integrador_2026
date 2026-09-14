package pe.edu.upeu.saludablemente.perfil_reporte.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.ErrorGeneracionPdfException;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PerfilDashboardResponseDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotJsonBuilderServiceImpl implements SnapshotJsonBuilderService {

    private final ObjectMapper objectMapper;

    @Override
    public String serializarSnapshotInmutable(PerfilDashboardResponseDTO dashboard,
                                              String periodoSemestral,
                                              String idReporte) {
        try {
            SnapshotDatosDTO snapshot = SnapshotDatosDTO.builder()
                    .filiacion(convertirAMap(dashboard.getFiliacion()))
                    .evaluacionActual(convertirAMap(dashboard.getEvaluacionActual()))
                    .analiticaHistorica(convertirAMap(dashboard.getAnaliticaHistorica()))
                    .semaforoBienestarGlobal(dashboard.getSemaforoBienestarGlobal())
                    .aptitudFisica(convertirAMap(dashboard.getAptitudFisica()))
                    .alertasActivas(convertirListaAMap(dashboard.getAlertasActivas()))
                    .recomendacionIA(convertirAMap(dashboard.getRecomendacionIA()))
                    .metas(convertirListaAMap(dashboard.getMetas()))
                    .agendaActividades(convertirListaAMap(dashboard.getAgendaActividades()))
                    .periodoSemestral(periodoSemestral)
                    .fechaGeneracion(LocalDateTime.now().toString())
                    .idReporte(idReporte)
                    .versionPlantilla("1.0")
                    .build();

            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.error("Error al serializar snapshot inmutable: {}", e.getMessage(), e);
            throw new ErrorGeneracionPdfException("Error al serializar snapshot clinico", e);
        }
    }

    @Override
    public SnapshotDatosDTO deserializarSnapshot(String jsonSnapshot) {
        try {
            return objectMapper.readValue(jsonSnapshot, SnapshotDatosDTO.class);
        } catch (Exception e) {
            log.error("Error al deserializar snapshot: {}", e.getMessage(), e);
            throw new ErrorGeneracionPdfException("Error al deserializar snapshot historico", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertirAMap(Object objeto) {
        if (objeto == null) return null;
        return objectMapper.convertValue(objeto, Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertirListaAMap(List<?> lista) {
        if (lista == null) return null;
        return lista.stream()
                .map(item -> (Map<String, Object>) objectMapper.convertValue(item, Map.class))
                .toList();
    }
}
