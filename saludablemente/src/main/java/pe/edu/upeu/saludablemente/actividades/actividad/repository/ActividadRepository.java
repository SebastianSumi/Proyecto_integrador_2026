package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDateTime;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Actividad a where a.id = :id")
    java.util.Optional<Actividad> findByIdForUpdate(@Param("id") Long id);

    List<Actividad> findAllByState(EstadoActividad state);

    @Query("""
            select (count(a) > 0) from Actividad a
            where a.placeKey = :placeKey
              and a.state <> pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad.CANCELLED
              and a.startAt < :endAt
              and a.endAt > :startAt
              and (:excludedId is null or a.id <> :excludedId)
            """)
    boolean existsOverlapping(
            @Param("placeKey") String placeKey,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludedId") Long excludedId
    );
}
