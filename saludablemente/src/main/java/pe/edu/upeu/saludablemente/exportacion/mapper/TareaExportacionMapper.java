package pe.edu.upeu.saludablemente.exportacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.saludablemente.exportacion.dto.EstadoExportacionResponseDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ProgresoExportacionSseDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.TareaHistorialItemDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.TareaIniciadaResponseDTO;
import pe.edu.upeu.saludablemente.exportacion.entity.LogInteroperabilidadFhirEntity;
import pe.edu.upeu.saludablemente.exportacion.entity.SuscriptorWebhookEntity;
import pe.edu.upeu.saludablemente.exportacion.entity.TareaExportacionEntity;

@Mapper(componentModel = "spring")
public interface TareaExportacionMapper {

    // ── TareaExportacionEntity → TareaIniciadaResponseDTO ──
    @Mapping(target = "estado", source = "estadoTarea")
    @Mapping(target = "mensaje", constant = "La tarea ha sido programada en el motor de lotes.")
    @Mapping(target = "endpointSseProgreso",
             expression = "java(\"/api/v1/exportaciones/\" + entity.getIdTarea() + \"/progreso-stream\")")
    TareaIniciadaResponseDTO toIniciadaResponse(TareaExportacionEntity entity);

    // ── TareaExportacionEntity → EstadoExportacionResponseDTO ──
    @Mapping(target = "estado", source = "estadoTarea")
    @Mapping(target = "formato", source = "formatoSalida")
    @Mapping(target = "modoPrivacidad", source = "modoPrivacidad")
    @Mapping(target = "estaCifrado",
             expression = "java(\"S\".equalsIgnoreCase(entity.getEstaCifrado()))")
    @Mapping(target = "urlDescargaDirecta",
             expression = "java(\"/api/v1/exportaciones/\" + entity.getIdTarea() + \"/descargar\")")
    @Mapping(target = "tiempoTotalProcesamientoSegundos",
             expression = "java(calcularTiempoTotal(entity))")
    @Mapping(target = "tamanoFormateado",
             expression = "java(formatearTamano(entity.getTamanoBytes()))")
    @Mapping(target = "totalRegistrosExportados", source = "totalRegistrosProcesados")
    @Mapping(target = "fechaInicio", source = "fechaInicioProceso")
    @Mapping(target = "urlPrefirmadaS3", ignore = true)
    @Mapping(target = "expiracionDescarga", ignore = true)
    EstadoExportacionResponseDTO toEstadoResponse(TareaExportacionEntity entity);

    // ── TareaExportacionEntity → TareaHistorialItemDTO ──
    @Mapping(target = "estado", source = "estadoTarea")
    @Mapping(target = "formato", source = "formatoSalida")
    @Mapping(target = "totalRegistros", source = "totalRegistrosProcesados")
    @Mapping(target = "disponibleParaDescarga",
             expression = "java(entity.estaCompletado() && entity.getRutaAlmacenamiento() != null)")
    @Mapping(target = "tamanoFormateado",
             expression = "java(formatearTamano(entity.getTamanoBytes()))")
    TareaHistorialItemDTO toHistorialItem(TareaExportacionEntity entity);

    // ── TareaExportacionEntity → ProgresoExportacionSseDTO ──
    @Mapping(target = "registrosProcesados", source = "totalRegistrosProcesados")
    @Mapping(target = "totalRegistros", source = "totalRegistrosEstimados")
    @Mapping(target = "tiempoEstimadoRestanteSegundos", ignore = true)
    @Mapping(target = "mensajeDetalle",
             expression = "java(construirMensajeDetalle(entity))")
    ProgresoExportacionSseDTO toProgresoSSE(TareaExportacionEntity entity);

    // ── SuscriptorWebhookEntity → SuscriptorWebhookDTO ──
    @Mapping(target = "eventosSuscritos",
             expression = "java(deserializarEventos(entity.getEventosSuscritos()))")
    @Mapping(target = "activo",
             expression = "java(\"S\".equalsIgnoreCase(entity.getActivo()))")
    pe.edu.upeu.saludablemente.exportacion.dto.SuscriptorWebhookDTO toSuscriptorDTO(SuscriptorWebhookEntity entity);

    // ── LogInteroperabilidadFhirEntity → FhirAccessLogDTO ──
    pe.edu.upeu.saludablemente.exportacion.dto.FhirAccessLogDTO toFhirAccessLogDTO(LogInteroperabilidadFhirEntity entity);

    // ═══════════════════════════════════════════════════════════
    // Métodos helper — usados por las expresiones de arriba
    // ═══════════════════════════════════════════════════════════

    default Long calcularTiempoTotal(TareaExportacionEntity entity) {
        if (entity.getFechaInicioProceso() == null || entity.getFechaFinalizacion() == null) {
            return null;
        }
        return java.time.Duration.between(
                entity.getFechaInicioProceso(),
                entity.getFechaFinalizacion()).getSeconds();
    }

    default String formatearTamano(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 B";
        }
        String[] unidades = {"B", "KB", "MB", "GB"};
        int i = 0;
        double tamano = bytes;
        while (tamano >= 1024 && i < unidades.length - 1) {
            tamano /= 1024;
            i++;
        }
        return String.format(java.util.Locale.US, "%.2f %s", tamano, unidades[i]);
    }

    default String construirMensajeDetalle(TareaExportacionEntity entity) {
        if (entity.getFaseActual() == null) {
            return "En cola...";
        }
        return switch (entity.getFaseActual()) {
            case EXTRAYENDO_DATOS -> "Extrayendo datos de los modulos fuente...";
            case APLICANDO_PRIVACIDAD -> "Aplicando nivel de privacidad solicitado...";
            case TRANSFORMANDO_FORMATO -> "Generando archivo en formato " + entity.getFormatoSalida();
            case CIFRANDO -> "Cifrando paquete con AES-256...";
            case ALMACENANDO -> "Almacenando paquete compilado...";
            case FINALIZADO -> "Paquete listo para descarga.";
        };
    }

    default java.util.List<String> deserializarEventos(String eventosJson) {
        return java.util.List.of();
    }
}
