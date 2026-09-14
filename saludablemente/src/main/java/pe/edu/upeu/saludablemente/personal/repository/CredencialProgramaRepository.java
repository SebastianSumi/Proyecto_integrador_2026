package pe.edu.upeu.saludablemente.personal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.personal.entity.CredencialPrograma;

import java.util.Optional;

public interface CredencialProgramaRepository extends JpaRepository<CredencialPrograma, Long> {

    Optional<CredencialPrograma> findByCodigoQrHash(String codigoQrHash);

    boolean existsByCodigoQrHash(String codigoQrHash);
}