package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.ExportacionForenseRequestDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.PaqueteEvidenciaDTO;

public interface ExportadorEvidenciaForenseService {

    PaqueteEvidenciaDTO exportarEvidencia(ExportacionForenseRequestDTO request);
}
