package pe.edu.upeu.saludablemente.auditoria.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.dto.CambioAtomicoViewDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.TimelineEventoDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;
import pe.edu.upeu.saludablemente.auditoria.mapper.BitacoraForenseMapper;
import pe.edu.upeu.saludablemente.auditoria.repository.BitacoraTransaccionalRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineReconstructionServiceImpl implements TimelineReconstructionService {

    private final BitacoraTransaccionalRepository bitacoraRepository;
    private final BitacoraForenseMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEventoDTO> reconstruirTimeline(String entidadAfectada, String idEntidad) {
        if (entidadAfectada == null || idEntidad == null) {
            return List.of();
        }

        List<BitacoraTransaccionalEntity> registros = bitacoraRepository
                .findHistorialEntidad(entidadAfectada, idEntidad);

        List<TimelineEventoDTO> timeline = new ArrayList<>();

        for (BitacoraTransaccionalEntity registro : registros) {
            List<CambioAtomicoViewDTO> cambios = mapper.extraerCambios(registro.getDiferencialCambios());

            TimelineEventoDTO evento = TimelineEventoDTO.builder()
                    .idBitacora(registro.getIdBitacora())
                    .secuencia(registro.getSecuencia())
                    .tipoOperacion(registro.getTipoOperacion() != null
                            ? registro.getTipoOperacion().name() : null)
                    .usuarioAutor(registro.getUsuarioAutor())
                    .fechaRegistro(registro.getFechaRegistro())
                    .descripcionEvento(generarDescripcion(registro, cambios))
                    .cambios(cambios)
                    .build();

            timeline.add(evento);
        }

        return timeline;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> reconstruirEstadoEnFecha(String entidadAfectada, String idEntidad,
                                                        LocalDateTime fechaCorte) {
        if (entidadAfectada == null || idEntidad == null || fechaCorte == null) {
            return new HashMap<>();
        }

        List<BitacoraTransaccionalEntity> registros = bitacoraRepository
                .findHistorialEntidad(entidadAfectada, idEntidad);

        Map<String, Object> estadoReconstruido = new HashMap<>();

        for (BitacoraTransaccionalEntity registro : registros) {
            if (registro.getFechaRegistro().isAfter(fechaCorte)) {
                break;
            }

            if (TipoOperacion.INSERT.equals(registro.getTipoOperacion())) {
                Map<String, Object> snapshot = deserializarSnapshot(registro.getSnapshotPosterior());
                if (snapshot != null) {
                    estadoReconstruido = new HashMap<>(snapshot);
                }
            } else if (TipoOperacion.UPDATE.equals(registro.getTipoOperacion())) {
                List<CambioAtomicoViewDTO> cambios = mapper.extraerCambios(registro.getDiferencialCambios());
                for (CambioAtomicoViewDTO cambio : cambios) {
                    if (cambio.getValorNuevo() == null) {
                        estadoReconstruido.remove(cambio.getCampo());
                    } else {
                        estadoReconstruido.put(cambio.getCampo(), cambio.getValorNuevo());
                    }
                }
            } else if (TipoOperacion.DELETE.equals(registro.getTipoOperacion())) {
                estadoReconstruido.clear();
                estadoReconstruido.put("__estado__", "ENTIDAD_ELIMINADA");
                estadoReconstruido.put("__fechaEliminacion__", registro.getFechaRegistro().toString());
            }
        }

        estadoReconstruido.put("__fechaReconstruccion__", fechaCorte.toString());
        estadoReconstruido.put("__entidad__", entidadAfectada);
        estadoReconstruido.put("__idEntidad__", idEntidad);

        log.info("Estado reconstruido para {}:{} en fecha {}. Total eventos aplicados: {}",
                entidadAfectada, idEntidad, fechaCorte, registros.size());

        return estadoReconstruido;
    }

    private String generarDescripcion(BitacoraTransaccionalEntity registro,
                                      List<CambioAtomicoViewDTO> cambios) {
        return String.format("%s ejecuto %s sobre %s:%s (%d campos afectados)",
                registro.getUsuarioAutor(),
                registro.getTipoOperacion(),
                registro.getEntidadAfectada(),
                registro.getIdEntidad(),
                cambios.size());
    }

    private Map<String, Object> deserializarSnapshot(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Error al deserializar snapshot: {}", e.getMessage());
            return null;
        }
    }
}
