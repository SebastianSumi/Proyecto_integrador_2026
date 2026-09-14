package pe.edu.upeu.saludablemente.auditoria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;
import pe.edu.upeu.saludablemente.auditoria.repository.BitacoraTransaccionalRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerIntegrityVerifierServiceImpl implements LedgerIntegrityVerifierService {

    private final BitacoraTransaccionalRepository bitacoraRepository;
    private final HashLedgerService hashLedgerService;

    @Override
    public ResultadoVerificacion verificarCadenaCompleta() {
        log.info("Verificando integridad de la cadena completa");
        return verificarDesde(0L);
    }

    @Override
    public ResultadoVerificacion verificarDesde(Long secuenciaInicio) {
        List<BitacoraTransaccionalEntity> registros = bitacoraRepository.findBySecuenciaAfter(
                secuenciaInicio != null ? secuenciaInicio : 0L);

        if (registros.isEmpty()) {
            return new ResultadoVerificacion(true, 0, List.of(),
                    "No hay registros para verificar");
        }

        List<Long> secuenciasRotas = new ArrayList<>();
        String hashAnteriorEsperado;

        if (secuenciaInicio != null && secuenciaInicio > 0) {
            var registroAnterior = bitacoraRepository.findBySecuenciaAfter(secuenciaInicio - 1);
            hashAnteriorEsperado = registroAnterior.isEmpty()
                    ? hashLedgerService.obtenerHashGenesis()
                    : registroAnterior.get(0).getHashRegistro();
        } else {
            hashAnteriorEsperado = hashLedgerService.obtenerHashGenesis();
        }

        for (BitacoraTransaccionalEntity registro : registros) {
            String hashAnteriorRegistro = registro.getHashAnterior();

            if (!hashAnteriorEsperado.equals(hashAnteriorRegistro)) {
                log.error("Cadena rota en secuencia {}. Hash anterior esperado: {}, encontrado: {}",
                        registro.getSecuencia(), hashAnteriorEsperado, hashAnteriorRegistro);
                secuenciasRotas.add(registro.getSecuencia());
            }

            String payload = construirPayloadHash(registro);
            String hashRecalculado = hashLedgerService.calcularHashSha256(payload + hashAnteriorRegistro);

            if (!hashRecalculado.equals(registro.getHashRegistro())) {
                log.error("Hash invalido en secuencia {}. Esperado: {}, Calculado: {}",
                        registro.getSecuencia(), registro.getHashRegistro(), hashRecalculado);
                if (!secuenciasRotas.contains(registro.getSecuencia())) {
                    secuenciasRotas.add(registro.getSecuencia());
                }
            }

            hashAnteriorEsperado = registro.getHashRegistro();
        }

        boolean integra = secuenciasRotas.isEmpty();
        String mensaje = integra
                ? "Cadena de auditoria integra. " + registros.size() + " registros verificados"
                : "Se detectaron " + secuenciasRotas.size() + " registros con integridad comprometida";

        if (!integra) {
            log.error("ALERTA CRITICA: {}", mensaje);
        }

        return new ResultadoVerificacion(integra, registros.size(), secuenciasRotas, mensaje);
    }

    @Override
    public boolean verificarRegistro(UUID idBitacora) {
        var registroOpt = bitacoraRepository.findById(
                new BitacoraTransaccionalEntity.BitacoraId(idBitacora, null));

        if (registroOpt.isEmpty()) {
            List<BitacoraTransaccionalEntity> todos = bitacoraRepository.findAll();
            BitacoraTransaccionalEntity encontrado = todos.stream()
                    .filter(r -> idBitacora.equals(r.getIdBitacora()))
                    .findFirst()
                    .orElse(null);
            if (encontrado == null) {
                return false;
            }
            return verificarUnRegistro(encontrado);
        }

        return verificarUnRegistro(registroOpt.get());
    }

    private boolean verificarUnRegistro(BitacoraTransaccionalEntity registro) {
        String payload = construirPayloadHash(registro);
        String hashRecalculado = hashLedgerService.calcularHashSha256(
                payload + registro.getHashAnterior());

        boolean valido = hashRecalculado.equals(registro.getHashRegistro());
        if (!valido) {
            log.error("Registro {} tiene hash invalido", registro.getIdBitacora());
        }
        return valido;
    }

    @Override
    public List<Long> encontrarRegistrosRotos() {
        return verificarCadenaCompleta().getSecuenciasRotas();
    }

    private String construirPayloadHash(BitacoraTransaccionalEntity registro) {
        return String.format("%s|%s|%s|%s|%s|%s",
                registro.getTipoEvento(),
                registro.getEntidadAfectada(),
                registro.getIdEntidad(),
                registro.getTipoOperacion() != null ? registro.getTipoOperacion().name() : null,
                registro.getUsuarioAutor(),
                registro.getDiferencialCambios());
    }
}
