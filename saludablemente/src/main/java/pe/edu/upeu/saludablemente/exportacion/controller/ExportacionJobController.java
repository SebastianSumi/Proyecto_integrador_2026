package pe.edu.upeu.saludablemente.exportacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pe.edu.upeu.saludablemente.exception.TareaExportacionNoEncontradaException;
import pe.edu.upeu.saludablemente.exportacion.dto.*;
import pe.edu.upeu.saludablemente.exportacion.entity.TareaExportacionEntity;
import pe.edu.upeu.saludablemente.exportacion.mapper.TareaExportacionMapper;
import pe.edu.upeu.saludablemente.exportacion.repository.TareaExportacionRepository;
import pe.edu.upeu.saludablemente.exportacion.service.ExportacionBatchOrchestrator;
import pe.edu.upeu.saludablemente.exportacion.service.StreamingExportService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/exportaciones")
@RequiredArgsConstructor
@Tag(name = "Exportaciones", description = "API para gestion y descarga de tareas de exportacion")
public class ExportacionJobController {

    private final ExportacionBatchOrchestrator orchestrator;
    private final StreamingExportService streamingService;
    private final TareaExportacionRepository tareaRepository;
    private final TareaExportacionMapper mapper;

    private static final Long USUARIO_DEFAULT = 1L;

    @PostMapping("/iniciar")
    @Operation(summary = "Iniciar una nueva tarea de exportacion")
    public ResponseEntity<TareaIniciadaResponseDTO> iniciarExportacion(
            @Valid @RequestBody IniciarExportacionRequestDTO request) {

        log.info("Solicitud de exportacion recibida: {}", request.getTituloLote());
        TareaIniciadaResponseDTO respuesta = orchestrator.iniciarExportacion(request, USUARIO_DEFAULT);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{idTarea}/estado")
    @Operation(summary = "Consultar el estado de una tarea")
    public ResponseEntity<EstadoExportacionResponseDTO> obtenerEstado(@PathVariable UUID idTarea) {
        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException("Tarea no encontrada: " + idTarea));
        return ResponseEntity.ok(mapper.toEstadoResponse(tarea));
    }

    @GetMapping("/{idTarea}/descargar")
    @Operation(summary = "Descargar el archivo generado")
    public ResponseEntity<StreamingResponseBody> descargar(
            @PathVariable UUID idTarea,
            @RequestParam(name = "token", required = false) String token,
            HttpServletRequest request) {

        log.info("Solicitud de descarga de tarea {}", idTarea);

        TareaExportacionEntity tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new TareaExportacionNoEncontradaException("Tarea no encontrada: " + idTarea));

        String ip = obtenerIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Validar y registrar bitacora local
        orchestrator.descargarPaquete(idTarea, USUARIO_DEFAULT, ip, userAgent);

        StreamingResponseBody body = streamingService.servirArchivo(tarea.getRutaAlmacenamiento());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tarea.getRutaAlmacenamiento() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(tarea.getTamanoBytes() != null ? tarea.getTamanoBytes() : 0))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @PostMapping("/{idTarea}/cancelar")
    @Operation(summary = "Cancelar una tarea en ejecucion o en cola")
    public ResponseEntity<Void> cancelar(@PathVariable UUID idTarea) {
        log.info("Cancelando tarea {}", idTarea);
        orchestrator.cancelarTarea(idTarea);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/historial")
    @Operation(summary = "Consultar el historial de tareas de exportacion")
    public ResponseEntity<Page<TareaHistorialItemDTO>> historial(
            @RequestBody(required = false) FiltroTareasExportacionRequestDTO filtro) {

        int pagina = filtro != null && filtro.getPagina() != null ? filtro.getPagina() : 0;
        int limite = filtro != null && filtro.getLimite() != null ? filtro.getLimite() : 10;
        Pageable pageable = PageRequest.of(pagina, limite);

        LocalDateTime desde = filtro != null && filtro.getFechaDesde() != null
                ? filtro.getFechaDesde()
                : LocalDateTime.now().minusDays(30);

        Page<TareaHistorialItemDTO> resultado = tareaRepository.findUltimas(desde, pageable)
                .map(mapper::toHistorialItem);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/catalogo")
    @Operation(summary = "Catalogo de formatos y niveles de privacidad disponibles")
    public ResponseEntity<CatalogoFormatosResponseDTO> obtenerCatalogo() {
        return ResponseEntity.ok(CatalogoFormatosResponseDTO.builder()
                .formatosDisponibles(List.of(
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("MS_EXCEL_XLSX")
                                .nombre("Microsoft Excel (.xlsx)")
                                .descripcion("Libro con datos, formulas y resumen")
                                .soportaCifrado(true)
                                .soportaGraficos(true)
                                .build(),
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("CSV_TABULAR")
                                .nombre("CSV Delimitado (.csv)")
                                .descripcion("Texto plano UTF-8 con BOM")
                                .soportaCifrado(false)
                                .soportaGraficos(false)
                                .build(),
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("JSON_ESTRUCTURADO")
                                .nombre("JSON Estructurado (.json)")
                                .descripcion("Jerarquia de datos estandar")
                                .soportaCifrado(false)
                                .soportaGraficos(false)
                                .build(),
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("HL7_FHIR_JSON")
                                .nombre("HL7 FHIR Bundle R4 (.json)")
                                .descripcion("Recursos Patient y Observation con LOINC")
                                .soportaCifrado(false)
                                .soportaGraficos(false)
                                .build(),
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("ZIP_EXPEDIENTES")
                                .nombre("Paquete ZIP (.zip)")
                                .descripcion("Contenedor completo con Excel, CSV y PDF")
                                .soportaCifrado(true)
                                .soportaGraficos(true)
                                .build(),
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("PDF_CONSOLIDADO")
                                .nombre("PDF Consolidado (.pdf)")
                                .descripcion("Reporte de poblacion con resumen ejecutivo")
                                .soportaCifrado(false)
                                .soportaGraficos(true)
                                .build(),
                        CatalogoFormatosResponseDTO.FormatoInfo.builder()
                                .codigo("APACHE_PARQUET")
                                .nombre("Apache Parquet (.parquet)")
                                .descripcion("Formato columnar para analitica")
                                .soportaCifrado(false)
                                .soportaGraficos(false)
                                .build()
                ))
                .nivelesPrivacidad(List.of(
                        CatalogoFormatosResponseDTO.NivelPrivacidadInfo.builder()
                                .codigo("COMPLETO_NOMINATIVO")
                                .nombre("Completo Nominativo")
                                .descripcion("Identidad clinica y civil completa")
                                .build(),
                        CatalogoFormatosResponseDTO.NivelPrivacidadInfo.builder()
                                .codigo("PSEUDONIMIZADO")
                                .nombre("Pseudonimizado")
                                .descripcion("Sustituye nombres y DNI por hashes deterministas")
                                .build(),
                        CatalogoFormatosResponseDTO.NivelPrivacidadInfo.builder()
                                .codigo("ANONIMIZADO_ESTADISTICO")
                                .nombre("Anonimizado Estadistico (K-Anonymity)")
                                .descripcion("Generaliza o suprime cuasi-identificadores")
                                .build()
                ))
                .build());
    }

    private String obtenerIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
