package pe.edu.upeu.saludablemente.personal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.saludablemente.exception.BusinessRuleException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.entity.Persona;
import pe.edu.upeu.saludablemente.personal.mapper.PersonaMapper;
import pe.edu.upeu.saludablemente.personal.repository.CredencialProgramaRepository;
import pe.edu.upeu.saludablemente.personal.repository.PersonaRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonaServiceImplTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CredencialProgramaRepository credencialProgramaRepository;

    @Mock
    private PersonaMapper personaMapper;

    @InjectMocks
    private PersonaServiceImpl service;

    @Test
    void validatesAnExistingActivePersona() {
        Persona persona = new Persona();
        persona.setActivo(true);
        when(personaRepository.findById(7L)).thenReturn(Optional.of(persona));

        assertDoesNotThrow(() -> service.validateActivePersona(7L));
    }

    @Test
    void rejectsAnUnknownPersonaWithNotFound() {
        when(personaRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.validateActivePersona(8L));
    }

    @Test
    void rejectsAnInactivePersonaWithBusinessRuleConflict() {
        Persona persona = new Persona();
        persona.setActivo(false);
        when(personaRepository.findById(9L)).thenReturn(Optional.of(persona));

        assertThrows(BusinessRuleException.class, () -> service.validateActivePersona(9L));
    }
}
