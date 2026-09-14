package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import java.time.LocalDate;

/**
 * Coordinates schedule writes for one exact place and date within the active database transaction.
 */
public interface AgendaActividadLock {

    void lock(String lugar, LocalDate fecha);
}