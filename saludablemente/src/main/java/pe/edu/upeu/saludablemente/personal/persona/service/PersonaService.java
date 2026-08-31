package pe.edu.upeu.saludablemente.personal.persona.service;

import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaRequest;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaResponse;
import java.util.List;

public interface PersonaService {
    List<PersonaResponse> listar();
    PersonaResponse obtener(Long id);
    PersonaResponse crear(PersonaRequest request);
    PersonaResponse actualizar(Long id, PersonaRequest request);
    void eliminar(Long id);
    List<PersonaResponse> listarPorTeam(Long teamId);
}
