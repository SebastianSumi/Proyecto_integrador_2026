package pe.edu.upeu.saludablemente.metas.meta.entity;

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
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaTest {

    @Test
    void initializesWithEnCursoState() {
        Meta meta = new Meta();

        assertEquals(EstadoMeta.EN_CURSO, meta.getEstado());
    }

    @Test
    void exposesPersistentFields() {
        Meta meta = new Meta();

        meta.setId(1L);
        meta.setPersonaId(8L);
        meta.setTipoMeta("Peso");
        meta.setDescripcion("Alcanzar un peso saludable");
        meta.setValorObjetivo(new BigDecimal("65.50"));
        meta.setValorActual(new BigDecimal("72.00"));
        meta.setFechaInicio(LocalDate.of(2026, 9, 13));
        meta.setFechaLimite(LocalDate.of(2026, 12, 13));

        assertEquals(1L, meta.getId());
        assertEquals(8L, meta.getPersonaId());
        assertEquals("Peso", meta.getTipoMeta());
        assertEquals("Alcanzar un peso saludable", meta.getDescripcion());
        assertEquals(new BigDecimal("65.50"), meta.getValorObjetivo());
        assertEquals(new BigDecimal("72.00"), meta.getValorActual());
        assertEquals(LocalDate.of(2026, 9, 13), meta.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 13), meta.getFechaLimite());
        assertEquals(EstadoMeta.EN_CURSO, meta.getEstado());
    }

    @Test
    void declaresEssentialJpaMapping() throws NoSuchFieldException {
        assertTrue(Meta.class.isAnnotationPresent(Entity.class));

        Table table = Meta.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("METAS", table.name());
        assertEquals("SALUDABLEMENTE_OWNER", table.schema());

        Field id = Meta.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);
        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());

        assertColumn(Meta.class.getDeclaredField("personaId"), false, 255);
        assertColumn(Meta.class.getDeclaredField("tipoMeta"), false, 40);
        assertColumn(Meta.class.getDeclaredField("descripcion"), true, 200);
        assertDecimalColumn(Meta.class.getDeclaredField("valorObjetivo"), false);
        assertDecimalColumn(Meta.class.getDeclaredField("valorActual"), true);
        assertColumn(Meta.class.getDeclaredField("fechaInicio"), false, 255);
        assertColumn(Meta.class.getDeclaredField("fechaLimite"), false, 255);

        Field estado = Meta.class.getDeclaredField("estado");
        assertEquals(EnumType.STRING, estado.getAnnotation(Enumerated.class).value());
        assertColumn(estado, false, 20);
    }

    private void assertColumn(Field field, boolean nullable, int length) {
        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }

    private void assertDecimalColumn(Field field, boolean nullable) {
        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals(nullable, column.nullable());
        assertEquals(6, column.precision());
        assertEquals(2, column.scale());
    }
}
