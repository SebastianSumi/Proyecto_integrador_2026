package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.exception.MotivoDesestimacionRequeridoException;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion.entity.ResolucionClinicaEntity;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesestimacionService {

    private static final Set<String> MOTIVOS_VALIDOS = Set.of(
            "BAJO_TRATAMIENTO_PREVIO",
            "HIPERTENSION_CONTROLADA",
            "DIABETES_CONTROLADA",
            "COLESTEROL_CONTROLADO",
            "TRIGLICERIDOS_CONTROLADOS",
            "PACIENTE_EN_SEGUIMIENTO",
            "ERROR_EN_REGISTRO",
            "DUPLICADO",
            "FALSO_POSITIVO"
    );

    public void validarMotivoDesestimacion(DesestimarAlertaRequestDTO request) {
        if (request == null) {
            throw new MotivoDesestimacionRequeridoException("El request de desestimación no puede ser nulo");
        }

        if (request.getMotivoDesestimacion() == null || request.getMotivoDesestimacion().trim().isEmpty()) {
            throw new MotivoDesestimacionRequeridoException("El motivo de desestimación es obligatorio para la alerta: " + request.getIdAlerta());
        }

        String motivo = request.getMotivoDesestimacion().trim().toUpperCase();
        if (!MOTIVOS_VALIDOS.contains(motivo)) {
            throw new MotivoDesestimacionRequeridoException(
                    String.format("El motivo '%s' no es válido. Motivos clínicos permitidos: %s", motivo, MOTIVOS_VALIDOS)
            );
        }

        log.debug("Motivo de desestimación validado: {}", motivo);
    }

    public ResolucionClinicaEntity registrarDesestimacion(
            ResolucionClinicaEntity resolucion,
            DesestimarAlertaRequestDTO request) {

        if (resolucion == null) {
            throw new IllegalArgumentException("La resolución no puede ser nula");
        }

        resolucion.setMotivoDesestimacion(request.getMotivoDesestimacion().trim().toUpperCase());
        resolucion.setObservacionesClinicas(request.getObservacionesClinicas());
        resolucion.setEsSeguimientoProgramado(false);

        log.info("Desestimación registrada para alerta {}: {}",
                request.getIdAlerta(), request.getMotivoDesestimacion());

        return resolucion;
    }

    public Set<String> getMotivosValidos() {
        return MOTIVOS_VALIDOS;
    }
}
