package pe.edu.upeu.saludablemente.personal.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.personal.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
