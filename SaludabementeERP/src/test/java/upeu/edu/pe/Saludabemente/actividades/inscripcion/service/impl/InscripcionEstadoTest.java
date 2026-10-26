package upeu.edu.pe.Saludabemente.actividades.inscripcion.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.entity.ActividadInscritoId;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.repository.ActividadInscritoRepository;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonalActividadesApiService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InscripcionEstadoTest {
    private final ActividadRepository actividades = mock(ActividadRepository.class);
    private final ActividadInscritoRepository inscritos = mock(ActividadInscritoRepository.class);
    private final PersonalActividadesApiService personal = mock(PersonalActividadesApiService.class);
    private final InscripcionServiceImpl servicio = new InscripcionServiceImpl(actividades, inscritos, personal);

    private void estado(String estado) {
        var actividad = new Actividad();
        actividad.setIdActividad(1L);
        actividad.setEstado(estado);
        when(actividades.findById(1L)).thenReturn(Optional.of(actividad));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FINALIZADA", "CANCELADA", "CERRADA", "DESCONOCIDO"})
    void historialNoPermiteNingunaMutacion(String estado) {
        estado(estado);
        assertThrows(BusinessRuleException.class, () -> servicio.inscribirPersonas(1L, List.of(2L)));
        assertThrows(BusinessRuleException.class, () -> servicio.inscribirATodos(1L));
        assertThrows(BusinessRuleException.class, () -> servicio.quitarInscrito(1L, 2L));
        verifyNoInteractions(inscritos, personal);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROGRAMADA", "EN_CURSO"})
    void estadosOperativosPermitenInscribirIndividualmenteSinDuplicados(String estado) {
        estado(estado);
        when(inscritos.existsById(new ActividadInscritoId(1L, 3L))).thenReturn(true);
        assertEquals(1, servicio.inscribirPersonas(1L, List.of(2L, 2L, 3L)));
        verify(personal).validarPersonas(List.of(2L, 2L, 3L));
        verify(inscritos).saveAll(argThat(lista -> {
            var iterador = lista.iterator();
            return iterador.hasNext() && iterador.next().getIdPersona().equals(2L) && !iterador.hasNext();
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROGRAMADA", "EN_CURSO"})
    void estadosOperativosPermitenInscribirATodos(String estado) {
        estado(estado);
        when(personal.listarPersonasActivasIds()).thenReturn(List.of(2L, 3L));
        assertEquals(2, servicio.inscribirATodos(1L));
        verify(inscritos).deleteByIdActividad(1L);
        verify(inscritos).saveAll(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROGRAMADA", "EN_CURSO"})
    void estadosOperativosPermitenQuitar(String estado) {
        estado(estado);
        var id = new ActividadInscritoId(1L, 2L);
        when(inscritos.existsById(id)).thenReturn(true);
        assertTrue(servicio.quitarInscrito(1L, 2L));
        verify(inscritos).deleteById(id);
    }

    @Test
    void actividadInexistenteConservaError404SinEscrituras() {
        when(actividades.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> servicio.inscribirPersonas(1L, List.of(2L)));
        assertThrows(ResourceNotFoundException.class, () -> servicio.inscribirATodos(1L));
        assertThrows(ResourceNotFoundException.class, () -> servicio.quitarInscrito(1L, 2L));
        verifyNoInteractions(inscritos, personal);
    }

    @ParameterizedTest
    @ValueSource(strings = {"FINALIZADA", "CANCELADA"})
    void historialSigueDisponibleParaConsulta(String estado) {
        estado(estado);
        when(inscritos.findByIdActividad(1L)).thenReturn(List.of());
        when(inscritos.countByIdActividad(1L)).thenReturn(5L);
        assertTrue(servicio.listarInscritos(1L).isEmpty());
        assertTrue(servicio.listarInscritosDetalle(1L).isEmpty());
        assertEquals(5L, servicio.contarInscritos(1L));
        verify(inscritos, never()).deleteByIdActividad(anyLong());
        verify(inscritos, never()).deleteById(any());
        verify(inscritos, never()).saveAll(anyList());
    }
}
