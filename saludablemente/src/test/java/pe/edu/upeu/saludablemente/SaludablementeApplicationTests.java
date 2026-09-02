package pe.edu.upeu.saludablemente;

import pe.edu.upeu.saludablemente.teams.team.service.TeamService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration")
class SaludablementeApplicationTests {

    @MockitoBean
    private TeamService teamService;

    @Test
    void contextLoads() {
    }
}