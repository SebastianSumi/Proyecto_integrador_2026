package pe.edu.upeu.saludablemente.personal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.exception.BusinessRuleException;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;
import pe.edu.upeu.saludablemente.personal.dto.PersonaRequestDto;
import pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto;
import pe.edu.upeu.saludablemente.personal.entity.CredencialPrograma;
import pe.edu.upeu.saludablemente.personal.entity.EstadoCredencial;
import pe.edu.upeu.saludablemente.personal.entity.Persona;
import pe.edu.upeu.saludablemente.personal.entity.PreferenciaComunicacion;
import pe.edu.upeu.saludablemente.personal.entity.Sexo;
import pe.edu.upeu.saludablemente.personal.entity.TipoCredencial;
import pe.edu.upeu.saludablemente.personal.mapper.PersonaMapper;
import pe.edu.upeu.saludablemente.personal.repository.CredencialProgramaRepository;
import pe.edu.upeu.saludablemente.personal.repository.PersonaRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private static final String ALGORITMO_HASH = "SHA-256";

    private final PersonaRepository personaRepository;
    private final CredencialProgramaRepository credencialProgramaRepository;
    private final PersonaMapper personaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponseDto> listar(Boolean activo, String nombres, String celular) {
        return personaRepository.buscar(activo, nombres, celular, Sort.by("id"))
                .stream()
                .map(personaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponseDto obtener(Long idPersona) {
        return personaMapper.toResponse(buscarOFallar(idPersona));
    }

    @Override
    @Transactional
    public PersonaResponseDto createPersona(PersonaRequestDto request) {
        validarCelularNoDuplicado(request.getCelular(), null);

        Persona persona = personaMapper.toEntity(request);
        persona.setActivo(true);

        PreferenciaComunicacion preferencia = new PreferenciaComunicacion();
        preferencia.setCanalPreferido("WhatsApp");
        preferencia.setHorarioContactoInicio(LocalTime.of(8, 0));
        preferencia.setHorarioContactoFin(LocalTime.of(18, 0));
        preferencia.setAceptaRecordatorios(true);
        preferencia.setPersona(persona);
        persona.setPreferenciaComunicacion(preferencia);

        CredencialPrograma credencial = new CredencialPrograma();
        credencial.setCodigoQrHash(generarQrHashUnico());
        credencial.setTipoCredencial(TipoCredencial.GENERAL);
        credencial.setFechaEmision(LocalDateTime.now());
        credencial.setEstado(EstadoCredencial.ACTIVA);
        credencial.setPersona(persona);
        persona.getCredenciales().add(credencial);

        return personaMapper.toResponse(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public PersonaResponseDto actualizar(Long idPersona, PersonaRequestDto request) {
        Persona persona = buscarOFallar(idPersona);
        if (Boolean.FALSE.equals(persona.getActivo())) {
            throw new BusinessRuleException("No se puede actualizar la persona con id " + idPersona + " porque se encuentra inactiva o anonimizada");
        }
        validarCelularNoDuplicado(request.getCelular(), idPersona);

        persona.setIdTeam(request.getIdTeam());
        persona.setNombres(request.getNombres());
        persona.setApellidoPaterno(request.getApellidoPaterno());
        persona.setApellidoMaterno(request.getApellidoMaterno());
        persona.setCelular(request.getCelular());
        persona.setFechaNacimiento(request.getFechaNacimiento());
        persona.setSexo(request.getSexo() == null ? null : Sexo.valueOf(request.getSexo()));
        persona.setTallaPolo(request.getTallaPolo());
        return personaMapper.toResponse(personaRepository.save(persona));
    }

    @Override
    @Transactional
    public void desactivarYAnonimizar(Long idPersona) {
        Persona persona = buscarOFallar(idPersona);
        if (Boolean.FALSE.equals(persona.getActivo())) {
            throw new BusinessRuleException("La persona con id " + idPersona + " ya se encuentra inactiva");
        }

        persona.setActivo(false);
        persona.setNombres("ANONIMO");
        persona.setApellidoPaterno("ANONIMO");
        persona.setApellidoMaterno("ANONIMO");
        persona.setCelular(String.format("ANON-%08d", idPersona));
        persona.setFechaNacimiento(null);
        persona.setSexo(null);
        persona.setTallaPolo(null);
        persona.setIdTeam(null);

        persona.getCredenciales().forEach(credencial -> credencial.setEstado(EstadoCredencial.ANONIMIZADA));
        personaRepository.save(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponseDto identificarPorQr(String qrHash) {
        CredencialPrograma credencial = credencialProgramaRepository.findByCodigoQrHash(qrHash)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial QR no encontrada para el hash " + qrHash));

        if (credencial.getEstado() != EstadoCredencial.ACTIVA) {
            throw new BusinessRuleException("La credencial QR no se encuentra ACTIVA");
        }

        Persona persona = credencial.getPersona();
        if (persona == null || Boolean.FALSE.equals(persona.getActivo())) {
            throw new BusinessRuleException("El colaborador asociado a la credencial no se encuentra activo");
        }
        return personaMapper.toResponse(persona);
    }

    private Persona buscarOFallar(Long idPersona) {
        return personaRepository.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + idPersona));
    }

    private void validarCelularNoDuplicado(String celular, Long idAExcluir) {
        Optional<Persona> existente = personaRepository.findByCelularAndActivoTrue(celular);
        if (existente.isPresent() && !existente.get().getId().equals(idAExcluir)) {
            throw new BusinessRuleException("Ya existe una persona activa registrada con el celular " + celular);
        }
    }

    private String generarQrHashUnico() {
        for (int intento = 0; intento < 5; intento++) {
            String hash = sha256(UUID.randomUUID().toString());
            if (!credencialProgramaRepository.existsByCodigoQrHash(hash)) {
                return hash;
            }
        }
        throw new BusinessRuleException("No se pudo generar un Hash QR unico, reintente la operacion");
    }

    private String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO_HASH);
            byte[] bytes = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash no disponible: " + ALGORITMO_HASH, e);
        }
    }
}