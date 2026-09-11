package pe.edu.upeu.saludablemente.personal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.personal.entity.Persona;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByCelularAndActivoTrue(String celular);

    List<Persona> findByActivoTrue();
}