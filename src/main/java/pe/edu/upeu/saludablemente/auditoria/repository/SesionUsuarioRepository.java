package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.entity.SesionUsuarioEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionUsuarioRepository
        extends JpaRepository<SesionUsuarioEntity, UUID> {

    Optional<SesionUsuarioEntity> findFirstByUsuarioOrderByFechaLoginDesc(String usuario);

    default Optional<SesionUsuarioEntity> findUltimaSesion(String usuario) {
        return findFirstByUsuarioOrderByFechaLoginDesc(usuario);
    }

    List<SesionUsuarioEntity> findByUsuarioOrderByFechaLoginDesc(String usuario);

    List<SesionUsuarioEntity> findByUsuarioAndSesionActiva(String usuario, String sesionActiva);

    @Query("SELECT COUNT(s) FROM SesionUsuarioEntity s " +
           "WHERE s.direccionIp = :ip AND s.fechaLogin > :desde")
    long countByIpAndFechaAfter(@Param("ip") String ip, @Param("desde") LocalDateTime desde);

    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuarioEntity s SET s.sesionActiva = 'N' WHERE s.usuario = :usuario")
    void invalidarSesionesDeUsuario(@Param("usuario") String usuario);
}
