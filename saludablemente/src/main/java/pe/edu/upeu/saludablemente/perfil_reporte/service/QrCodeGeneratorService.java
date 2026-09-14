package pe.edu.upeu.saludablemente.perfil_reporte.service;

public interface QrCodeGeneratorService {

    String generarQrBase64(String contenido, int ancho, int alto);
}
