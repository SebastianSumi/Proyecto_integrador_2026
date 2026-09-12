package pe.edu.upeu.saludablemente.teams.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamTest {

    @Test
    void initializesActiveAsTrue() {
        Team team = new Team();

        assertTrue(team.isActive());
    }

    @Test
    void exposesSettersAndGettersForPersistentFields() {
        Team team = new Team();

        team.setId(1L);
        team.setName("Wellness team");
        team.setDescription("Coordinates wellness activities");

        assertEquals(1L, team.getId());
        assertEquals("Wellness team", team.getName());
        assertEquals("Coordinates wellness activities", team.getDescription());
    }

    @Test
    void declaresEssentialJpaMapping() throws NoSuchFieldException {
        assertTrue(Team.class.isAnnotationPresent(Entity.class));

        Table table = Team.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("TEAMS", table.name());
        assertEquals("SALUDABLEMENTE_OWNER", table.schema());

        Field id = Team.class.getDeclaredField("id");
        assertTrue(id.isAnnotationPresent(Id.class));
        GeneratedValue generatedValue = id.getAnnotation(GeneratedValue.class);
        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());
        assertColumn(id, "ID", true, 255);

        assertColumn(Team.class.getDeclaredField("name"), "NAME", false, 60);
        assertColumn(Team.class.getDeclaredField("description"), "DESCRIPTION", true, 200);
        assertColumn(Team.class.getDeclaredField("active"), "ACTIVE", false, 255);
    }

    private void assertColumn(Field field, String name, boolean nullable, int length) {
        Column column = field.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals(name, column.name());
        assertEquals(nullable, column.nullable());
        assertEquals(length, column.length());
    }
}
