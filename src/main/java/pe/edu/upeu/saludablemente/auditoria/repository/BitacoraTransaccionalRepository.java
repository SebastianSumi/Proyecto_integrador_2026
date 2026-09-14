package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BitacoraTransaccionalRepository
        extends JpaRepository<BitacoraTransaccionalEntity, BitacoraTransaccionalEntity.BitacoraId> {

    @Query("SELECT b FROM BitacoraTransaccionalEntity b ORDER BY b.secuencia DESC")
    Page<BitacoraTransaccionalEntity> findUltimos(Pageable pageable);

    @Query("SELECT b FROM BitacoraTransaccionalEntity b " +
           "WHERE b.secuencia = (SELECT MAX(b2.secuencia) FROM BitacoraTransaccionalEntity b2)")
    Optional<BitacoraTransaccionalEntity> findUltimoRegistro();

    @Query("SELECT MAX(b.secuencia) FROM BitacoraTransaccionalEntity b")
    Long findMaxSecuencia();

    List<BitacoraTransaccionalEntity> findByEntidadAfectadaAndIdEntidadOrderBySecuenciaAsc(
            String entidadAfectada, String idEntidad);

    List<BitacoraTransaccionalEntity> findByUsuarioAutorOrderByFechaRegistroDesc(String usuarioAutor);

    List<BitacoraTransaccionalEntity> findByIdPersonaOrderByFechaRegistroDesc(Long idPersona);

    @Query("SELECT b FROM BitacoraTransaccionalEntity b " +
           "WHERE b.fechaRegistro BETWEEN :inicio AND :fin " +
           "ORDER BY b.secuencia ASC")
    List<BitacoraTransaccionalEntity> findByRangoFecha(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT b FROM BitacoraTransaccionalEntity b " +
           "WHERE b.secuencia > :secuenciaInicio ORDER BY b.secuencia ASC")
    List<BitacoraTransaccionalEntity> findBySecuenciaAfter(@Param("secuenciaInicio") Long secuenciaInicio);

    @Query("SELECT b FROM BitacoraTransaccionalEntity b " +
           "WHERE b.entidadAfectada = :entidad AND b.idEntidad = :idEntidad " +
           "ORDER BY b.secuencia ASC")
    List<BitacoraTransaccionalEntity> findHistorialEntidad(
            @Param("entidad") String entidad,
            @Param("idEntidad") String idEntidad);

    long countByFueraHorarioLaboral(String fueraHorarioLaboral);
}
