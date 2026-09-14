package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void declaresOptionalOperationalFiltersAndSortForSearch() throws Exception {
        Method method = ActividadRepository.class.getMethod(
                "buscar", EstadoActividad.class, LocalDate.class, LocalDate.class, Sort.class);

        assertEquals(List.class, method.getReturnType());
        assertEquals(ActividadResumen.class, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
        assertNull(method.getAnnotation(EntityGraph.class));

        Query query = method.getAnnotation(Query.class);
        assertNotNull(query);
        String normalizedQuery = query.value().replaceAll("\\s+", " ").trim();
        assertEquals(
                "select new pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen( "
                        + "actividad.id, actividad.nombre, actividad.fecha, actividad.horaInicio, "
                        + "actividad.horaFin, actividad.lugar, actividad.estado) from Actividad actividad "
                        + "where (:estado is null or actividad.estado = :estado) "
                        + "and (:desde is null or actividad.fecha >= :desde) "
                        + "and (:hasta is null or actividad.fecha <= :hasta)",
                normalizedQuery
        );

        Parameter[] parameters = method.getParameters();
        assertEquals("estado", parameters[0].getAnnotation(Param.class).value());
        assertEquals("desde", parameters[1].getAnnotation(Param.class).value());
        assertEquals("hasta", parameters[2].getAnnotation(Param.class).value());
        assertEquals(Sort.class, parameters[3].getType());
        assertNull(parameters[3].getAnnotation(Param.class));
    }

    @Test
    void declaresDateRangeAggregateGroupedByStateWithoutEntityGraph() throws Exception {
        Method method = ActividadRepository.class.getMethod("agregados", LocalDate.class, LocalDate.class);

        assertEquals(List.class, method.getReturnType());
        assertEquals(ActividadAgregado.class, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
        assertNull(method.getAnnotation(EntityGraph.class));

        Query query = method.getAnnotation(Query.class);
        assertNotNull(query);
        String normalizedQuery = query.value().replaceAll("\\s+", " ").trim();
        assertEquals(
                "select new pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado( "
                        + "actividad.estado, count(actividad)) from Actividad actividad "
                        + "where (:desde is null or actividad.fecha >= :desde) "
                        + "and (:hasta is null or actividad.fecha <= :hasta) "
                        + "group by actividad.estado order by actividad.estado",
                normalizedQuery
        );

        Parameter[] parameters = method.getParameters();
        assertEquals("desde", parameters[0].getAnnotation(Param.class).value());
        assertEquals("hasta", parameters[1].getAnnotation(Param.class).value());
        assertFalse(normalizedQuery.contains("inscripciones"));
    }
}
