package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.EventoAuditoriaSanitizadoDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;

public interface BitacoraPersistenciaService {

    BitacoraTransaccionalEntity persistirEvento(EventoAuditoriaSanitizadoDTO evento);

    void registrarLectura(Long idPersona, String tipoEntidad, String idEntidad, String usuarioLector);
}
