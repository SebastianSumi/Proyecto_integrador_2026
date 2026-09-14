package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.entity.AlertaClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.repository.AlertaClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.alerta.service.MaquinariaEstadosService;
import pe.edu.upeu.saludablemente.alertas_clinicas.exception.AlertaConcurrenteException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.AtenderAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.CatalogoAccionResponseDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.CatalogoAccionCorrectivaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.mapper.ResolucionMapper;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.repository.CatalogoAccionRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.repository.ResolucionClinicaRepository;
import pe.edu.upeu.saludablemente.alertas_clinicas.shared.enums.EstadoAlerta;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolucionServiceImpl implements ResolucionService {

    private final ResolucionClinicaRepository resolucionRepository;
    private final CatalogoAccionRepository catalogoAccionRepository;
    private final AlertaClinicaRepository alertaRepository;
    private final MaquinariaEstadosService maquinariaEstadosService;
    private final TrazabilidadResponsableService trazabilidadResponsableService;
    private final DesestimacionService desestimacionService;
    private final ProgramadorSeguimientoService programadorSeguimientoService;
    private final ResolucionMapper resolucionMapper;

    @Override
    @Transactional
    public void atenderAlerta(AtenderAlertaRequestDTO request) {
        if (request == null || request.getIdAlerta() == null) {
            throw new IllegalArgumentException("El ID de alerta es obligatorio.");
        }

        AlertaClinicaEntity alerta = alertaRepository.findById(request.getIdAlerta())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró alerta clínica con ID: " + request.getIdAlerta()));

        if (resolucionRepository.existsByAlerta_IdAlerta(alerta.getIdAlerta())) {
            throw new AlertaConcurrenteException("La alerta " + alerta.getIdAlerta() + " ya tiene una resolución registrada.");
        }

        CatalogoAccionCorrectivaEntity accion = catalogoAccionRepository.findById(request.getIdAccionCorrectiva())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró acción correctiva con ID: " + request.getIdAccionCorrectiva()));

        // Transitar estado a ATENDIDA
        maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.ATENDIDA);

        ResolucionClinicaEntity resolucion = ResolucionClinicaEntity.builder()
                .alerta(alerta)
                .accionCorrectiva(accion)
                .observacionesClinicas(request.getObservacionesClinicas())
                .build();

        // Estampar trazabilidad y programar seguimiento si la acción lo amerita
        trazabilidadResponsableService.sellarConTrazabilidad(resolucion, request.getIdUsuarioEvaluador(), request.getNombreEvaluador());
        programadorSeguimientoService.programarSeguimiento(resolucion, accion);

        resolucionRepository.save(resolucion);
        alertaRepository.save(alerta);

        log.info("Alerta {} atendida formalmente con acción {}", alerta.getIdAlerta(), accion.getCodigoTipificado());
    }

    @Override
    @Transactional
    public void desestimarAlerta(DesestimarAlertaRequestDTO request) {
        if (request == null || request.getIdAlerta() == null) {
            throw new IllegalArgumentException("El ID de alerta es obligatorio.");
        }

        desestimacionService.validarMotivoDesestimacion(request);

        AlertaClinicaEntity alerta = alertaRepository.findById(request.getIdAlerta())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró alerta clínica con ID: " + request.getIdAlerta()));

        if (resolucionRepository.existsByAlerta_IdAlerta(alerta.getIdAlerta())) {
            throw new AlertaConcurrenteException("La alerta " + alerta.getIdAlerta() + " ya tiene una resolución registrada.");
        }

        // Transitar estado a DESESTIMADA
        maquinariaEstadosService.transitarEstado(alerta, EstadoAlerta.DESESTIMADA);

        ResolucionClinicaEntity resolucion = ResolucionClinicaEntity.builder()
                .alerta(alerta)
                .build();

        desestimacionService.registrarDesestimacion(resolucion, request);
        trazabilidadResponsableService.sellarConTrazabilidad(resolucion, request.getIdUsuarioEvaluador(), request.getNombreEvaluador());

        resolucionRepository.save(resolucion);
        alertaRepository.save(alerta);

        log.info("Alerta {} desestimada justificadamente con motivo: {}", alerta.getIdAlerta(), request.getMotivoDesestimacion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoAccionResponseDTO> obtenerCatalogoAcciones() {
        List<CatalogoAccionCorrectivaEntity> acciones = catalogoAccionRepository.findAll();
        return resolucionMapper.toAccionDTOList(acciones);
    }
}
