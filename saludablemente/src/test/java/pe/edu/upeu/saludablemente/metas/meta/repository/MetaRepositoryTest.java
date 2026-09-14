package pe.edu.upeu.saludablemente.metas.meta.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.metas.meta.entity.Meta;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaRepositoryTest {

    @Test
    void exposesJpaRepositoryContractAndLookupByParentPersona() throws NoSuchMethodException {
        assertTrue(JpaRepository.class.isAssignableFrom(MetaRepository.class));
        assertEquals(1, MetaRepository.class.getDeclaredMethods().length);

        Method lookup = MetaRepository.class.getMethod("findByPersonaId", Long.class);

        assertEquals(List.class, lookup.getReturnType());
        assertArrayEquals(new Class<?>[]{Long.class}, lookup.getParameterTypes());
    }
}

