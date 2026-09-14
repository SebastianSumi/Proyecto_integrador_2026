package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.CallableStatement;
import java.sql.Date;
import java.time.LocalDate;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OracleAgendaActividadLockTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CallableStatement statement;

    @Test
    @SuppressWarnings("unchecked")
    void delegatesExactScheduleKeyToOracleLockProcedure() throws Exception {
        OracleAgendaActividadLock lock = new OracleAgendaActividadLock(jdbcTemplate);
        LocalDate fecha = LocalDate.of(2026, 9, 12);

        lock.lock("Parque central", fecha);

        ArgumentCaptor<CallableStatementCallback<Void>> callback = ArgumentCaptor.forClass(CallableStatementCallback.class);
        verify(jdbcTemplate).execute(eq("{ call SALUDABLEMENTE_OWNER.LOCK_AGENDA_ACTIVIDAD(?, ?) }"), callback.capture());

        callback.getValue().doInCallableStatement(statement);

        verify(statement).setString(1, "Parque central");
        verify(statement).setDate(2, Date.valueOf(fecha));
        verify(statement).execute();
    }
}
