package pe.edu.upeu.saludablemente.personal.service;

import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;

import java.util.List;

public interface PersonaService {

    List<PersonaResponseDto> listar(boolean soloActivos);

    PersonaResponseDto obtener(Long idPersona);

    PersonaResponseDto createPersona(PersonaRequestDto request);

    PersonaResponseDto actualizar(Long idPersona, PersonaRequestDto request);

    void desactivarYAnonimizar(Long idPersona);

    PersonaResponseDto identificarPorQr(String qrHash);
}