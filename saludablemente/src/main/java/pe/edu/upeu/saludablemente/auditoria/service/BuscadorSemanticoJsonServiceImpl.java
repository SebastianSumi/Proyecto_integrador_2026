package pe.edu.upeu.saludablemente.auditoria.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.auditoria.dto.AuditoriaForenseResponseDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.EventoAuditoriaDetalleDTO;
import pe.edu.upeu.saludablemente.auditoria.dto.FiltroAuditoriaForenseRequestDTO;
import pe.edu.upeu.saludablemente.auditoria.entity.BitacoraTransaccionalEntity;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;
import pe.edu.upeu.saludablemente.auditoria.mapper.BitacoraForenseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuscadorSemanticoJsonServiceImpl implements BuscadorSemanticoJsonService {

    private static final List<String> CAMPOS_ORDENAMIENTO_VALIDOS = List.of(
            "secuencia", "fechaRegistro", "usuarioAutor", "entidadAfectada", "tipoOperacion");

    private final BitacoraForenseMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public AuditoriaForenseResponseDTO buscar(FiltroAuditoriaForenseRequestDTO filtro) {
        validarFiltro(filtro);

        int pagina = filtro.getPagina() != null && filtro.getPagina() >= 0 ? filtro.getPagina() : 0;
        int limite = filtro.getLimite() != null && filtro.getLimite() > 0 && filtro.getLimite() <= 500
                ? filtro.getLimite() : 50;

        String ordenamiento = construirOrdenamiento(filtro.getOrdenarPor(), filtro.getDireccion());

        StringBuilder jpql = new StringBuilder("SELECT b FROM BitacoraTransaccionalEntity b WHERE ");
        Map<String, Object> parametros = new HashMap<>();

        construirWhere(jpql, parametros, filtro);

        jpql.append(ordenamiento);

        Query query = entityManager.createQuery(jpql.toString(), BitacoraTransaccionalEntity.class);
        parametros.forEach(query::setParameter);
        query.setFirstResult(pagina * limite);
        query.setMaxResults(limite);

        @SuppressWarnings("unchecked")
        List<BitacoraTransaccionalEntity> resultados = query.getResultList();

        List<EventoAuditoriaDetalleDTO> eventos = resultados.stream()
                .map(mapper::toDetalleDTOSinSnapshots)
                .toList();

        long total = contar(filtro);
        int totalPaginas = (int) Math.ceil((double) total / limite);

        return AuditoriaForenseResponseDTO.builder()
                .totalRegistros(total)
                .pagina(pagina)
                .totalPaginas(totalPaginas)
                .limite(limite)
                .eventos(eventos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long contar(FiltroAuditoriaForenseRequestDTO filtro) {
        validarFiltro(filtro);

        StringBuilder jpql = new StringBuilder("SELECT COUNT(b) FROM BitacoraTransaccionalEntity b WHERE ");
        Map<String, Object> parametros = new HashMap<>();

        construirWhere(jpql, parametros, filtro);

        Query query = entityManager.createQuery(jpql.toString());
        parametros.forEach(query::setParameter);

        return ((Number) query.getSingleResult()).longValue();
    }

    private void construirWhere(StringBuilder jpql, Map<String, Object> parametros,
                                 FiltroAuditoriaForenseRequestDTO filtro) {
        jpql.append("b.fechaRegistro BETWEEN :fechaInicio AND :fechaFin ");
        parametros.put("fechaInicio", filtro.getFechaInicio());
        parametros.put("fechaFin", filtro.getFechaFin());

        if (filtro.getUsuarioAutor() != null && !filtro.getUsuarioAutor().isBlank()) {
            jpql.append("AND LOWER(b.usuarioAutor) LIKE LOWER(:usuarioAutor) ");
            parametros.put("usuarioAutor", "%" + filtro.getUsuarioAutor() + "%");
        }

        if (filtro.getEntidadAfectada() != null && !filtro.getEntidadAfectada().isBlank()) {
            jpql.append("AND b.entidadAfectada = :entidadAfectada ");
            parametros.put("entidadAfectada", filtro.getEntidadAfectada());
        }

        if (filtro.getIdEntidad() != null && !filtro.getIdEntidad().isBlank()) {
            jpql.append("AND b.idEntidad = :idEntidad ");
            parametros.put("idEntidad", filtro.getIdEntidad());
        }

        if (filtro.getTipoOperacion() != null && !filtro.getTipoOperacion().isBlank()) {
            try {
                TipoOperacion operacion = TipoOperacion.valueOf(filtro.getTipoOperacion().toUpperCase());
                jpql.append("AND b.tipoOperacion = :tipoOperacion ");
                parametros.put("tipoOperacion", operacion);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (filtro.getIdPersona() != null) {
            jpql.append("AND b.idPersona = :idPersona ");
            parametros.put("idPersona", filtro.getIdPersona());
        }

        if (filtro.getDireccionIp() != null && !filtro.getDireccionIp().isBlank()) {
            jpql.append("AND b.direccionIp = :direccionIp ");
            parametros.put("direccionIp", filtro.getDireccionIp());
        }

        if (Boolean.TRUE.equals(filtro.getSoloFueraHorarioLaboral())) {
            jpql.append("AND b.fueraHorarioLaboral = 'S' ");
        }

        if (filtro.getCampoAfectado() != null && !filtro.getCampoAfectado().isBlank()) {
            jpql.append("AND LOWER(CAST(b.diferencialCambios AS String)) LIKE LOWER(:campoAfectado) ");
            parametros.put("campoAfectado", "%\"campo\":\"" + filtro.getCampoAfectado() + "\"%");
        }

        if (filtro.getValorBuscado() != null && !filtro.getValorBuscado().isBlank()) {
            jpql.append("AND LOWER(CAST(b.diferencialCambios AS String)) LIKE LOWER(:valorBuscado) ");
            parametros.put("valorBuscado", "%" + filtro.getValorBuscado() + "%");
        }
    }

    private String construirOrdenamiento(String campo, String direccion) {
        String campoValido = CAMPOS_ORDENAMIENTO_VALIDOS.contains(campo) ? campo : "secuencia";
        String direccionValida = "DESC".equalsIgnoreCase(direccion) ? "DESC" : "ASC";
        return "ORDER BY b." + campoValido + " " + direccionValida;
    }

    private void validarFiltro(FiltroAuditoriaForenseRequestDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException("El filtro no puede ser nulo");
        }
        if (filtro.getFechaInicio() == null || filtro.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
