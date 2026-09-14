package pe.edu.upeu.saludablemente.asistencia.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.asistencia.dto.AsistenciaDto;
import pe.edu.upeu.saludablemente.asistencia.entity.AsistenciaEntity;
import pe.edu.upeu.saludablemente.asistencia.mapper.AsistenciaMapper;
import pe.edu.upeu.saludablemente.asistencia.repository.AsistenciaRepository;
import pe.edu.upeu.saludablemente.asistencia.service.AsistenciaService;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private AsistenciaMapper asistenciaMapper;

    @Override
    public List<AsistenciaDto> listarTodas() {
        return asistenciaRepository.findAll().stream()
                .map(asistenciaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsistenciaDto> listarPorActividad(Long idActividad) {
        return asistenciaRepository.findByIdActividad(idActividad).stream()
                .map(asistenciaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AsistenciaDto registrarAsistencia(AsistenciaDto dto) {
        // Evita duplicados si ya se marcó en la misma actividad para la misma persona (idempotencia)
        var existente = asistenciaRepository.findByIdActividadAndIdPersona(dto.getIdActividad(), dto.getIdPersona());
        if (existente.isPresent()) {
            return asistenciaMapper.toDto(existente.get());
        }

        AsistenciaEntity entity = asistenciaMapper.toEntity(dto);
        if (entity.getFechaHoraMarcado() == null) {
            entity.setFechaHoraMarcado(LocalDateTime.now());
        }

        AsistenciaEntity saved = asistenciaRepository.save(entity);
        return asistenciaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<AsistenciaDto> sincronizarAsistenciasOffline(List<AsistenciaDto> registrosOffline) {
        List<AsistenciaDto> resultadosSincronizados = new ArrayList<>();
        
        for (AsistenciaDto dto : registrosOffline) {
            dto.setEsOffline(true);
            dto.setMetodo("OFFLINE_SYNC");
            AsistenciaDto procesado = registrarAsistencia(dto);
            resultadosSincronizados.add(procesado);
        }
        
        return resultadosSincronizados;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asistencia no encontrada con ID: " + id);
        }
        asistenciaRepository.deleteById(id);
    }
}