package pe.edu.upeu.saludablemente.auditoria.service;

import pe.edu.upeu.saludablemente.auditoria.dto.HashChainResultDTO;

public interface HashLedgerService {

    HashChainResultDTO calcularSello(String payloadJson);

    String calcularHashSha256(String contenido);

    String calcularHashSha256(byte[] contenido);

    String obtenerHashGenesis();

    boolean verificarHash(String payload, String hashEsperado);
}
