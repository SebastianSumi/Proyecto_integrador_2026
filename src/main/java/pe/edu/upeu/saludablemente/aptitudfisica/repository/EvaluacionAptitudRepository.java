package pe.edu.upeu.saludablemente.aptitudfisica.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto;
import pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto;
import pe.edu.upeu.saludablemente.aptitudfisica.entity.EvaluacionAptitud;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EvaluacionAptitudRepository extends JpaRepository<EvaluacionAptitud, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<EvaluacionAptitud> findById(Long id);

    @EntityGraph(attributePaths = "detalles")
    @Query("""
        SELECT v FROM EvaluacionAptitud v
        WHERE (:personaId IS NULL OR v.idPersona = :personaId)
          AND (:sincronizado IS NULL OR v.sincronizado = :sincronizado)
          AND (:desde IS NULL OR v.fechaRegistro >= :desde)
          AND (:hasta IS NULL OR v.fechaRegistro <= :hasta)
        """)
    List<EvaluacionAptitud> buscar(@Param("personaId") Long personaId,
                                   @Param("sincronizado") Boolean sincronizado,
                                   @Param("desde") LocalDate desde,
                                   @Param("hasta") LocalDate hasta,
                                   Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudResumenDto(
            v.id, v.idPersona, v.fechaRegistro, v.puntajeGlobal, v.diagnosticoAptitud, v.sincronizado, SIZE(v.detalles))
        FROM EvaluacionAptitud v
        WHERE (:personaId IS NULL OR v.idPersona = :personaId)
          AND (:sincronizado IS NULL OR v.sincronizado = :sincronizado)
          AND (:desde IS NULL OR v.fechaRegistro >= :desde)
          AND (:hasta IS NULL OR v.fechaRegistro <= :hasta)
        """)
    List<EvaluacionAptitudResumenDto> buscarResumen(@Param("personaId") Long personaId,
                                                    @Param("sincronizado") Boolean sincronizado,
                                                    @Param("desde") LocalDate desde,
                                                    @Param("hasta") LocalDate hasta,
                                                    Sort sort);

    @Query("""
        SELECT new pe.edu.upeu.saludablemente.aptitudfisica.dto.EvaluacionAptitudAgregadoDto(
            COUNT(v), COALESCE(SUM(v.puntajeGlobal), 0BD))
        FROM EvaluacionAptitud v
        WHERE (:personaId IS NULL OR v.idPersona = :personaId)
        """)
    EvaluacionAptitudAgregadoDto agregados(@Param("personaId") Long personaId);
}