package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;

import java.util.List;

public interface ExportPermissionService {

    void verificarPermisos(Long idUsuario, FiltroPoblacionalParams filtros);

    List<String> obtenerSedesPermitidas(Long idUsuario);
}
