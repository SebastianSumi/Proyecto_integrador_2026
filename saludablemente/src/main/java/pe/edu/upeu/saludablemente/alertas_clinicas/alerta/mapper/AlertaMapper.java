package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaDetalleResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaInboxResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.AlertaPacienteResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaDetalleEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AlertaMapper {

    @Mapping(target = "areaTrabajo", constant = "General")
    @Mapping(target = "tipoIndicador", source = "detalles", qualifiedByName = "mapTipoIndicadorPrincipal")
    @Mapping(target = "tiempoSlaRestanteHoras", source = "fechaVencimientoSla", qualifiedByName = "mapHorasRestantes")
    AlertaInboxResponseDTO toInboxDTO(AlertaClinicaEntity entity);

    @Mapping(target = "edad", ignore = true)
    @Mapping(target = "sexo", ignore = true)
    @Mapping(target = "sindromeDetectado", ignore = true)
    @Mapping(target = "detallesClinicos", source = "detalles")
    @Mapping(target = "horasSlaRestantes", source = "fechaVencimientoSla", qualifiedByName = "mapHorasRestantes")
    AlertaDetalleResponseDTO toDetalleDTO(AlertaClinicaEntity entity);

    @Mapping(target = "tipoIndicador", source = "detalles", qualifiedByName = "mapTipoIndicadorString")
    @Mapping(target = "mensajeAmigable", source = "entity", qualifiedByName = "mapMensajeAmigable")
    @Mapping(target = "estadoAtencion", expression = "java(entity.getEstado() != null ? entity.getEstado().name() : null)")
    @Mapping(target = "diasRestantesSla", source = "fechaVencimientoSla", qualifiedByName = "mapDiasRestantes")
    AlertaPacienteResponseDTO toPacienteDTO(AlertaClinicaEntity entity);

    @Mapping(target = "esAlterado", constant = "true")
    @Mapping(target = "diagnostico", ignore = true)
    @Mapping(target = "unidad", ignore = true)
    IndicadorEvaluadoDTO toIndicadorDTO(AlertaClinicaDetalleEntity detalle);

    List<IndicadorEvaluadoDTO> toIndicadoresDTOList(List<AlertaClinicaDetalleEntity> detalles);

    @Named("mapTipoIndicadorPrincipal")
    default TipoIndicador mapTipoIndicadorPrincipal(List<AlertaClinicaDetalleEntity> detalles) {
        if (detalles != null && !detalles.isEmpty()) {
            return detalles.get(0).getTipoIndicador();
        }
        return null;
    }

    @Named("mapTipoIndicadorString")
    default String mapTipoIndicadorString(List<AlertaClinicaDetalleEntity> detalles) {
        TipoIndicador tipo = mapTipoIndicadorPrincipal(detalles);
        return tipo != null ? tipo.name() : "GENERAL";
    }

    @Named("mapHorasRestantes")
    default Long mapHorasRestantes(LocalDateTime fechaVencimientoSla) {
        if (fechaVencimientoSla == null) return 0L;
        Duration duration = Duration.between(LocalDateTime.now(), fechaVencimientoSla);
        return Math.max(0L, duration.toHours());
    }

    @Named("mapDiasRestantes")
    default Long mapDiasRestantes(LocalDateTime fechaVencimientoSla) {
        if (fechaVencimientoSla == null) return 0L;
        Duration duration = Duration.between(LocalDateTime.now(), fechaVencimientoSla);
        return Math.max(0L, duration.toDays());
    }

    @Named("mapMensajeAmigable")
    default String mapMensajeAmigable(AlertaClinicaEntity entity) {
        if (entity == null) return "";
        TipoIndicador tipo = mapTipoIndicadorPrincipal(entity.getDetalles());
        String ind = tipo != null ? tipo.name().replace("_", " ").toLowerCase() : "salud";
        return "Tus niveles de " + ind + " requieren revisión médica preventiva para cuidar tu bienestar.";
    }
}
