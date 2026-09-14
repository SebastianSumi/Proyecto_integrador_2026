package pe.edu.upeu.saludablemente.nutricional.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalAgregadoDto;
import pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResumenDto;
import pe.edu.upeu.saludablemente.nutricional.entity.EstadoEvaluacionNutricional;
import pe.edu.upeu.saludablemente.nutricional.entity.EvaluacionNutricional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EvaluacionNutricionalRepository extends JpaRepository<EvaluacionNutricional, Long> {

    @Override
    @EntityGraph(attributePaths = {"detalleAntropometrico", "detalleBioquimico"})
    Optional<EvaluacionNutricional> findById(Long id);

    @EntityGraph(attributePaths = {"detalleAntropometrico", "detalleBioquimico"})
    @Query("""
        SELECT v FROM EvaluacionNutricional v
        WHERE (:personaId IS NULL OR v.idPersona = :personaId)
          AND (:estado IS NULL OR v.estadoEvaluacion = :estado)
          AND (:periodoSemestral IS NULL OR v.periodoSemestral = :periodoSemestral)
          AND (:desde IS NULL OR v.fechaEvaluacion >= :desde)
          AND (:hasta IS NULL OR v.fechaEvaluacion <= :hasta)
        """)
    List<EvaluacionNutricional> buscar(@Param("personaId") Long personaId,
                                       @Param("estado") EstadoEvaluacionNutricional estado,
                                       @Param("periodoSemestral") String periodoSemestral,
                                       @Param("desde") LocalDate desde,
                                       @Param("hasta") LocalDate hasta,
                                       Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalResumenDto(
            v.id, v.idPersona, v.fechaEvaluacion, v.estadoEvaluacion, v.periodoSemestral)
        FROM EvaluacionNutricional v
        WHERE (:personaId IS NULL OR v.idPersona = :personaId)
          AND (:estado IS NULL OR v.estadoEvaluacion = :estado)
          AND (:periodoSemestral IS NULL OR v.periodoSemestral = :periodoSemestral)
          AND (:desde IS NULL OR v.fechaEvaluacion >= :desde)
          AND (:hasta IS NULL OR v.fechaEvaluacion <= :hasta)
        """)
    List<EvaluacionNutricionalResumenDto> buscarResumen(@Param("personaId") Long personaId,
                                                        @Param("estado") EstadoEvaluacionNutricional estado,
                                                        @Param("periodoSemestral") String periodoSemestral,
                                                        @Param("desde") LocalDate desde,
                                                        @Param("hasta") LocalDate hasta,
                                                        Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.saludablemente.nutricional.dto.EvaluacionNutricionalAgregadoDto(
            COUNT(v), COALESCE(AVG(d.imc), 0.0), COALESCE(AVG(d.porcentajeGrasa), 0.0))
        FROM EvaluacionNutricional v
        LEFT JOIN v.detalleAntropometrico d
        WHERE (:personaId IS NULL OR v.idPersona = :personaId)
          AND (:estado IS NULL OR v.estadoEvaluacion = :estado)
        """)
    EvaluacionNutricionalAgregadoDto agregados(@Param("personaId") Long personaId,
                                               @Param("estado") EstadoEvaluacionNutricional estado);
}