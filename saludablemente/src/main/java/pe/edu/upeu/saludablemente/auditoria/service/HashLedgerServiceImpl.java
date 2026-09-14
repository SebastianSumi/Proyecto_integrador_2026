package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.dto.HashChainResultDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;
import pe.edu.upeu.saludablemente.auditoria.repository.BitacoraTransaccionalRepository;
import pe.edu.upeu.saludablemente.exception.HashCalculationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashLedgerServiceImpl implements HashLedgerService {

    private static final String ALGORITMO = "SHA-256";
    private static final String HASH_GENESIS =
            "GENESIS_HASH_0000000000000000000000000000000000000000000000000000000000000000";

    private final BitacoraTransaccionalRepository bitacoraRepository;

    @Override
    public synchronized HashChainResultDTO calcularSello(String payloadJson) {
        if (payloadJson == null) {
            payloadJson = "";
        }

        Optional<BitacoraTransaccionalEntity> ultimo = bitacoraRepository.findUltimoRegistro();

        String hashAnterior;
        Long secuenciaActual;

        if (ultimo.isPresent()) {
            hashAnterior = ultimo.get().getHashRegistro();
            secuenciaActual = ultimo.get().getSecuencia() + 1;
        } else {
            hashAnterior = HASH_GENESIS;
            secuenciaActual = 1L;
        }

        String contenidoAHashear = payloadJson + hashAnterior;
        String hashRegistro = calcularHashSha256(contenidoAHashear);

        return HashChainResultDTO.builder()
                .hashRegistro(hashRegistro)
                .hashAnterior(hashAnterior)
                .secuencia(secuenciaActual)
                .build();
    }

    @Override
    public String calcularHashSha256(String contenido) {
        if (contenido == null) {
            contenido = "";
        }
        return calcularHashSha256(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String calcularHashSha256(byte[] contenido) {
        if (contenido == null) {
            contenido = new byte[0];
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            byte[] hash = digest.digest(contenido);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algoritmo SHA-256 no disponible", e);
            throw new HashCalculationException("Algoritmo SHA-256 no disponible", e);
        }
    }

    @Override
    public String obtenerHashGenesis() {
        return HASH_GENESIS;
    }

    @Override
    public boolean verificarHash(String payload, String hashEsperado) {
        if (payload == null || hashEsperado == null) {
            return false;
        }
        String hashCalculado = calcularHashSha256(payload);
        return hashCalculado.equals(hashEsperado);
    }
}
