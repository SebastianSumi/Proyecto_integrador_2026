package pe.edu.upeu.saludablemente.perfil_reporte.service;

public interface PdfDigitalSignerService {

    String calcularHashSha256(byte[] contenido);

    String calcularHashSha256(String contenido);

    String generarFirmaDigital(String hashSha256, String idReporte);
}
