package pe.edu.upeu.saludablemente.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.saludablemente.noticia.entity.NoticiaEntity;
import pe.edu.upeu.saludablemente.notificacion.entity.NotificacionEntity;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OracleLargeTextMappingTest {

    @Test
    void mapsNewsAndNotificationBodiesAsPortableLobsInsteadOfTextColumns() throws NoSuchFieldException {
        assertLob(NoticiaEntity.class, "contenido");
        assertLob(NotificacionEntity.class, "mensaje");
    }

    private void assertLob(Class<?> entityType, String fieldName) throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        assertNotNull(field.getAnnotation(Lob.class));
        assertEquals("", field.getAnnotation(Column.class).columnDefinition());
    }
}
