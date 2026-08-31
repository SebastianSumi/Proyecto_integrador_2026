package pe.edu.upeu.saludablemente.personal.persona.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaRequest;
import pe.edu.upeu.saludablemente.personal.persona.dto.PersonaResponse;
import pe.edu.upeu.saludablemente.personal.persona.entity.Persona;
import pe.edu.upeu.saludablemente.personal.persona.mapper.PersonaMapper;
import pe.edu.upeu.saludablemente.personal.persona.repository.PersonaRepository;
import pe.edu.upeu.saludablemente.personal.team.entity.Team;
import pe.edu.upeu.saludablemente.personal.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {
    private final PersonaRepository personaRepository;
    private final TeamRepository teamRepository;
    private final PersonaMapper personaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> listar() {
        return personaRepository.findAll().stream().map(this::toResponseConEdad).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtener(Long id) {
        return toResponseConEdad(buscarOFallar(id));
    }

    @Override
    @Transactional
    public PersonaResponse crear(PersonaRequest request) {
        Team team = buscarTeamOFallar(request.getTeamId());
        Persona persona = personaMapper.toEntity(request, team);
        return toResponseConEdad(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public PersonaResponse actualizar(Long id, PersonaRequest request) {
        Persona persona = buscarOFallar(id);
        Team team = buscarTeamOFallar(request.getTeamId());
        persona.setNombres(request.getNombres());
        persona.setApellidoPaterno(request.getApellidoPaterno());
        persona.setApellidoMaterno(request.getApellidoMaterno());
        persona.setAreaTrabajo(request.getAreaTrabajo());
        persona.setCelular(request.getCelular());
        persona.setFechaNacimiento(request.getFechaNacimiento());
        persona.setTallaPolo(request.getTallaPolo());
        persona.setEstadoCivil(request.getEstadoCivil());
        persona.setNivelEducativo(request.getNivelEducativo());
        persona.setSexo(request.getSexo());
        persona.setTeam(team);
        return toResponseConEdad(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        personaRepository.delete(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> listarPorTeam(Long teamId) {
        buscarTeamOFallar(teamId);
        return personaRepository.findByTeamId(teamId).stream().map(this::toResponseConEdad).toList();
    }

    private Persona buscarOFallar(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + id));
    }

    private Team buscarTeamOFallar(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team no encontrado: " + teamId));
    }

    private PersonaResponse toResponseConEdad(Persona persona) {
        PersonaResponse response = personaMapper.toResponse(persona);
        response.setEdad(calcularEdad(persona.getFechaNacimiento()));
        return response;
    }

    private int calcularEdad(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
