package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.ParticionInfoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ParticionamientoOracleService {

    void crearParticionMensual(String tabla, LocalDateTime mes);

    void registrarParticionEnControl(String tabla, String nombreParticion,
                                     LocalDateTime inicio, LocalDateTime fin);

    List<ParticionInfoDTO> listarParticiones(String tabla);

    void sincronizarMetadatosParticiones(String tabla);

    void eliminarParticion(String tabla, String nombreParticion);
}
