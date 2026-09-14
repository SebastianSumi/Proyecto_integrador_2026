package pe.edu.upeu.saludablemente.asistencia.service;

import pe.edu.upeu.saludablemente.asistencia.dto.AsistenciaDto;
import java.util.List;

public interface AsistenciaService {
    List<AsistenciaDto> listarTodas();
    List<AsistenciaDto> listarPorActividad(Long idActividad);
    AsistenciaDto registrarAsistencia(AsistenciaDto dto);
    List<AsistenciaDto> sincronizarAsistenciasOffline(List<AsistenciaDto> registrosOffline);
    void eliminar(Long id);
}