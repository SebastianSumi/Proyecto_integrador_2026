package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class OracleAgendaActividadLock implements AgendaActividadLock {

    private static final String LOCK_PROCEDURE = "{ call SALUDABLEMENTE_OWNER.LOCK_AGENDA_ACTIVIDAD(?, ?) }";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void lock(String lugar, LocalDate fecha) {
        jdbcTemplate.execute(LOCK_PROCEDURE, (CallableStatementCallback<Void>) statement -> {
            statement.setString(1, lugar);
            statement.setDate(2, Date.valueOf(fecha));
            statement.execute();
            return null;
        });
    }
}
