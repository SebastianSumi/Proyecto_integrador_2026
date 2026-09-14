package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.IntegridadLedgerResponseDTO;

import java.util.UUID;

public interface VerificacionIntegridadService {

    IntegridadLedgerResponseDTO verificarCadenaCompleta();

    IntegridadLedgerResponseDTO verificarDesde(Long secuenciaInicio);

    IntegridadLedgerResponseDTO verificarRegistroIndividual(UUID idBitacora);
}
