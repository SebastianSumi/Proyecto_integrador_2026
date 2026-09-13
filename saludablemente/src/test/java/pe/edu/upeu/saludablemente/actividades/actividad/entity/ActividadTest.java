package pe.edu.upeu.saludablemente.actividades.actividad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.Inscripcion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActividadTest {

    @Test
    void initializesWithProgramadaState() {
        Actividad actividad = new Actividad();

        assertEquals(EstadoActividad.PROGRAMADA, actividad.getEstado());
    }

    @Test
    void exposesPersistentFields() {
        Actividad actividad = new Actividad();

        actividad.setId(1L);
        actividad.setNombre("Caminata saludable");
        actividad.setFecha(LocalDate.of(2026, 9, 12));
        actividad.setHoraInicio(LocalTime.of(8, 0));
        actividad.setHoraFin(LocalTime.of(9, 0));
        actividad.setLugar("Parque central");
        actividad.setCreadorId(7L);

        assertEquals(1L, actividad.getId());
        assertEquals("Caminata saludable", actividad.getNombre());
        assertEquals(LocalDate.of(2026, 9, 12), actividad.getFecha());
        assertEquals(LocalTime.of(8, 0), actividad.getHoraInicio());
        assertEquals(LocalTime.of(9, 0), actividad.getHoraFin());
        assertEquals("Parque central", actividad.getLugar());
        assertEquals(EstadoActividad.PROGRAMADA, actividad.getEstado());
        assertEquals(7L, actividad.getCreadorId());
    }

    @Test
    void declaresEssentialJpaMapping() throws NoSuchFieldException {
        assertTrue(Actividad.class.isAnnotationPresent(Entity.class));

        Table table = Actividad.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("ACTIVIDADES", table.name());
        assertEquals("SALUDABLEMENTE_OWNER", table.schema());

        Field id = Actividad.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);
        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());

        assertColumn(Actividad.class.getDeclaredField("nombre"), false, 100);
        assertColumn(Actividad.class.getDeclaredField("fecha"), false, 255);
        assertColumn(Actividad.class.getDeclaredField("horaInicio"), false, 255);
        assertColumn(Actividad.class.getDeclaredField("horaFin"), false, 255);
        assertColumn(Actividad.class.getDeclaredField("lugar"), false, 100);
        assertColumn(Actividad.class.getDeclaredField("creadorId"), false, 255);

        Field estado = Actividad.class.getDeclaredField("estado");
        assertEquals(EnumType.STRING, estado.getAnnotation(Enumerated.class).value());
        assertColumn(estado, false, 20);
    }

    @Test
    void exposesLazyEnrollmentCollectionAsInverseRelationship() throws NoSuchFieldException {
        Field inscripciones = Actividad.class.getDeclaredField("inscripciones");
        OneToMany relationship = inscripciones.getAnnotation(OneToMany.class);

        assertNotNull(relationship);
        assertEquals("actividad", relationship.mappedBy());
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertEquals(0, relationship.cascade().length);
        assertTrue(!relationship.orphanRemoval());
        assertEquals(List.class, inscripciones.getType());
        assertTrue(Inscripcion.class.isAssignableFrom(
                (Class<?>) ((java.lang.reflect.ParameterizedType) inscripciones.getGenericType())
                        .getActualTypeArguments()[0]));
    }

    private void assertColumn(Field field, boolean nullable, int length) {
        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
