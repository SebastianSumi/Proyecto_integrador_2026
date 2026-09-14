package pe.edu.upeu.saludablemente.perfil_reporte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pe.edu.upeu.saludablemente.exception.ReporteNoEncontradoException;
import pe.edu.upeu.saludablemente.perfil_reporte.entity.ReportePersonalEntity;
import pe.edu.upeu.saludablemente.perfil_reporte.repository.ReportePersonalRepository;
import pe.edu.upeu.saludablemente.perfil_reporte.storage.StorageAdapter;

import java.io.OutputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingPdfServiceImpl implements StreamingPdfService {

    private final ReportePersonalRepository reporteRepository;
    private final StorageAdapter storageAdapter;

    @Override
    public StreamingResponseBody obtenerStreamingDescarga(UUID idReporte) {
        ReportePersonalEntity reporte = obtenerReporteOFallar(idReporte);
        final String ruta = reporte.getRutaAlmacenamiento();

        return outputStream -> {
            log.debug("Iniciando streaming de reporte {} desde {}", idReporte, ruta);
            storageAdapter.copiarAStream(ruta, outputStream);
            log.debug("Streaming completado para reporte {}", idReporte);
        };
    }

    @Override
    public void escribirPdfDirectoAlStream(UUID idReporte, OutputStream outputStream) {
        ReportePersonalEntity reporte = obtenerReporteOFallar(idReporte);
        storageAdapter.copiarAStream(reporte.getRutaAlmacenamiento(), outputStream);
    }

    @Override
    public long obtenerTamanoArchivo(UUID idReporte) {
        ReportePersonalEntity reporte = obtenerReporteOFallar(idReporte);
        if (reporte.getTamanoBytes() != null) {
            return reporte.getTamanoBytes();
        }
        return storageAdapter.obtenerTamanoArchivo(reporte.getRutaAlmacenamiento());
    }

    @Override
    public String obtenerNombreArchivo(UUID idReporte) {
        ReportePersonalEntity reporte = obtenerReporteOFallar(idReporte);
        return "reporte_salud_" + reporte.getPeriodoSemestral() + "_v"
                + reporte.getVersionReporte() + ".pdf";
    }

    private ReportePersonalEntity obtenerReporteOFallar(UUID idReporte) {
        return reporteRepository.findById(idReporte)
                .orElseThrow(() -> new ReporteNoEncontradoException("Reporte no encontrado: " + idReporte));
    }
}
