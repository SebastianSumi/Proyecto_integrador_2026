package pe.edu.upeu.saludablemente.auditoria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.detector.AnomaliaDetector;
import pe.edu.upeu.saludablemente.auditoria.detector.PromptInjectionDetector;
import pe.edu.upeu.saludablemente.auditoria.dto.TipoAnomaliaDeteccionDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.AlertaSeguridadEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.EstadoAlertaSeguridad;
import pe.edu.upeu.saludablemente.auditoria.enums.SeveridadAnomalia;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoAnomalia;
import pe.edu.upeu.saludablemente.auditoria.repository.AlertaSeguridadRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SeguridadAnomaliaServiceImpl implements SeguridadAnomaliaService {

    private final List<AnomaliaDetector> detectores;
    private final AlertaSeguridadRepository alertaRepository;
    private final NotificacionSeguridadService notificacionService;
    private final RevocacionSesionService revocacionService;
    private final PromptInjectionDetector promptInjectionDetector;
    private final ObjectMapper objectMapper;

    public SeguridadAnomaliaServiceImpl(List<AnomaliaDetector> detectores,
                                        AlertaSeguridadRepository alertaRepository,
                                        NotificacionSeguridadService notificacionService,
                                        RevocacionSesionService revocacionService,
                                        PromptInjectionDetector promptInjectionDetector,
                                        ObjectMapper objectMapper) {
        this.detectores = detectores;
        this.alertaRepository = alertaRepository;
        this.notificacionService = notificacionService;
        this.revocacionService = revocacionService;
        this.promptInjectionDetector = promptInjectionDetector;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void evaluarContexto(AnomaliaDetector.ContextoDeteccion contexto) {
        if (contexto == null) {
            return;
        }

        for (AnomaliaDetector detector : detectores) {
            try {
                if (!detector.aplica(contexto)) {
                    continue;
                }

                TipoAnomaliaDeteccionDTO resultado = detector.evaluar(contexto);

                if (resultado != null && resultado.isDetectada()) {
                    persistirYNotificar(resultado);
                }

            } catch (Exception e) {
                log.error("Error al ejecutar detector {}: {}",
                        detector.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void evaluarTextoParaInyeccion(String usuario, String texto, Long idPersona, String campo) {
        try {
            TipoAnomaliaDeteccionDTO resultado = promptInjectionDetector.evaluarTexto(
                    usuario, texto, idPersona, campo);

            if (resultado != null && resultado.isDetectada()) {
                persistirYNotificar(resultado);
            }
        } catch (Exception e) {
            log.error("Error al evaluar texto para inyeccion: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<AlertaSeguridadEntity> obtenerAlertasNoResueltas() {
        return alertaRepository.findByEstadoAlertaOrderByFechaDeteccionDesc(
                EstadoAlertaSeguridad.NO_RESUELTA);
    }

    private void persistirYNotificar(TipoAnomaliaDeteccionDTO deteccion) {
        try {
            String metadataJson = objectMapper.writeValueAsString(deteccion.getMetadataContexto());

            AlertaSeguridadEntity entidad = AlertaSeguridadEntity.builder()
                    .usuarioAfectado(deteccion.getUsuarioAfectado())
                    .tipoAnomalia(deteccion.getTipoAnomalia())
                    .severidad(deteccion.getSeveridad())
                    .metadataContexto(metadataJson)
                    .estadoAlerta(EstadoAlertaSeguridad.NO_RESUELTA)
                    .fechaDeteccion(LocalDateTime.now())
                    .build();

            AlertaSeguridadEntity guardada = alertaRepository.save(entidad);

            if (deteccion.getSeveridad() == SeveridadAnomalia.CRITICAL) {
                notificacionService.notificarAlertaCritica(guardada);

                if (deteccion.getTipoAnomalia() == TipoAnomalia.IMPOSSIBLE_TRAVEL) {
                    revocacionService.revocarSesion(
                            deteccion.getUsuarioAfectado(),
                            "Impossible Travel detectado");
                }
            } else if (deteccion.getSeveridad() == SeveridadAnomalia.WARNING) {
                notificacionService.notificarAlertaWarning(guardada);
            }

        } catch (Exception e) {
            log.error("Error al persistir alerta de seguridad: {}", e.getMessage(), e);
        }
    }
}
