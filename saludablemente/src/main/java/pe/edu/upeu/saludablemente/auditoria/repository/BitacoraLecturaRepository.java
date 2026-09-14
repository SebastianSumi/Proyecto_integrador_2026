package pe.edu.upeu.saludablemente.auditoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraLecturaEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface BitacoraLecturaRepository
        extends JpaRepository<BitacoraLecturaEntity, BitacoraLecturaEntity.BitacoraLecturaId> {

    List<BitacoraLecturaEntity> findByIdPersonaOrderByFechaAccesoDesc(Long idPersona);

    List<BitacoraLecturaEntity> findByUsuarioLectorOrderByFechaAccesoDesc(String usuarioLector);

    List<BitacoraLecturaEntity> findByFechaAccesoBetween(LocalDateTime inicio, LocalDateTime fin);
}
