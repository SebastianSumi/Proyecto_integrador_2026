package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.dto.*;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity.RecomendacionDetalleEntity;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity.RecomendacionIAEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecomendacionMapper {

    public RecomendacionVigenteResponseDTO toVigenteResponseDTO(RecomendacionIAEntity entity) {
        if (entity == null) return null;

        RutinaEjercicioDTO rutina = null;
        PlanNutricionalDTO nutricion = null;

        for (RecomendacionDetalleEntity detalle : entity.getDetalles()) {
            switch (detalle.getTipoSeccion()) {
                case EJERCICIO -> rutina = RutinaEjercicioDTO.fromMap(detalle.getContenidoEstructurado());
                case NUTRICION -> nutricion = PlanNutricionalDTO.fromMap(detalle.getContenidoEstructurado());
            }
        }

        return RecomendacionVigenteResponseDTO.builder()
                .idRecomendacion(entity.getIdRecomendacion())
                .idEvaluacionBase(entity.getIdEvaluacion())
                .fechaGeneracion(entity.getFechaGeneracion())
                .vigente(entity.getVigente())
                .leida(entity.getLeida())
                .rutinaEjercicio(rutina)
                .planNutricional(nutricion)
                .build();
    }

    public RecomendacionHistorialItemDTO toHistorialItemDTO(RecomendacionIAEntity entity) {
        return RecomendacionHistorialItemDTO.builder()
                .idRecomendacion(entity.getIdRecomendacion())
                .periodoSemestral(generarPeriodoSemestral(entity.getFechaGeneracion()))
                .fechaGeneracion(entity.getFechaGeneracion())
                .modeloIaUsado(entity.getModeloIaUsado())
                .vigente(entity.getVigente())
                .estadoInferencia(entity.getEstadoInferencia())
                .build();
    }

    public RecomendacionAuditoriaDetalleDTO toAuditoriaDetalleDTO(RecomendacionIAEntity entity) {
        return RecomendacionAuditoriaDetalleDTO.builder()
                .idRecomendacion(entity.getIdRecomendacion())
                .idPersona(entity.getIdPersona())
                .idEvaluacion(entity.getIdEvaluacion())
                .modeloIaUsado(entity.getModeloIaUsado())
                .scoreConfianzaIa(entity.getScoreConfianzaIa())
                .fechaGeneracion(entity.getFechaGeneracion())
                .promptContexto(entity.getPromptContexto())
                .tiempoInferenciaMs(entity.getTiempoInferenciaMs())
                .vigente(entity.getVigente())
                .estadoInferencia(entity.getEstadoInferencia())
                .build();
    }

    private String generarPeriodoSemestral(LocalDateTime fecha) {
        if (fecha == null) return "Sin periodo";
        int year = fecha.getYear();
        int semester = fecha.getMonthValue() <= 6 ? 1 : 2;
        return year + "-" + semester;
    }
}