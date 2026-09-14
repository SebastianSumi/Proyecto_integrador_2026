package pe.edu.upeu.saludablemente.auditoria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.dto.AuditContextDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.DiferencialJsonDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.EventoAuditoriaSanitizadoDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.HashChainResultDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraLecturaEntity;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;
import pe.edu.upeu.saludablemente.auditoria.repository.BitacoraLecturaRepository;
import pe.edu.upeu.saludablemente.auditoria.repository.BitacoraTransaccionalRepository;
import pe.edu.upeu.saludablemente.exception.EventoAuditoriaInvalidoException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BitacoraPersistenciaServiceImpl implements BitacoraPersistenciaService {

    private static final LocalTime HORA_INICIO_LABORAL = LocalTime.of(7, 0);
    private static final LocalTime HORA_FIN_LABORAL = LocalTime.of(19, 0);

    private final BitacoraTransaccionalRepository bitacoraRepository;
    private final BitacoraLecturaRepository bitacoraLecturaRepository;
    private final JsonDiffCalculatorService jsonDiffCalculatorService;
    private final HashLedgerService hashLedgerService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BitacoraTransaccionalEntity persistirEvento(EventoAuditoriaSanitizadoDTO evento) {
        if (evento == null) {
            throw new EventoAuditoriaInvalidoException("Evento nulo recibido para persistencia");
        }

        DiferencialJsonDTO diferencial = jsonDiffCalculatorService.calcularDiferencial(
                evento.getEstadoAnterior(), evento.getEstadoNuevo());

        String diferencialJson = jsonDiffCalculatorService.serializarDiferencial(diferencial);
        String snapshotAnteriorJson = jsonDiffCalculatorService.serializarMapa(evento.getEstadoAnterior());
        String snapshotPosteriorJson = jsonDiffCalculatorService.serializarMapa(evento.getEstadoNuevo());

        String payloadParaHash = construirPayloadHash(evento, diferencialJson);
        HashChainResultDTO cadena = hashLedgerService.calcularSello(payloadParaHash);

        AuditContextDTO contexto = evento.getContextoHttp();

        BitacoraTransaccionalEntity entidad = BitacoraTransaccionalEntity.builder()
                .idBitacora(UUID.randomUUID())
                .fechaRegistro(evento.getTimestampEvento() != null
                        ? evento.getTimestampEvento() : LocalDateTime.now())
                .secuencia(cadena.getSecuencia())
                .tipoEvento(evento.getTipoEvento())
                .entidadAfectada(evento.getTipoEntidad())
                .idEntidad(evento.getIdEntidad())
                .tipoOperacion(parsearTipoOperacion(evento.getTipoOperacion()))
                .idPersona(evento.getIdPersona())
                .usuarioAutor(evento.getUsuarioAutor() != null
                        ? evento.getUsuarioAutor() : "sistema")
                .rolUsuario(contexto != null ? contexto.getRoles() : null)
                .tenantId(contexto != null ? contexto.getTenantId() : null)
                .diferencialCambios(diferencialJson)
                .snapshotAnterior(snapshotAnteriorJson)
                .snapshotPosterior(snapshotPosteriorJson)
                .direccionIp(contexto != null ? contexto.getDireccionIp() : null)
                .puertoRemoto(contexto != null ? contexto.getPuertoRemoto() : null)
                .userAgent(contexto != null ? contexto.getUserAgent() : null)
                .endpointHttp(contexto != null ? contexto.getUri() : null)
                .metodoHttp(contexto != null ? contexto.getMetodoHttp() : null)
                .hashRegistro(cadena.getHashRegistro())
                .hashAnterior(cadena.getHashAnterior())
                .fueraHorarioLaboral(esFueraHorarioLaboral(evento.getTimestampEvento()) ? "S" : "N")
                .build();

        BitacoraTransaccionalEntity guardado = bitacoraRepository.save(entidad);

        log.info("Bitacora persistida. Secuencia: {}, Entidad: {}, Operacion: {}, Hash: {}...",
                guardado.getSecuencia(), guardado.getEntidadAfectada(),
                guardado.getTipoOperacion(), guardado.getHashRegistro().substring(0, 12));

        return guardado;
    }

    @Override
    @Transactional
    public void registrarLectura(Long idPersona, String tipoEntidad, String idEntidad, String usuarioLector) {
        if (idPersona == null || tipoEntidad == null || idEntidad == null) {
            return;
        }

        BitacoraLecturaEntity entidad = BitacoraLecturaEntity.builder()
                .idLectura(UUID.randomUUID())
                .fechaAcceso(LocalDateTime.now())
                .idPersona(idPersona)
                .tipoEntidad(tipoEntidad)
                .idEntidad(idEntidad)
                .usuarioLector(usuarioLector != null ? usuarioLector : "desconocido")
                .build();

        bitacoraLecturaRepository.save(entidad);
        log.debug("Lectura registrada: persona={}, entidad={}, id={}",
                idPersona, tipoEntidad, idEntidad);
    }

    private String construirPayloadHash(EventoAuditoriaSanitizadoDTO evento, String diferencialJson) {
        return String.format("%s|%s|%s|%s|%s|%s",
                evento.getTipoEvento(),
                evento.getTipoEntidad(),
                evento.getIdEntidad(),
                evento.getTipoOperacion(),
                evento.getUsuarioAutor(),
                diferencialJson);
    }

    private TipoOperacion parsearTipoOperacion(String valor) {
        if (valor == null) {
            return TipoOperacion.EXECUTE;
        }
        try {
            return TipoOperacion.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TipoOperacion.EXECUTE;
        }
    }

    private boolean esFueraHorarioLaboral(LocalDateTime timestamp) {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        DayOfWeek dia = timestamp.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return true;
        }
        LocalTime hora = timestamp.toLocalTime();
        return hora.isBefore(HORA_INICIO_LABORAL) || hora.isAfter(HORA_FIN_LABORAL);
    }
}
