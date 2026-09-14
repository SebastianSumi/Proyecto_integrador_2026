package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;

import java.util.Map;

public interface AuditoriaService {

    /**
     * Registra un cambio (creacion, actualizacion o eliminacion) sobre una entidad.
     * Se invoca desde los servicios de negocio dentro de su propia transaccion.
     */
    void registrarCambio(String tipoEntidad,
                         String idEntidad,
                         TipoOperacion tipoOperacion,
                         Map<String, Object> estadoAnterior,
                         Map<String, Object> estadoNuevo,
                         Long idPersona,
                         String usuarioAutor);

    /**
     * Registra un acceso de lectura a un expediente clinico sensible.
     * Se invoca cuando alguien consulta datos personales de salud.
     */
    void registrarLectura(Long idPersona,
                          String tipoEntidad,
                          String idEntidad,
                          String usuarioLector);

    /**
     * Registra la ejecucion de una inferencia con IA.
     * Congela el prompt enviado, modelo usado, tiempo de inferencia.
     */
    void registrarInferenciaIA(String idRecomendacion,
                               Long idPersona,
                               String modeloIaUsado,
                               String promptContexto,
                               Integer tiempoInferenciaMs);

    /**
     * Registra la descarga de un reporte clinico en PDF.
     */
    void registrarDescargaReporte(String idReporte,
                                  Long idPersona,
                                  Long idSolicitante);
}
