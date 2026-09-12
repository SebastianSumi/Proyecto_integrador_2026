package pe.edu.upeu.saludablemente.teams.team.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByActive(boolean active);
}