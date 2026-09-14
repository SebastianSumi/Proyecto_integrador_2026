package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.AuditoriaForenseResponseDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.FiltroAuditoriaForenseRequestDTO;

public interface BuscadorSemanticoJsonService {

    AuditoriaForenseResponseDTO buscar(FiltroAuditoriaForenseRequestDTO filtro);

    long contar(FiltroAuditoriaForenseRequestDTO filtro);
}
