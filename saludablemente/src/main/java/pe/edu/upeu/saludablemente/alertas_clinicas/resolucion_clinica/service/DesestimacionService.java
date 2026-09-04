package pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.dto.DesestimarAlertaRequestDTO;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.entity.ResolucionClinicaEntity;
import pe.edu.upeu.saludablemente.alertas_clinicas.resolucion_clinica.exception.MotivoDesestimacionRequeridoException;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesestimacionService {

    // Motivos válidos para desestimación
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

    /**
     * Valida el motivo de desestimación
     */
    public void validarMotivoDesestimacion(DesestimarAlertaRequestDTO request)
            throws MotivoDesestimacionRequeridoException {

        if (request == null) {
            throw new MotivoDesestimacionRequeridoException(null);
        }

        if (request.getMotivoDesestimacion() == null ||
                request.getMotivoDesestimacion().trim().isEmpty()) {
            throw new MotivoDesestimacionRequeridoException(request.getIdAlerta());
        }

        // Validar que el motivo esté en la lista de válidos
        String motivo = request.getMotivoDesestimacion().toUpperCase();
        if (!MOTIVOS_VALIDOS.contains(motivo)) {
            throw new MotivoDesestimacionRequeridoException(
                    String.format("El motivo '%s' no es válido. Motivos permitidos: %s",
                            motivo, MOTIVOS_VALIDOS)
            );
        }

        log.debug("✅ Motivo de desestimación validado: {}", motivo);
    }

    /**
     * Obtiene la descripción del motivo de desestimación
     */
    public String getDescripcionMotivo(String motivo) {
        return switch (motivo.toUpperCase()) {
            case "BAJO_TRATAMIENTO_PREVIO" -> "El paciente ya se encuentra bajo tratamiento médico verificado";
            case "HIPERTENSION_CONTROLADA" -> "La hipertensión está controlada con tratamiento actual";
            case "DIABETES_CONTROLADA" -> "La diabetes está controlada con tratamiento actual";
            case "COLESTEROL_CONTROLADO" -> "El colesterol está controlado con tratamiento actual";
            case "TRIGLICERIDOS_CONTROLADOS" -> "Los triglicéridos están controlados con tratamiento actual";
            case "PACIENTE_EN_SEGUIMIENTO" -> "El paciente está en seguimiento médico regular";
            case "ERROR_EN_REGISTRO" -> "La alerta fue generada por un error en el registro de datos";
            case "DUPLICADO" -> "La alerta es duplicada de otra existente";
            case "FALSO_POSITIVO" -> "La alerta fue identificada como falso positivo";
            default -> "Motivo no especificado";
        };
    }

    /**
     * Registra la desestimación en la resolución
     */
    public ResolucionClinicaEntity registrarDesestimacion(
            ResolucionClinicaEntity resolucion,
            DesestimarAlertaRequestDTO request) {

        if (resolucion == null) {
            throw new IllegalArgumentException("La resolución no puede ser nula");
        }

        resolucion.setMotivoDesestimacion(request.getMotivoDesestimacion());
        resolucion.setObservacionesClinicas(request.getObservacionesClinicas());
        resolucion.setEsSeguimientoProgramado(false);

        log.info("📝 Desestimación registrada para alerta {}: {}",
                request.getIdAlerta(), request.getMotivoDesestimacion());

        return resolucion;
    }

    /**
     * Obtiene los motivos válidos para desestimación
     */
    public Set<String> getMotivosValidos() {
        return MOTIVOS_VALIDOS;
    }
}