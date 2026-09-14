package pe.edu.upeu.saludablemente.perfil_reporte.service;

import pe.edu.upeu.saludablemente.perfil_reporte.dto.PdfGenerationResultDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.PdfMetadataDTO;
import pe.edu.upeu.saludablemente.perfil_reporte.dto.SnapshotDatosDTO;

import java.io.OutputStream;

public interface PdfGeneratorService {

    PdfGenerationResultDTO generarPdf(SnapshotDatosDTO snapshot, PdfMetadataDTO metadata);

    void escribirPdfDirecto(SnapshotDatosDTO snapshot, PdfMetadataDTO metadata, OutputStream outputStream);
}
