package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.CifradoArchivoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AesExcelEncryptor {

    private final AesZipEncryptor validadorContrasena;

    public byte[] cifrarExcel(byte[] excelPlano, String contrasena) {
        validadorContrasena.validarContrasena(contrasena);
        log.info("Aplicando cifrado protegido por contrasena a Excel de {} bytes", excelPlano.length);

        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(contrasena);

            try (OPCPackage opcPackage = OPCPackage.open(new ByteArrayInputStream(excelPlano))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                opcPackage.save(encryptor.getDataStream(fs));
                fs.writeFilesystem(baos);

                byte[] resultado = baos.toByteArray();
                log.info("Excel cifrado con exito. Tamano final: {} bytes", resultado.length);
                return resultado;
            }

        } catch (Exception e) {
            log.error("Error al cifrar Excel: {}", e.getMessage(), e);
            throw new CifradoArchivoException("Error al cifrar archivo Excel con contrasena", e);
        }
    }
}
