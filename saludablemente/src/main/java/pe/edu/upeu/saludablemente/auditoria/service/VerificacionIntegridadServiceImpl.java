package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.dto.IntegridadLedgerResponseDTO;
import pe.edu.upeu.saludablemente.exception.BitacoraNoEncontradaException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificacionIntegridadServiceImpl implements VerificacionIntegridadService {

    private final LedgerIntegrityVerifierService ledgerVerifier;

    @Override
    public IntegridadLedgerResponseDTO verificarCadenaCompleta() {
        log.info("Verificacion de integridad de la cadena completa solicitada");

        LedgerIntegrityVerifierService.ResultadoVerificacion resultado =
                ledgerVerifier.verificarCadenaCompleta();

        return IntegridadLedgerResponseDTO.builder()
                .integra(resultado.isIntegra())
                .totalRegistrosVerificados(resultado.getTotalRegistros())
                .secuenciasRotas(resultado.getSecuenciasRotas())
                .mensaje(resultado.getMensaje())
                .fechaVerificacion(LocalDateTime.now())
                .build();
    }

    @Override
    public IntegridadLedgerResponseDTO verificarDesde(Long secuenciaInicio) {
        if (secuenciaInicio == null || secuenciaInicio < 0) {
            secuenciaInicio = 0L;
        }

        log.info("Verificacion de integridad desde secuencia {}", secuenciaInicio);

        LedgerIntegrityVerifierService.ResultadoVerificacion resultado =
                ledgerVerifier.verificarDesde(secuenciaInicio);

        return IntegridadLedgerResponseDTO.builder()
                .integra(resultado.isIntegra())
                .totalRegistrosVerificados(resultado.getTotalRegistros())
                .secuenciasRotas(resultado.getSecuenciasRotas())
                .mensaje(resultado.getMensaje())
                .fechaVerificacion(LocalDateTime.now())
                .build();
    }

    @Override
    public IntegridadLedgerResponseDTO verificarRegistroIndividual(UUID idBitacora) {
        if (idBitacora == null) {
            throw new BitacoraNoEncontradaException("El id de bitacora es nulo");
        }

        log.info("Verificacion de integridad del registro {}", idBitacora);

        boolean integro = ledgerVerifier.verificarRegistro(idBitacora);

        return IntegridadLedgerResponseDTO.builder()
                .integra(integro)
                .totalRegistrosVerificados(1)
                .secuenciasRotas(integro ? List.of() : List.of(-1L))
                .idBitacoraVerificado(idBitacora)
                .registroIndividualIntegro(integro)
                .mensaje(integro
                        ? "El registro mantiene su integridad criptografica"
                        : "ALERTA: el registro ha sido alterado o su hash no coincide")
                .fechaVerificacion(LocalDateTime.now())
                .build();
    }
}
