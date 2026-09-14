package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.Actividad;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @EntityGraph(attributePaths = "inscripciones")
    @Query("select actividad from Actividad actividad where actividad.id = :id")
    Optional<Actividad> findDetalleById(@Param("id") Long id);

    @Query("""
            select new pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen(
                actividad.id, actividad.nombre, actividad.fecha, actividad.horaInicio,
                actividad.horaFin, actividad.lugar, actividad.estado)
            from Actividad actividad
            where (:estado is null or actividad.estado = :estado)
                and (:desde is null or actividad.fecha >= :desde)
                and (:hasta is null or actividad.fecha <= :hasta)
            """)
    List<ActividadResumen> buscar(
            @Param("estado") EstadoActividad estado,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            Sort sort
    );

    @Query("""
            select new pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado(
                actividad.estado, count(actividad))
            from Actividad actividad
            where (:desde is null or actividad.fecha >= :desde)
                and (:hasta is null or actividad.fecha <= :hasta)
            group by actividad.estado
            order by actividad.estado
            """)
    List<ActividadAgregado> agregados(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

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
