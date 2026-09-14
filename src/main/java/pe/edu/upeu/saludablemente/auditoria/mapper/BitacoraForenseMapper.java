package pe.edu.upeu.saludablemente.auditoria.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.auditoria.dto.CambioAtomicoViewDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.EventoAuditoriaDetalleDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BitacoraForenseMapper {

    private final ObjectMapper objectMapper;

    public BitacoraForenseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventoAuditoriaDetalleDTO toDetalleDTO(BitacoraTransaccionalEntity entity,
                                                   boolean verificarIntegridad) {
        if (entity == null) {
            return null;
        }

        return EventoAuditoriaDetalleDTO.builder()
                .idBitacora(entity.getIdBitacora())
                .secuencia(entity.getSecuencia())
                .tipoEvento(entity.getTipoEvento())
                .entidadAfectada(entity.getEntidadAfectada())
                .idEntidad(entity.getIdEntidad())
                .tipoOperacion(entity.getTipoOperacion() != null
                        ? entity.getTipoOperacion().name() : null)
                .idPersona(entity.getIdPersona())
                .usuarioAutor(entity.getUsuarioAutor())
                .rolUsuario(entity.getRolUsuario())
                .direccionIp(entity.getDireccionIp())
                .userAgent(entity.getUserAgent())
                .endpointHttp(entity.getEndpointHttp())
                .metodoHttp(entity.getMetodoHttp())
                .codigoRespuestaHttp(entity.getCodigoRespuestaHttp())
                .hashRegistro(entity.getHashRegistro())
                .hashAnterior(entity.getHashAnterior())
                .fueraHorarioLaboral("S".equalsIgnoreCase(entity.getFueraHorarioLaboral()))
                .fechaRegistro(entity.getFechaRegistro())
                .cambios(extraerCambios(entity.getDiferencialCambios()))
                .snapshotAnterior(deserializarMapa(entity.getSnapshotAnterior()))
                .snapshotPosterior(deserializarMapa(entity.getSnapshotPosterior()))
                .integridadVerificada(verificarIntegridad)
                .build();
    }

    public EventoAuditoriaDetalleDTO toDetalleDTOSinSnapshots(BitacoraTransaccionalEntity entity) {
        if (entity == null) {
            return null;
        }

        return EventoAuditoriaDetalleDTO.builder()
                .idBitacora(entity.getIdBitacora())
                .secuencia(entity.getSecuencia())
                .tipoEvento(entity.getTipoEvento())
                .entidadAfectada(entity.getEntidadAfectada())
                .idEntidad(entity.getIdEntidad())
                .tipoOperacion(entity.getTipoOperacion() != null
                        ? entity.getTipoOperacion().name() : null)
                .idPersona(entity.getIdPersona())
                .usuarioAutor(entity.getUsuarioAutor())
                .rolUsuario(entity.getRolUsuario())
                .direccionIp(entity.getDireccionIp())
                .userAgent(entity.getUserAgent())
                .endpointHttp(entity.getEndpointHttp())
                .metodoHttp(entity.getMetodoHttp())
                .codigoRespuestaHttp(entity.getCodigoRespuestaHttp())
                .hashRegistro(entity.getHashRegistro())
                .hashAnterior(entity.getHashAnterior())
                .fueraHorarioLaboral("S".equalsIgnoreCase(entity.getFueraHorarioLaboral()))
                .fechaRegistro(entity.getFechaRegistro())
                .cambios(extraerCambios(entity.getDiferencialCambios()))
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<CambioAtomicoViewDTO> extraerCambios(String diferencialJson) {
        List<CambioAtomicoViewDTO> cambios = new ArrayList<>();
        if (diferencialJson == null || diferencialJson.isBlank()) {
            return cambios;
        }

        try {
            Map<String, Object> diferencial = objectMapper.readValue(
                    diferencialJson, new TypeReference<Map<String, Object>>() {});

            Object cambiosObj = diferencial.get("cambios");
            if (cambiosObj instanceof List<?> lista) {
                for (Object item : lista) {
                    if (item instanceof Map<?, ?> mapItem) {
                        CambioAtomicoViewDTO cambio = CambioAtomicoViewDTO.builder()
                                .campo((String) mapItem.get("campo"))
                                .valorAnterior(mapItem.get("valorAnterior"))
                                .valorNuevo(mapItem.get("valorNuevo"))
                                .tipoCambio((String) mapItem.get("tipoCambio"))
                                .build();
                        cambios.add(cambio);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al parsear diferencial de cambios: {}", e.getMessage());
        }

        return cambios;
    }

    private Map<String, Object> deserializarMapa(String json) {
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
