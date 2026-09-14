package pe.edu.upeu.saludablemente.exportacion.service;

import pe.edu.upeu.saludablemente.exportacion.dto.IniciarExportacionRequestDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.ResultadoSerializacionDTO;
import pe.edu.upeu.saludablemente.exportacion.dto.TareaIniciadaResponseDTO;

import java.util.UUID;

public interface ExportacionBatchOrchestrator {

    TareaIniciadaResponseDTO iniciarExportacion(IniciarExportacionRequestDTO request,
                                                 Long idUsuario);

    void procesarTarea(UUID idTarea);

    void completarTarea(UUID idTarea, ResultadoSerializacionDTO resultado);

    void marcarFallida(UUID idTarea, Exception causa);

    void cancelarTarea(UUID idTarea);

    byte[] descargarPaquete(UUID idTarea, Long idSolicitante, String ip, String userAgent);
}
