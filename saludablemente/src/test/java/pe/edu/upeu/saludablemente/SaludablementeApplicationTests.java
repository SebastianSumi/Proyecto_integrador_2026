package pe.edu.upeu.saludablemente;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SaludablementeApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    void contextLoadsWithH2TestProfileInsteadOfOracle() throws Exception {
        assertThat(environment.getActiveProfiles()).contains("test");

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:");
            assertThat(connection.getMetaData().getDriverName()).containsIgnoringCase("h2");
        }
    }
}
