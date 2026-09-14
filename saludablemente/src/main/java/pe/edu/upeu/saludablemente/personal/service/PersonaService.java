package pe.edu.upeu.saludablemente.personal.service;

import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;

import java.util.List;

public interface PersonaService {

    List<PersonaResponseDto> listar(Boolean activo, String nombres, String celular);

    PersonaResponseDto obtener(Long idPersona);

    /**
     * Public cross-module boundary for operations that require an existing active Persona.
     *
     * @param idPersona the logical Persona identifier
     * @throws pe.edu.upeu.saludablemente.exception.ResourceNotFoundException when the Persona does not exist
     * @throws pe.edu.upeu.saludablemente.exception.BusinessRuleException when the Persona is inactive
     */
    void validateActivePersona(Long idPersona);

    PersonaResponseDto createPersona(PersonaRequestDto request);

    PersonaResponseDto actualizar(Long idPersona, PersonaRequestDto request);

    void desactivarYAnonimizar(Long idPersona);

    PersonaResponseDto identificarPorQr(String qrHash);
}
