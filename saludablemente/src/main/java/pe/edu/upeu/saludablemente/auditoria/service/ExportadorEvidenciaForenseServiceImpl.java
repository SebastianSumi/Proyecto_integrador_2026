package pe.edu.upeu.saludablemente.auditoria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.config.RsaKeyConfig;
import pe.edu.upeu.saludablemente.auditoria.dto.AuditoriaForenseResponseDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.CambioAtomicoViewDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.EventoAuditoriaDetalleDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.ExportacionForenseRequestDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.PaqueteEvidenciaDTO;
import pe.edu.upeu.saludablemente.exception.BusinessException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportadorEvidenciaForenseServiceImpl implements ExportadorEvidenciaForenseService {

    private static final DateTimeFormatter FORMATO_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String FORMATO_CSV = "CSV";
    private static final String FORMATO_JSON = "JSON";

    private final BuscadorSemanticoJsonService buscadorService;
    private final HashLedgerService hashLedgerService;
    private final RsaKeyConfig rsaKeyConfig;
    private final ObjectMapper objectMapper;

    @Override
    public PaqueteEvidenciaDTO exportarEvidencia(ExportacionForenseRequestDTO request) {
        validarRequest(request);

        log.info("Exportando evidencia forense en formato {} por {}",
                request.getFormato(), request.getSolicitanteIdentificador());

        AuditoriaForenseResponseDTO resultado = buscadorService.buscar(request.getFiltros());

        byte[] contenido = generarContenido(resultado.getEventos(), request.getFormato());

        String hashSha256 = hashLedgerService.calcularHashSha256(new String(contenido, StandardCharsets.UTF_8));
        String firmaRsa = firmarConRsa(hashSha256);

        String nombreArchivo = String.format("evidencia_auditoria_%s.%s",
                LocalDateTime.now().format(FORMATO_TIMESTAMP),
                request.getFormato().toLowerCase());

        PaqueteEvidenciaDTO paquete = PaqueteEvidenciaDTO.builder()
                .nombreArchivo(nombreArchivo)
                .formato(request.getFormato())
                .contenido(contenido)
                .tamanoBytes(contenido.length)
                .hashSha256(hashSha256)
                .firmaRsa(firmaRsa)
                .fechaGeneracion(LocalDateTime.now())
                .solicitante(request.getSolicitanteIdentificador())
                .justificacionLegal(request.getJustificacionLegal())
                .totalRegistrosIncluidos(resultado.getTotalRegistros())
                .build();

        log.info("Evidencia exportada exitosamente: {} ({} bytes, {} registros)",
                nombreArchivo, contenido.length, resultado.getTotalRegistros());

        return paquete;
    }

    private byte[] generarContenido(List<EventoAuditoriaDetalleDTO> eventos, String formato) {
        if (FORMATO_JSON.equalsIgnoreCase(formato)) {
            return generarJson(eventos);
        }
        if (FORMATO_CSV.equalsIgnoreCase(formato)) {
            return generarCsv(eventos);
        }
        throw new BusinessException("Formato no soportado: " + formato + ". Use CSV o JSON");
    }

    private byte[] generarJson(List<EventoAuditoriaDetalleDTO> eventos) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(eventos);
        } catch (Exception e) {
            log.error("Error al serializar evidencia JSON: {}", e.getMessage(), e);
            throw new BusinessException("Error al generar el archivo de evidencia en formato JSON");
        }
    }

    private byte[] generarCsv(List<EventoAuditoriaDetalleDTO> eventos) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {

            String[] cabecera = {
                    "SECUENCIA", "FECHA_REGISTRO", "USUARIO_AUTOR", "ENTIDAD_AFECTADA",
                    "ID_ENTIDAD", "TIPO_OPERACION", "ID_PERSONA", "DIRECCION_IP",
                    "ENDPOINT", "METODO", "CAMPO", "VALOR_ANTERIOR", "VALOR_NUEVO",
                    "HASH_REGISTRO"
            };
            writer.writeNext(cabecera);

            for (EventoAuditoriaDetalleDTO evento : eventos) {
                if (evento.getCambios() == null || evento.getCambios().isEmpty()) {
                    writer.writeNext(construirFilaCsv(evento, null));
                } else {
                    for (CambioAtomicoViewDTO cambio : evento.getCambios()) {
                        writer.writeNext(construirFilaCsv(evento, cambio));
                    }
                }
            }

            writer.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar evidencia CSV: {}", e.getMessage(), e);
            throw new BusinessException("Error al generar el archivo de evidencia en formato CSV");
        }
    }

    private String[] construirFilaCsv(EventoAuditoriaDetalleDTO evento, CambioAtomicoViewDTO cambio) {
        return new String[]{
                String.valueOf(evento.getSecuencia()),
                evento.getFechaRegistro() != null ? evento.getFechaRegistro().toString() : "",
                valorSeguro(evento.getUsuarioAutor()),
                valorSeguro(evento.getEntidadAfectada()),
                valorSeguro(evento.getIdEntidad()),
                valorSeguro(evento.getTipoOperacion()),
                evento.getIdPersona() != null ? String.valueOf(evento.getIdPersona()) : "",
                valorSeguro(evento.getDireccionIp()),
                valorSeguro(evento.getEndpointHttp()),
                valorSeguro(evento.getMetodoHttp()),
                cambio != null ? valorSeguro(cambio.getCampo()) : "",
                cambio != null ? String.valueOf(cambio.getValorAnterior()) : "",
                cambio != null ? String.valueOf(cambio.getValorNuevo()) : "",
                valorSeguro(evento.getHashRegistro())
        };
    }

    private String firmarConRsa(String hashSha256) {
        try {
            if (rsaKeyConfig.getPrivateKey() == null) {
                log.warn("Clave RSA privada no disponible. La firma sera nula");
                return null;
            }

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(rsaKeyConfig.getPrivateKey());
            signature.update(hashSha256.getBytes(StandardCharsets.UTF_8));
            byte[] firma = signature.sign();

            return Base64.getEncoder().encodeToString(firma);

        } catch (Exception e) {
            log.error("Error al firmar evidencia con RSA: {}", e.getMessage(), e);
            throw new BusinessException("Error al firmar digitalmente el paquete de evidencias");
        }
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private void validarRequest(ExportacionForenseRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de exportacion no puede ser nula");
        }
        if (request.getFiltros() == null) {
            throw new IllegalArgumentException("Los filtros son obligatorios");
        }
        if (request.getFormato() == null || request.getFormato().isBlank()) {
            throw new IllegalArgumentException("El formato es obligatorio");
        }
        if (request.getJustificacionLegal() == null || request.getJustificacionLegal().isBlank()) {
            throw new IllegalArgumentException("La justificacion legal es obligatoria");
        }
    }
}
