package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActividadRepositoryContractTest {

    @Test
    void declaresASeparateEntityGraphForTheDetailedActivityView() throws Exception {
        Method method = ActividadRepository.class.getMethod("findDetalleById", Long.class);

        EntityGraph entityGraph = method.getAnnotation(EntityGraph.class);
        Query query = method.getAnnotation(Query.class);

        assertNotNull(entityGraph);
        assertArrayEquals(new String[]{"inscripciones"}, entityGraph.attributePaths());
        assertNotNull(query);
        assertEquals("select actividad from Actividad actividad where actividad.id = :id", query.value().trim());
    }
}
