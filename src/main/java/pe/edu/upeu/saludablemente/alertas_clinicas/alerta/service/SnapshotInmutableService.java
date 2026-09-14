package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.ScoreDetalleDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotInmutableService {

    private final ObjectMapper objectMapper;

    public String generarSnapshotJson(
            Long idPersona,
            Long idEvaluacionOrigen,
            NivelSeveridad nivelSeveridad,
            Integer scoreRiesgo,
            String sindromeDetectado,
            EstadoAlerta estadoInicial,
            List<IndicadorEvaluadoDTO> indicadores,
            List<ScoreDetalleDTO> scoreDetalles) {

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("idPersona", idPersona);
        snapshot.put("idEvaluacionOrigen", idEvaluacionOrigen);
        snapshot.put("nivelSeveridad", nivelSeveridad != null ? nivelSeveridad.name() : null);
        snapshot.put("scoreRiesgo", scoreRiesgo);
        snapshot.put("sindromeDetectado", sindromeDetectado);
        snapshot.put("estadoInicial", estadoInicial != null ? estadoInicial.name() : null);
        snapshot.put("fechaGeneracion", LocalDateTime.now().toString());

        if (scoreDetalles != null && !scoreDetalles.isEmpty()) {
            snapshot.put("detallesScore", scoreDetalles.stream()
                    .map(this::scoreDetalleToMap)
                    .toList());
        }

        if (indicadores != null && !indicadores.isEmpty()) {
            snapshot.put("indicadoresEvaluados", indicadores.stream()
                    .map(this::indicadorToMap)
                    .toList());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("versionSnapshot", "1.0");
        metadata.put("sistema", "SaludableMente - Alertas Clinicas LP2");
        snapshot.put("metadata", metadata);

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar snapshot JSON", e);
            return "{}";
        }
    }

    private Map<String, Object> scoreDetalleToMap(ScoreDetalleDTO detalle) {
        Map<String, Object> map = new HashMap<>();
        map.put("tipoIndicador", detalle.getTipoIndicador() != null ? detalle.getTipoIndicador().name() : null);
        map.put("valorMedido", detalle.getValorMedido());
        map.put("porcentajeDesviacion", detalle.getPorcentajeDesviacion());
        map.put("puntosBase", detalle.getPuntosBase());
        map.put("multiplicadorReincidencia", detalle.getMultiplicadorReincidencia());
        map.put("puntosFinales", detalle.getPuntosFinales());
        map.put("justificacion", detalle.getJustificacion());
        return map;
    }

    private Map<String, Object> indicadorToMap(IndicadorEvaluadoDTO indicador) {
        Map<String, Object> map = new HashMap<>();
        map.put("tipoIndicador", indicador.getTipoIndicador() != null ? indicador.getTipoIndicador().name() : null);
        map.put("valorMedido", indicador.getValorMedido());
        map.put("limiteReferencia", indicador.getLimiteReferencia());
        map.put("porcentajeDesviacion", indicador.getPorcentajeDesviacion());
        map.put("multiplicadorReincidencia", indicador.getMultiplicadorReincidencia());
        map.put("diagnostico", indicador.getDiagnostico());
        map.put("unidad", indicador.getUnidad());
        map.put("esAlterado", indicador.getEsAlterado());
        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> deserializarSnapshot(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error al deserializar snapshot", e);
            return new HashMap<>();
        }
    }
}
