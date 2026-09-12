package pe.edu.upeu.saludablemente.actividades.inscripcion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InscripcionTest {

    @Test
    void initializesWithInscritaState() {
        Inscripcion inscripcion = new Inscripcion();

        assertEquals(EstadoInscripcion.INSCRITA, inscripcion.getEstado());
    }

    @Test
    void exposesCurrentEnrollmentFields() {
        Inscripcion inscripcion = new Inscripcion();
        LocalDateTime inscritaEn = LocalDateTime.of(2026, 9, 12, 8, 30);
        LocalDateTime canceladaEn = LocalDateTime.of(2026, 9, 13, 9, 0);

        inscripcion.setId(1L);
        inscripcion.setActividadId(2L);
        inscripcion.setPersonaId(3L);
        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcion.setInscritaEn(inscritaEn);
        inscripcion.setCanceladaEn(canceladaEn);

        assertEquals(1L, inscripcion.getId());
        assertEquals(2L, inscripcion.getActividadId());
        assertEquals(3L, inscripcion.getPersonaId());
        assertEquals(EstadoInscripcion.CANCELADA, inscripcion.getEstado());
        assertEquals(inscritaEn, inscripcion.getInscritaEn());
        assertEquals(canceladaEn, inscripcion.getCanceladaEn());
    }

    @Test
    void declaresEssentialJpaMapping() throws NoSuchFieldException {
        assertTrue(Inscripcion.class.isAnnotationPresent(Entity.class));

        Table table = Inscripcion.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("INSCRIPCIONES", table.name());
        assertEquals("SALUDABLEMENTE_OWNER", table.schema());

        Field id = Inscripcion.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);
        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());

        assertColumn(Inscripcion.class.getDeclaredField("actividadId"), false, 255);
        assertColumn(Inscripcion.class.getDeclaredField("personaId"), false, 255);
        assertColumn(Inscripcion.class.getDeclaredField("inscritaEn"), false, 255);
        assertColumn(Inscripcion.class.getDeclaredField("canceladaEn"), true, 255);

        Field estado = Inscripcion.class.getDeclaredField("estado");
        assertEquals(EnumType.STRING, estado.getAnnotation(Enumerated.class).value());
        assertColumn(estado, false, 20);
    }

    private void assertColumn(Field field, boolean nullable, int length) {
        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
