package pe.edu.upeu.saludablemente.personal.persona.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.personal.persona.entity.Persona;
import java.util.List;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    @Override
    @EntityGraph(attributePaths = "team")
    List<Persona> findAll();

    List<Persona> findByTeamId(Long teamId);
}
