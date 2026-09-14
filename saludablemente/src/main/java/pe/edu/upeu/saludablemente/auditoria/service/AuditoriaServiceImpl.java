package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.context.AuditContextHolder;
import pe.edu.upeu.saludablemente.auditoria.detector.AnomaliaDetector;
import pe.edu.upeu.saludablemente.auditoria.dto.AuditContextDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.EventoAuditoriaSanitizadoDTO;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final DataMaskingService dataMaskingService;
    private final BitacoraPersistenciaService bitacoraPersistenciaService;
    private final SeguridadAnomaliaService seguridadAnomaliaService;

    @Override
    @Transactional
    public void registrarCambio(String tipoEntidad,
                                String idEntidad,
                                TipoOperacion tipoOperacion,
                                Map<String, Object> estadoAnterior,
                                Map<String, Object> estadoNuevo,
                                Long idPersona,
                                String usuarioAutor) {

        if (tipoEntidad == null || tipoOperacion == null) {
            log.warn("Intento de registrar cambio sin tipoEntidad o tipoOperacion. Se omite.");
            return;
        }

        AuditContextDTO contexto = AuditContextHolder.get();

        Map<String, Object> anteriorEnmascarado = dataMaskingService.enmascararMapa(estadoAnterior);
        Map<String, Object> nuevoEnmascarado = dataMaskingService.enmascararMapa(estadoNuevo);

        EventoAuditoriaSanitizadoDTO evento = EventoAuditoriaSanitizadoDTO.builder()
                .tipoEvento("CAMBIO_" + tipoOperacion.name())
                .tipoEntidad(tipoEntidad)
                .idEntidad(idEntidad)
                .tipoOperacion(tipoOperacion.name())
                .idPersona(idPersona)
                .estadoAnterior(anteriorEnmascarado)
                .estadoNuevo(nuevoEnmascarado)
                .contextoHttp(contexto)
                .usuarioAutor(usuarioAutor != null ? usuarioAutor
                        : (contexto != null ? contexto.getUsuario() : "sistema"))
                .timestampEvento(LocalDateTime.now())
                .build();

        bitacoraPersistenciaService.persistirEvento(evento);

        try {
            AnomaliaDetector.ContextoDeteccion contextoDeteccion =
                    AnomaliaDetector.ContextoDeteccion.builder()
                            .usuario(evento.getUsuarioAutor())
                            .direccionIp(contexto != null ? contexto.getDireccionIp() : null)
                            .userAgent(contexto != null ? contexto.getUserAgent() : null)
                            .uri(contexto != null ? contexto.getUri() : null)
                            .metodoHttp(contexto != null ? contexto.getMetodoHttp() : null)
                            .tipoEntidad(tipoEntidad)
                            .idEntidad(idEntidad)
                            .tipoOperacion(tipoOperacion.name())
                            .idPersona(idPersona)
                            .timestamp(LocalDateTime.now())
                            .build();

            seguridadAnomaliaService.evaluarContexto(contextoDeteccion);

        } catch (Exception e) {
            log.warn("Error al evaluar anomalias (no bloquea la auditoria): {}", e.getMessage());
        }

        log.debug("Cambio registrado: {} {} ({})",
                tipoEntidad, idEntidad, tipoOperacion);
    }

    @Override
    @Transactional
    public void registrarLectura(Long idPersona,
                                 String tipoEntidad,
                                 String idEntidad,
                                 String usuarioLector) {

        if (idPersona == null || tipoEntidad == null || idEntidad == null) {
            log.warn("Intento de registrar lectura con datos incompletos. Se omite.");
            return;
        }

        AuditContextDTO contexto = AuditContextHolder.get();

        String usuarioFinal = usuarioLector != null ? usuarioLector
                : (contexto != null ? contexto.getUsuario() : "desconocido");

        bitacoraPersistenciaService.registrarLectura(
                idPersona, tipoEntidad, idEntidad, usuarioFinal);

        log.debug("Lectura registrada: persona={}, entidad={}, id={}, usuario={}",
                idPersona, tipoEntidad, idEntidad, usuarioFinal);
    }

    @Override
    @Transactional
    public void registrarInferenciaIA(String idRecomendacion,
                                       Long idPersona,
                                       String modeloIaUsado,
                                       String promptContexto,
                                       Integer tiempoInferenciaMs) {

        if (idRecomendacion == null) {
            log.warn("Intento de registrar inferencia IA sin idRecomendacion. Se omite.");
            return;
        }

        AuditContextDTO contexto = AuditContextHolder.get();

        Map<String, Object> estadoNuevo = new HashMap<>();
        estadoNuevo.put("modeloIaUsado", modeloIaUsado);
        estadoNuevo.put("promptContexto", promptContexto);
        estadoNuevo.put("tiempoInferenciaMs", tiempoInferenciaMs);
        estadoNuevo.put("idPersona", idPersona);

        Map<String, Object> enmascarado = dataMaskingService.enmascararMapa(estadoNuevo);

        EventoAuditoriaSanitizadoDTO evento = EventoAuditoriaSanitizadoDTO.builder()
                .tipoEvento("INFERENCIA_IA_EJECUTADA")
                .tipoEntidad("RECOMENDACION_IA")
                .idEntidad(String.valueOf(idRecomendacion))
                .tipoOperacion(TipoOperacion.INSERT.name())
                .idPersona(idPersona)
                .estadoAnterior(null)
                .estadoNuevo(enmascarado)
                .contextoHttp(contexto)
                .usuarioAutor("sistema-ia")
                .timestampEvento(LocalDateTime.now())
                .build();

        bitacoraPersistenciaService.persistirEvento(evento);

        try {
            seguridadAnomaliaService.evaluarTextoParaInyeccion(
                    evento.getUsuarioAutor(), promptContexto, idPersona, "promptContexto");
        } catch (Exception e) {
            log.warn("Error al evaluar inyeccion en prompt IA: {}", e.getMessage());
        }

        log.info("Inferencia IA registrada: recomendacion={}, modelo={}, tiempoMs={}",
                idRecomendacion, modeloIaUsado, tiempoInferenciaMs);
    }

    @Override
    @Transactional
    public void registrarDescargaReporte(String idReporte,
                                          Long idPersona,
                                          Long idSolicitante) {

        if (idReporte == null) {
            log.warn("Intento de registrar descarga sin idReporte. Se omite.");
            return;
        }

        AuditContextDTO contexto = AuditContextHolder.get();

        Map<String, Object> estadoNuevo = new HashMap<>();
        estadoNuevo.put("idReporte", idReporte);
        estadoNuevo.put("idPersona", idPersona);
        estadoNuevo.put("idSolicitante", idSolicitante);
        estadoNuevo.put("direccionIp", contexto != null ? contexto.getDireccionIp() : null);
        estadoNuevo.put("userAgent", contexto != null ? contexto.getUserAgent() : null);

        EventoAuditoriaSanitizadoDTO evento = EventoAuditoriaSanitizadoDTO.builder()
                .tipoEvento("REPORTE_DESCARGADO")
                .tipoEntidad("REPORTE_PERSONAL")
                .idEntidad(idReporte)
                .tipoOperacion(TipoOperacion.EXECUTE.name())
                .idPersona(idPersona)
                .estadoAnterior(null)
                .estadoNuevo(estadoNuevo)
                .contextoHttp(contexto)
                .usuarioAutor(idSolicitante != null
                        ? "usuario_" + idSolicitante
                        : (contexto != null ? contexto.getUsuario() : "sistema"))
                .timestampEvento(LocalDateTime.now())
                .build();

        bitacoraPersistenciaService.persistirEvento(evento);

        log.info("Descarga de reporte registrada: reporte={}, persona={}",
                idReporte, idPersona);
    }
}
