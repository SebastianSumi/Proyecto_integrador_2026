package pe.edu.upeu.saludablemente.perfil_reporte.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.ErrorGeneracionPdfException;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class QrCodeGeneratorServiceImpl implements QrCodeGeneratorService {

    @Override
    public String generarQrBase64(String contenido, int ancho, int alto) {
        if (contenido == null || contenido.isEmpty()) {
            return "";
        }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            log.error("Error al generar codigo QR: {}", e.getMessage(), e);
            throw new ErrorGeneracionPdfException("Error al generar codigo QR", e);
        }
    }
}
