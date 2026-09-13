package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @EntityGraph(attributePaths = "inscripciones")
    @Query("select actividad from Actividad actividad where actividad.id = :id")
    Optional<Actividad> findDetalleById(@Param("id") Long id);

    @Query("""
            select case when count(actividad) > 0 then true else false end
            from Actividad actividad
            where actividad.lugar = :lugar
                and actividad.fecha = :fecha
                and actividad.horaInicio < :horaFin
                and actividad.horaFin > :horaInicio
                and (:idExcluido is null or actividad.id <> :idExcluido)
            """)
    boolean existeSolapamiento(
            @Param("lugar") String lugar,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("idExcluido") Long idExcluido
    );
}
