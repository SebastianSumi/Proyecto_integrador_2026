package pe.edu.upeu.saludablemente.exportacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;
import pe.edu.upeu.saludablemente.auditoria.service.AuditoriaService;
import pe.edu.upeu.saludablemente.auditoria.service.HashLedgerService;
import pe.edu.upeu.saludablemente.exception.TareaExportacionNoEncontradaException;
import pe.edu.upeu.saludablemente.exportacion.dto.*;
import pe.edu.upeu.saludablemente.exportacion.entity.BitacoraDescargaExportacionEntity;
import pe.edu.upeu.saludablemente.exportacion.entity.TareaExportacionEntity;
import pe.edu.upeu.saludablemente.exportacion.enums.EstadoTarea;
import pe.edu.upeu.saludablemente.exportacion.enums.FaseExportacion;
import pe.edu.upeu.saludablemente.exportacion.enums.FormatoSalida;
import pe.edu.upeu.saludablemente.exportacion.repository.BitacoraDescargaExportacionRepository;
import pe.edu.upeu.saludablemente.exportacion.repository.TareaExportacionRepository;
import pe.edu.upeu.saludablemente.exportacion.storage.ExportStorageAdapter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class ExportacionBatchOrchestratorImpl implements ExportacionBatchOrchestrator {

    private static final long UMBRAL_SINCRONO = 1000;

    @Value("${exportacion.async.forzar-asincrono:false}")
    private boolean forzarAsincrono;

    private final TareaExportacionRepository tareaRepository;
    private final BitacoraDescargaExportacionRepository bitacoraDescargaRepository;
    private final ExtraccionPoblacionalService extraccionService;
    private final FormatDispatcherService formatDispatcher;
    private final ExportStorageAdapter storageAdapter;
    private final ExportPermissionService permissionService;
    private final ExportRateLimiter rateLimiter;
    private final PresignedUrlGenerator presignedUrlGenerator;
    private final ExportProgressNotifier progressNotifier;
    private final HashLedgerService hashLedgerService;
    private final AuditoriaService auditoriaService;
    private final WebhookDispatcherService webhookDispatcher;
    private final ObjectMapper objectMapper;

    public ExportacionBatchOrchestratorImpl(TareaExportacionRepository tareaRepository,
                                              BitacoraDescargaExportacionRepository bitacoraDescargaRepository,
                                              ExtraccionPoblacionalService extraccionService,
                                              FormatDispatcherService formatDispatcher,
                                              ExportStorageAdapter storageAdapter,
                                              ExportPermissionService permissionService,
                                              ExportRateLimiter rateLimiter,
                                              PresignedUrlGenerator presignedUrlGenerator,
                                              ExportProgressNotifier progressNotifier,
                                              HashLedgerService hashLedgerService,
                                              AuditoriaService auditoriaService,
                                              WebhookDispatcherService webhookDispatcher,
                                              ObjectMapper objectMapper) {
        this.tareaRepository = tareaRepository;
        this.bitacoraDescargaRepository = bitacoraDescargaRepository;
        this.extraccionService = extraccionService;
        this.formatDispatcher = formatDispatcher;
        this.storageAdapter = storageAdapter;
        this.permissionService = permissionService;
        this.rateLimiter = rateLimiter;
        this.presignedUrlGenerator = presignedUrlGenerator;
        this.progressNotifier = progressNotifier;
        this.hashLedgerService = hashLedgerService;
        this.auditoriaService = auditoriaService;
        this.webhookDispatcher = webhookDispatcher;
        this.objectMapper = objectMapper;
    }

    // ═══════════════════════════════════════════════════════════
    // INICIAR
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public TareaIniciadaResponseDTO iniciarExportacion(IniciarExportacionRequestDTO request,
                                                        Long idUsuario) {

        // 1. Permisos
        permissionService.verificarPermisos(idUsuario, request.getFiltros());

        // 2. Cuota
        rateLimiter.verificarCuota(idUsuario, request.getFiltros());

        // 3. Contar poblacion
        long totalEstimado = extraccionService.contarPoblacion(request.getFiltros());

        // 4. Crear entidad
        TareaExportacionEntity entidad = TareaExportacionEntity.builder()
                .idUsuarioSolicitante(idUsuario)
                .tituloLote(request.getTituloLote())
                .estadoTarea(EstadoTarea.EN_COLA)
                .formatoSalida(request.getFormato())
                .modoPrivacidad(request.getModoPrivacidad())
                .parametrosFiltro(serializarFiltros(request.getFiltros()))
                .porcentajeAvance(0)
                .totalRegistrosEstimados(totalEstimado)
                .estaCifrado(request.isCifrarConContrasena() ? "S" : "N")
                .fechaSolicitud(LocalDateTime.now())
                .build();

        TareaExportacionEntity guardada = tareaRepository.save(entidad);

        log.info("Tarea de exportacion creada: id={}, usuario={}, formato={}, totalEstimado={}",
                guardada.getIdTarea(), idUsuario, request.getFormato(), totalEstimado);

        // Auditoría síncrona: Creación de tarea
        auditoriaService.registrarCambio(
                "TAREA_EXPORTACION",
                guardada.getIdTarea().toString(),
                TipoOperacion.INSERT,
                null,
                Map.of("estado", guardada.getEstadoTarea().name(), "formato", guardada.getFormatoSalida().name()),
                idUsuario,
                "Usuario " + idUsuario
        );

        // 5. Decidir síncrono vs asíncrono
        boolean modoSincrono = !forzarAsincrono
                && totalEstimado <= UMBRAL_SINCRONO
                && esFormatoLigero(request.getFormato());

        if (modoSincrono) {
            log.info("Tarea {} procesada en modo SINCRONO", guardada.getIdTarea());
            procesarTareaSincrona(guardada.getIdTarea());
        } else {
            log.info("Tarea {} procesada en modo ASINCRONO", guardada.getIdTarea());
            procesarTareaAsincrona(guardada.getIdTarea());
        }

        // 6. Devolver respuesta
        return TareaIniciadaResponseDTO.builder()
                .idTarea(guardada.getIdTarea())
                .estado(guardada.getEstadoTarea().name())
                .fechaSolicitud(guardada.getFechaSolicitud())
                .totalRegistrosEstimados(totalEstimado)
                .endpointSseProgreso("/api/v1/exportaciones/"
                        + guardada.getIdTarea() + "/progreso-stream")
                .mensaje(modoSincrono
                        ? "La tarea fue procesada sincronicamente."
                        : "La tarea ha sido programada en el motor de lotes.")
                .build();
    }

    private boolean esFormatoLigero(FormatoSalida formato) {
        return formato == FormatoSalida.CSV_TABULAR
                || formato == FormatoSalida.JSON_ESTRUCTURADO;
    }

    // ═══════════════════════════════════════════════════════════
    // PROCESAR
    // ═══════════════════════════════════════════════════════════

    @Async("exportTaskExecutor")
    public void procesarTareaAsincrona(UUID idTarea) {
        try {
            procesarTarea(idTarea);
        } catch (Exception e) {
            log.error("Error en tarea asincrona {}: {}", idTarea, e.getMessage(), e);
            marcarFallida(idTarea, e);
        }
    }

    public void procesarTareaSincrona(UUID idTarea) {
        try {
            procesarTarea(idTarea);
        } catch (Exception e) {
            log.error("Error en tarea sincrona {}: {}", idTarea, e.getMessage(), e);
            marcarFallida(idTarea, e);
        }
    }

    @Override
    @Transactional
    public void procesarTarea(UUID idTarea) {
        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException(
                        "Tarea no encontrada: " + idTarea));

        // Marcar como PROCESANDO
        tarea.setEstadoTarea(EstadoTarea.PROCESANDO);
        tarea.setFaseActual(FaseExportacion.EXTRAYENDO_DATOS);
        tarea.setFechaInicioProceso(LocalDateTime.now());
        tarea.setPorcentajeAvance(0);
        tareaRepository.save(tarea);

        notificarProgreso(tarea, "Extrayendo datos...");

        try {
            // Filtros
            FiltroPoblacionalParams filtros = deserializarFiltros(tarea.getParametrosFiltro());

            // Extraer
            Stream<CohorteClinicaDTO> stream = extraccionService.extraerPoblacionStream(filtros);

            // Fase: APLICANDO_PRIVACIDAD
            actualizarFase(tarea, FaseExportacion.APLICANDO_PRIVACIDAD, 10);

            // Fase: TRANSFORMANDO_FORMATO
            actualizarFase(tarea, FaseExportacion.TRANSFORMANDO_FORMATO, 30);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            OpcionesExportacionDTO opciones = OpcionesExportacionDTO.builder()
                    .incluirDiccionario(true)
                    .build();

            ResultadoSerializacionDTO resultado = formatDispatcher.despachar(
                    stream, tarea.getFormatoSalida(), opciones, buffer);

            // Fase: CIFRANDO
            actualizarFase(tarea, FaseExportacion.CIFRANDO, 70);
            byte[] contenidoFinal = buffer.toByteArray();

            // Fase: ALMACENANDO
            actualizarFase(tarea, FaseExportacion.ALMACENANDO, 90);

            String nombreArchivo = construirNombreArchivo(tarea);
            storageAdapter.guardarArchivo(nombreArchivo, contenidoFinal);

            // Hash SHA-256 del paquete
            String hashSha256 = hashLedgerService.calcularHashSha256(contenidoFinal);

            // Completar
            ResultadoSerializacionDTO resultadoFinal = ResultadoSerializacionDTO.builder()
                    .rutaArchivoTemporal(nombreArchivo)
                    .nombreArchivoFinal(nombreArchivo)
                    .formato(tarea.getFormatoSalida())
                    .tamanioBytes(contenidoFinal.length)
                    .hashSha256(hashSha256)
                    .totalRegistros(resultado.getTotalRegistros())
                    .tiempoSerializacionMs(resultado.getTiempoSerializacionMs())
                    .build();

            completarTarea(idTarea, resultadoFinal);

        } catch (Exception e) {
            log.error("Error al procesar tarea {}: {}", idTarea, e.getMessage(), e);
            throw new RuntimeException("Error al procesar tarea de exportacion", e);
        }
    }

    @Override
    @Transactional
    public void completarTarea(UUID idTarea, ResultadoSerializacionDTO resultado) {
        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException(
                        "Tarea no encontrada: " + idTarea));

        tarea.setEstadoTarea(EstadoTarea.COMPLETADO);
        tarea.setFaseActual(FaseExportacion.FINALIZADO);
        tarea.setPorcentajeAvance(100);
        tarea.setTotalRegistrosProcesados((long) resultado.getTotalRegistros());
        tarea.setRutaAlmacenamiento(resultado.getRutaArchivoTemporal());
        tarea.setTamanoBytes(resultado.getTamanioBytes());
        tarea.setHashSha256(resultado.getHashSha256());
        tarea.setFechaFinalizacion(LocalDateTime.now());

        TareaExportacionEntity guardada = tareaRepository.save(tarea);

        // Auditoría: Tarea completada
        auditoriaService.registrarCambio(
                "TAREA_EXPORTACION",
                idTarea.toString(),
                TipoOperacion.UPDATE,
                Map.of("estado", EstadoTarea.PROCESANDO.name()),
                Map.of("estado", EstadoTarea.COMPLETADO.name(), "hashSha256", resultado.getHashSha256()),
                guardada.getIdUsuarioSolicitante(),
                "Sistema Batch"
        );

        // Generar URL prefirmada
        PresignedUrlDTO url = presignedUrlGenerator.generarUrl(
                guardada.getIdTarea(), guardada.getIdUsuarioSolicitante());

        ProgresoExportacionSseDTO progresoFinal = ProgresoExportacionSseDTO.builder()
                .idTarea(idTarea)
                .porcentajeAvance(100)
                .faseActual("FINALIZADO")
                .registrosProcesados((long) resultado.getTotalRegistros())
                .totalRegistros(guardada.getTotalRegistrosEstimados())
                .mensajeDetalle("Paquete listo. Descarga disponible.")
                .build();

        progressNotifier.notificarCompletado(idTarea, progresoFinal);

        // Despachar Webhook a sistemas suscritos
        try {
            webhookDispatcher.despacharEvento("EXPORTACION_COMPLETADA", WebhookPayloadDTO.builder()
                    .idTarea(idTarea)
                    .tituloLote(guardada.getTituloLote())
                    .formato(guardada.getFormatoSalida().name())
                    .totalRegistros(guardada.getTotalRegistrosProcesados())
                    .hashSha256(guardada.getHashSha256())
                    .urlDescarga(url.getUrl())
                    .fechaCompletado(guardada.getFechaFinalizacion())
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo emitir webhook para tarea {}: {}", idTarea, e.getMessage());
        }

        log.info("Tarea {} COMPLETADA: {} registros, {} bytes, expira {}",
                idTarea, resultado.getTotalRegistros(), resultado.getTamanioBytes(),
                url.getFechaExpiracion());
    }

    @Override
    @Transactional
    public void marcarFallida(UUID idTarea, Exception causa) {
        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException(
                        "Tarea no encontrada: " + idTarea));

        tarea.setEstadoTarea(EstadoTarea.FALLIDO);
        tarea.setMensajeError(causa.getMessage());
        tarea.setFechaFinalizacion(LocalDateTime.now());
        tareaRepository.save(tarea);

        auditoriaService.registrarCambio(
                "TAREA_EXPORTACION",
                idTarea.toString(),
                TipoOperacion.UPDATE,
                Map.of("estado", EstadoTarea.PROCESANDO.name()),
                Map.of("estado", EstadoTarea.FALLIDO.name(), "error", causa.getMessage() != null ? causa.getMessage() : "Error"),
                tarea.getIdUsuarioSolicitante(),
                "Sistema Batch"
        );

        progressNotifier.notificarError(idTarea, causa.getMessage());

        log.error("Tarea {} FALLIDA: {}", idTarea, causa.getMessage());
    }

    @Override
    @Transactional
    public void cancelarTarea(UUID idTarea) {
        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException(
                        "Tarea no encontrada: " + idTarea));

        if (tarea.getEstadoTarea() == EstadoTarea.COMPLETADO) {
            log.warn("Tarea {} ya completada, no se puede cancelar", idTarea);
            return;
        }

        tarea.setEstadoTarea(EstadoTarea.CANCELADO);
        tarea.setFechaFinalizacion(LocalDateTime.now());
        tareaRepository.save(tarea);

        auditoriaService.registrarCambio(
                "TAREA_EXPORTACION",
                idTarea.toString(),
                TipoOperacion.UPDATE,
                Map.of("estado", tarea.getEstadoTarea().name()),
                Map.of("estado", EstadoTarea.CANCELADO.name()),
                tarea.getIdUsuarioSolicitante(),
                "Usuario Cancelador"
        );

        progressNotifier.cerrarEmitter(idTarea);

        log.info("Tarea {} CANCELADA", idTarea);
    }

    // ═══════════════════════════════════════════════════════════
    // DESCARGAR
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public byte[] descargarPaquete(UUID idTarea, Long idSolicitante,
                                    String ip, String userAgent) {
        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException(
                        "Tarea no encontrada: " + idTarea));

        if (tarea.getEstadoTarea() != EstadoTarea.COMPLETADO) {
            throw new TareaExportacionNoEncontradaException(
                    "La tarea aun no esta disponible para descarga");
        }

        presignedUrlGenerator.verificarVigencia(idTarea);

        byte[] contenido = storageAdapter.obtenerArchivo(tarea.getRutaAlmacenamiento());

        String hashActual = hashLedgerService.calcularHashSha256(contenido);
        if (!hashActual.equals(tarea.getHashSha256())) {
            log.error("ALERTA INTEGRIDAD: hash del paquete no coincide para tarea {}", idTarea);
            throw new RuntimeException(
                    "El hash del archivo no coincide. Descarga bloqueada por seguridad.");
        }

        BitacoraDescargaExportacionEntity bitacora = BitacoraDescargaExportacionEntity.builder()
                .idTarea(idTarea)
                .idUsuarioDescarga(idSolicitante)
                .direccionIp(ip)
                .userAgent(userAgent)
                .fechaDescarga(LocalDateTime.now())
                .build();
        bitacoraDescargaRepository.save(bitacora);

        // Auditoría síncrona: Descarga de reporte
        auditoriaService.registrarDescargaReporte(
                idTarea.toString(),
                null,
                idSolicitante
        );

        log.info("Tarea {} descargada por usuario {} desde IP {}", idTarea, idSolicitante, ip);

        return contenido;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private void actualizarFase(TareaExportacionEntity tarea, FaseExportacion fase, int porcentaje) {
        tarea.setFaseActual(fase);
        tarea.setPorcentajeAvance(porcentaje);
        tareaRepository.save(tarea);
        notificarProgreso(tarea, "Fase: " + fase.name());
    }

    private void notificarProgreso(TareaExportacionEntity tarea, String mensaje) {
        ProgresoExportacionSseDTO progreso = ProgresoExportacionSseDTO.builder()
                .idTarea(tarea.getIdTarea())
                .porcentajeAvance(tarea.getPorcentajeAvance())
                .faseActual(tarea.getFaseActual() != null ? tarea.getFaseActual().name() : null)
                .registrosProcesados(tarea.getTotalRegistrosProcesados())
                .totalRegistros(tarea.getTotalRegistrosEstimados())
                .mensajeDetalle(mensaje)
                .build();
        progressNotifier.notificarProgreso(tarea.getIdTarea(), progreso);
    }

    private String construirNombreArchivo(TareaExportacionEntity tarea) {
        String extension = switch (tarea.getFormatoSalida()) {
            case MS_EXCEL_XLSX -> "xlsx";
            case CSV_TABULAR -> "csv";
            case JSON_ESTRUCTURADO -> "json";
            case HL7_FHIR_JSON -> "fhir.json";
            case APACHE_PARQUET -> "parquet";
            case ZIP_EXPEDIENTES -> "zip";
            case PDF_CONSOLIDADO -> "pdf";
        };
        return String.format("%s_%s.%s",
                tarea.getTituloLote().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase(),
                tarea.getIdTarea().toString().substring(0, 8),
                extension);
    }

    private String serializarFiltros(FiltroPoblacionalParams filtros) {
        try {
            return objectMapper.writeValueAsString(filtros);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar los filtros", e);
        }
    }

    private FiltroPoblacionalParams deserializarFiltros(String json) {
        try {
            return objectMapper.readValue(json, FiltroPoblacionalParams.class);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo deserializar los filtros", e);
        }
    }
}
