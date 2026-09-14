package pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.dto.*;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaDetalleEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.mapper.AlertaMapper;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.catalog.OmronReferenceCatalog;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.IndicadorEvaluadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.ScoreDetalleDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.dto.SindromeResultadoDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service.AgrupacionSindromesService;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service.CalculadoraDesviacionService;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service.MatrizSeveridadService;
import pe.edu.upeu.saludablemente.alertas_clinicas.evaluacion_scoring.service.MultiplicadorReincidenciaService;
import pe.edu.upeu.saludablemente.alertas_clinicas.exception.EvaluacionOrigenInvalidaException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.NivelSeveridad;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.TipoIndicador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaClinicaRepository alertaRepository;
    private final AlertaMapper alertaMapper;
    private final OmronReferenceCatalog omronCatalog;
    private final CalculadoraDesviacionService calculadoraDesviacion;
    private final MatrizSeveridadService matrizSeveridadService;
    private final AgrupacionSindromesService agrupacionSindromesService;
    private final MultiplicadorReincidenciaService multiplicadorReincidenciaService;
    private final ConsolidacionDuplicadosService consolidacionDuplicadosService;
    private final SnapshotInmutableService snapshotInmutableService;

    @Override
    @Transactional
    public AlertaDetalleResponseDTO crearAlerta(AlertaRequestDTO request) {
        if (request == null || request.getIdPersona() == null) {
            throw new EvaluacionOrigenInvalidaException("El ID de la persona es requerido para generar una alerta clínica.");
        }

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new EvaluacionOrigenInvalidaException("Debe proporcionar al menos un indicador para evaluar la alerta.");
        }

        log.info("Iniciando evaluación síncrona de alerta clínica para paciente ID: {}", request.getIdPersona());

        // 1. Evaluar indicadores y desviaciones
        List<IndicadorEvaluadoDTO> indicadoresEvaluados = new ArrayList<>();
        List<AlertaClinicaDetalleEntity> detallesEntidad = new ArrayList<>();

        for (AlertaDetalleRequestDTO detalleReq : request.getDetalles()) {
            BigDecimal limite = resolverLimiteReferencia(detalleReq.getTipoIndicador(), detalleReq.getLimiteReferencia(), request.getSexo());
            BigDecimal desviacion = calculadoraDesviacion.calcularDesviacion(detalleReq.getValorMedido(), limite);
            boolean esAlterado = desviacion.compareTo(BigDecimal.ZERO) > 0;

            IndicadorEvaluadoDTO indicadorDTO = IndicadorEvaluadoDTO.builder()
                    .tipoIndicador(detalleReq.getTipoIndicador())
                    .valorMedido(detalleReq.getValorMedido())
                    .limiteReferencia(limite)
                    .porcentajeDesviacion(desviacion)
                    .esAlterado(esAlterado)
                    .multiplicadorReincidencia(BigDecimal.ONE)
                    .build();

            indicadoresEvaluados.add(indicadorDTO);

            AlertaClinicaDetalleEntity detalleEntity = AlertaClinicaDetalleEntity.builder()
                    .tipoIndicador(detalleReq.getTipoIndicador())
                    .valorMedido(detalleReq.getValorMedido())
                    .limiteReferencia(limite)
                    .porcentajeDesviacion(desviacion)
                    .multiplicadorReincidencia(BigDecimal.ONE)
                    .build();

            detallesEntidad.add(detalleEntity);
        }

        // 2. Detección de Síndromes
        SindromeResultadoDTO sindrome = agrupacionSindromesService.detectarSindrome(indicadoresEvaluados);

        // 3. Cálculo de Scoring Inteligente
        List<ScoreDetalleDTO> scoreDetalles = matrizSeveridadService.calcularScore(indicadoresEvaluados, null);
        int scoreTotal = scoreDetalles.stream().mapToInt(ScoreDetalleDTO::getPuntosFinales).sum();
        if (sindrome != null) {
            scoreTotal += sindrome.getPuntosExtra();
        }

        NivelSeveridad severidad = matrizSeveridadService.determinarSeveridad(scoreTotal);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vencimientoSla = ahora.plusHours(72);

        // 4. Generación de Snapshot inmutable
        String snapshotJson = snapshotInmutableService.generarSnapshotJson(
                request.getIdPersona(),
                request.getIdEvaluacionOrigen(),
                severidad,
                scoreTotal,
                sindrome != null ? sindrome.getCodigo() : null,
                EstadoAlerta.PENDIENTE,
                indicadoresEvaluados,
                scoreDetalles
        );

        // 5. Construcción de entidad cabecera
        AlertaClinicaEntity alerta = AlertaClinicaEntity.builder()
                .idPersona(request.getIdPersona())
                .nombrePaciente(request.getNombrePaciente() != null ? request.getNombrePaciente() : "Paciente " + request.getIdPersona())
                .idEvaluacionOrigen(request.getIdEvaluacionOrigen())
                .nivelSeveridad(severidad)
                .scoreRiesgo(scoreTotal)
                .estado(EstadoAlerta.PENDIENTE)
                .snapshotInmutable(snapshotJson)
                .fechaGeneracion(ahora)
                .fechaVencimientoSla(vencimientoSla)
                .build();

        for (AlertaClinicaDetalleEntity det : detallesEntidad) {
            alerta.addDetalle(det);
        }

        // 6. Consolidación de duplicados (ventana 24h)
        for (AlertaClinicaDetalleEntity det : detallesEntidad) {
            Optional<AlertaClinicaEntity> duplicadoOpt = consolidacionDuplicadosService.encontrarDuplicado(
                    request.getIdPersona(), det.getTipoIndicador(), ahora);
            if (duplicadoOpt.isPresent()) {
                log.info("Alerta redundante consolidada en registro activo ID: {}", duplicadoOpt.get().getIdAlerta());
                AlertaClinicaEntity consolidada = consolidacionDuplicadosService.consolidarConDuplicado(alerta, duplicadoOpt.get());
                AlertaClinicaEntity guardada = alertaRepository.save(consolidada);
                AlertaDetalleResponseDTO respuesta = alertaMapper.toDetalleDTO(guardada);
                if (sindrome != null) respuesta.setSindromeDetectado(sindrome.getCodigo());
                return respuesta;
            }
        }

        // 7. Persistencia atómica
        AlertaClinicaEntity guardada = alertaRepository.save(alerta);
        log.info("Alerta clínica creada exitosamente con ID: {}, Severidad: {}, Score: {}",
                guardada.getIdAlerta(), guardada.getNivelSeveridad(), guardada.getScoreRiesgo());

        AlertaDetalleResponseDTO respuesta = alertaMapper.toDetalleDTO(guardada);
        if (sindrome != null) {
            respuesta.setSindromeDetectado(sindrome.getCodigo());
        }
        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaInboxResponseDTO> obtenerBandejaInbox(FiltroInboxRequestDTO filtro) {
        int pagina = filtro != null && filtro.getPagina() != null ? Math.max(0, filtro.getPagina()) : 0;
        int limite = filtro != null && filtro.getLimite() != null ? Math.max(1, filtro.getLimite()) : 15;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaGeneracion"));

        EstadoAlerta estado = filtro != null ? filtro.getEstado() : null;
        NivelSeveridad severidad = filtro != null ? filtro.getNivelSeveridad() : null;
        Long idPersona = filtro != null ? filtro.getIdPersona() : null;
        TipoIndicador tipoIndicador = filtro != null ? filtro.getTipoIndicador() : null;

        Page<AlertaClinicaEntity> paginaEntidades = alertaRepository.buscarConFiltros(
                estado, severidad, idPersona, tipoIndicador, pageable);

        return paginaEntidades.map(alertaMapper::toInboxDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaDetalleResponseDTO obtenerAlertaPorId(UUID idAlerta) {
        AlertaClinicaEntity alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró alerta clínica con ID: " + idAlerta));

        return alertaMapper.toDetalleDTO(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaAgregadoDTO obtenerResumenMetricas() {
        return alertaRepository.obtenerMetricasAgregadas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaPacienteResponseDTO> obtenerAlertasPaciente(Long idPersona) {
        if (idPersona == null) {
            throw new EvaluacionOrigenInvalidaException("El ID de persona es requerido.");
        }

        List<AlertaClinicaEntity> alertas = alertaRepository.findByIdPersonaOrderByFechaGeneracionDesc(idPersona);
        return alertas.stream().map(alertaMapper::toPacienteDTO).toList();
    }

    private BigDecimal resolverLimiteReferencia(TipoIndicador tipo, BigDecimal limiteEnviado, String sexo) {
        if (limiteEnviado != null && limiteEnviado.compareTo(BigDecimal.ZERO) > 0) {
            return limiteEnviado;
        }

        if (tipo == null) return new BigDecimal("100.0");

        return switch (tipo) {
            case IMC -> omronCatalog.getImcNormalMax();
            case GRASA_VISCERAL -> omronCatalog.getGrasaVisceralNormalMax(sexo != null ? sexo : "M");
            case GLUCOSA -> omronCatalog.getGlucosaNormalMax();
            case COLESTEROL_TOTAL -> omronCatalog.getColesterolTotalNormalMax();
            case TRIGLICERIDOS -> omronCatalog.getTrigliceridosNormalMax();
            case PRESION_SISTOLICA -> omronCatalog.getPresionSistolicaNormalMax();
            case PRESION_DIASTOLICA -> omronCatalog.getPresionDiastolicaNormalMax();
            default -> new BigDecimal("100.0");
        };
    }
}
