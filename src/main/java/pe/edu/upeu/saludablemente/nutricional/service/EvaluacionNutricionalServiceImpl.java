package pe.edu.upeu.saludablemente.nutricional.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.BusinessRuleException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.nutricional.dto.DetalleBioquimicoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalAgregadoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalRequestDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResumenDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResponseDto;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleAntropometrico;
import pe.edu.upeu.saludablemente.nutricional.entity.DetalleBioquimico;
import pe.edu.upeu.saludablemente.nutricional.entity.EstadoEvaluacionNutricional;
import pe.edu.upeu.saludablemente.nutricional.entity.EvaluacionNutricional;
import pe.edu.upeu.saludablemente.nutricional.mapper.NutricionalMapper;
import pe.edu.upeu.saludablemente.nutricional.repository.EvaluacionNutricionalRepository;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;
import pe.edu.upeu.saludablemente.personal.service.PersonaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluacionNutricionalServiceImpl implements EvaluacionNutricionalService {

    private final EvaluacionNutricionalRepository evaluacionNutricionalRepository;
    private final PersonaService personaService;
    private final NutricionalMapper nutricionalMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionNutricionalResponseDto> listar(Long personaId,
                                                         EstadoEvaluacionNutricional estado,
                                                         String periodoSemestral,
                                                         LocalDate desde,
                                                         LocalDate hasta) {
        validarPersonaSiCorresponde(personaId);
        return evaluacionNutricionalRepository.buscar(personaId, estado, periodoSemestral, desde, hasta, Sort.by("id"))
                .stream()
                .map(nutricionalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionNutricionalResumenDto> listarResumen(Long personaId,
                                                               EstadoEvaluacionNutricional estado,
                                                               String periodoSemestral,
                                                               LocalDate desde,
                                                               LocalDate hasta) {
        validarPersonaSiCorresponde(personaId);
        return evaluacionNutricionalRepository.buscarResumen(personaId, estado, periodoSemestral, desde, hasta, Sort.by("id"));
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionNutricionalAgregadoDto obtenerAgregados(Long personaId, EstadoEvaluacionNutricional estado) {
        validarPersonaSiCorresponde(personaId);
        return evaluacionNutricionalRepository.agregados(personaId, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionNutricionalResponseDto obtener(Long idEvaluacion) {
        return nutricionalMapper.toResponse(buscarOFallar(idEvaluacion));
    }

    @Override
    @Transactional
    public EvaluacionNutricionalResponseDto registrarAntropometria(EvaluacionNutricionalRequestDto request) {
        PersonaResponseDto persona = personaService.obtener(request.getIdPersona());

        EvaluacionNutricional evaluacion = nutricionalMapper.toEntity(request);
        if (evaluacion.getEstadoEvaluacion() == null) {
            evaluacion.setEstadoEvaluacion(EstadoEvaluacionNutricional.EN_PROCESO);
        }

        DetalleAntropometrico detalle = evaluacion.getDetalleAntropometrico();
        detalle.setEvaluacionNutricional(evaluacion);
        calcularDiagnosticoAntropometrico(detalle, persona);

        return nutricionalMapper.toResponse(evaluacionNutricionalRepository.save(evaluacion));
    }

    @Override
    @Transactional
    public EvaluacionNutricionalResponseDto vincularBioquimico(Long idEvaluacion, DetalleBioquimicoDto detalleDto) {
        EvaluacionNutricional evaluacion = buscarOFallar(idEvaluacion);

        DetalleBioquimico detalle = nutricionalMapper.toBioquimico(detalleDto);
        detalle.setEvaluacionNutricional(evaluacion);
        if (detalle.getFechaImportacion() == null) {
            detalle.setFechaImportacion(LocalDateTime.now());
        }
        detalle.setDxBioquimico(diagnosticoBioquimico(detalle));

        evaluacion.setDetalleBioquimico(detalle);
        evaluacion.setEstadoEvaluacion(EstadoEvaluacionNutricional.COMPLETA);
        return nutricionalMapper.toResponse(evaluacionNutricionalRepository.save(evaluacion));
    }

    @Override
    @Transactional
    public void eliminar(Long idEvaluacion) {
        evaluacionNutricionalRepository.delete(buscarOFallar(idEvaluacion));
    }

    private void validarPersonaSiCorresponde(Long personaId) {
        if (personaId != null) {
            personaService.obtener(personaId);
        }
    }

    private EvaluacionNutricional buscarOFallar(Long idEvaluacion) {
        return evaluacionNutricionalRepository.findById(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluacion nutricional no encontrada: " + idEvaluacion));
    }

    private void calcularDiagnosticoAntropometrico(DetalleAntropometrico detalle, PersonaResponseDto persona) {
        if (detalle.getEstaturaCm() == null || detalle.getEstaturaCm().compareTo(BigDecimal.ZERO) <= 0
                || detalle.getPesoKg() == null || detalle.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La estatura y el peso deben ser mayores a cero");
        }

        BigDecimal estaturaM = detalle.getEstaturaCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal imc = detalle.getPesoKg().divide(estaturaM.multiply(estaturaM), 2, RoundingMode.HALF_UP);
        detalle.setImc(imc);
        detalle.setDxImc(diagnosticoImc(imc));

        if (detalle.getPerimetroAbdominalCm() != null) {
            detalle.setDxPerimetroAbdominal(
                    diagnosticoPerimetroAbdominal(detalle.getPerimetroAbdominalCm(), persona.getSexo()));
        }
        if (detalle.getPorcentajeGrasaVisceral() != null) {
            detalle.setDxGrasaVisceral(diagnosticoGrasaVisceral(detalle.getPorcentajeGrasaVisceral()));
        }
        if (detalle.getPorcentajeGrasa() != null) {
            detalle.setDxGrasa(diagnosticoGrasa(detalle.getPorcentajeGrasa(), persona.getSexo()));
        }
        if (detalle.getPorcentajeMasaMuscular() != null) {
            detalle.setDxMasaMuscular(diagnosticoMasaMuscular(detalle.getPorcentajeMasaMuscular(), persona.getSexo()));
        }
    }

    private String diagnosticoImc(BigDecimal imc) {
        double valor = imc.doubleValue();
        if (valor < 18.5) {
            return "Delgadez";
        }
        if (valor < 25.0) {
            return "Normal";
        }
        if (valor < 30.0) {
            return "Sobrepeso";
        }
        return "Obesidad";
    }

    private String diagnosticoPerimetroAbdominal(BigDecimal perimetro, String sexo) {
        double valor = perimetro.doubleValue();
        boolean esMujer = sexo != null && sexo.equalsIgnoreCase("F");
        if (esMujer) {
            if (valor < 80) {
                return "Normal";
            }
            if (valor < 90) {
                return "Alto";
            }
            return "Muy alto";
        }
        if (valor < 90) {
            return "Normal";
        }
        if (valor < 100) {
            return "Alto";
        }
        return "Muy alto";
    }

    private String diagnosticoGrasaVisceral(BigDecimal porcentaje) {
        double valor = porcentaje.doubleValue();
        if (valor <= 9) {
            return "Normal";
        }
        if (valor <= 14) {
            return "Alto";
        }
        return "Muy alto";
    }

    private String diagnosticoGrasa(BigDecimal porcentaje, String sexo) {
        double valor = porcentaje.doubleValue();
        boolean esMujer = sexo != null && sexo.equalsIgnoreCase("F");
        if (esMujer) {
            if (valor < 20) {
                return "Esencial";
            }
            if (valor < 30) {
                return "Normal";
            }
            if (valor < 35) {
                return "Alto";
            }
            return "Muy alto";
        }
        if (valor < 10) {
            return "Esencial";
        }
        if (valor < 20) {
            return "Normal";
        }
        if (valor < 25) {
            return "Alto";
        }
        return "Muy alto";
    }

    private String diagnosticoMasaMuscular(BigDecimal porcentaje, String sexo) {
        double valor = porcentaje.doubleValue();
        boolean esMujer = sexo != null && sexo.equalsIgnoreCase("F");
        if (esMujer) {
            if (valor < 24) {
                return "Bajo";
            }
            if (valor < 30) {
                return "Normal";
            }
            return "Alto";
        }
        if (valor < 33) {
            return "Bajo";
        }
        if (valor < 39) {
            return "Normal";
        }
        return "Alto";
    }

    private String diagnosticoBioquimico(DetalleBioquimico detalle) {
        List<String> alterados = new ArrayList<>();
        if (detalle.getGlucosa() != null && detalle.getGlucosa().compareTo(BigDecimal.valueOf(100)) > 0) {
            alterados.add("glucosa");
        }
        if (detalle.getColesterol() != null && detalle.getColesterol().compareTo(BigDecimal.valueOf(200)) > 0) {
            alterados.add("colesterol");
        }
        if (detalle.getTrigliceridos() != null && detalle.getTrigliceridos().compareTo(BigDecimal.valueOf(150)) > 0) {
            alterados.add("trigliceridos");
        }
        if (detalle.getPresionSistolica() != null && detalle.getPresionSistolica() > 120) {
            alterados.add("presion sistolica");
        }
        return alterados.isEmpty() ? "NORMAL" : "ALTERADO: " + String.join(", ", alterados);
    }
}