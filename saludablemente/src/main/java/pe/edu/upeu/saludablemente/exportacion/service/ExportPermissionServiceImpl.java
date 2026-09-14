package pe.edu.upeu.saludablemente.exportacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pe.edu.upeu.saludablemente.exception.AccesoDenegadoExportacionException;
import pe.edu.upeu.saludablemente.exportacion.dto.FiltroPoblacionalParams;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportPermissionServiceImpl implements ExportPermissionService {

    private static final String ROL_ADMIN = "ROLE_ADMIN";
    private static final String ROL_EPIDEMIOLOGO = "ROLE_EPIDEMIOLOGO";
    private static final String ROL_DIRECTOR_MEDICO = "ROLE_DIRECTOR_MEDICO";
    private static final String ROL_EVALUADOR = "ROLE_EVALUADOR";

    private static final Set<String> ROLES_CON_ACCESO_GLOBAL = Set.of(
            ROL_ADMIN, ROL_EPIDEMIOLOGO, ROL_DIRECTOR_MEDICO);

    @Override
    public void verificarPermisos(Long idUsuario, FiltroPoblacionalParams filtros) {
        if (idUsuario == null) {
            throw new AccesoDenegadoExportacionException("Usuario no identificado");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Sin autenticacion en SecurityContext. Verificacion de permisos omitida.");
            return;
        }

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean tieneRolPermitido = roles.stream().anyMatch(ROLES_CON_ACCESO_GLOBAL::contains)
                || roles.contains(ROL_EVALUADOR)
                || roles.contains(ROL_ADMIN);

        if (!tieneRolPermitido) {
            log.warn("Usuario {} sin roles de exportacion. Roles: {}", idUsuario, roles);
            throw new AccesoDenegadoExportacionException(
                    "El usuario no tiene rol autorizado para exportar datasets");
        }

        log.debug("Permisos verificados para usuario {} con roles {}", idUsuario, roles);
    }

    @Override
    public List<String> obtenerSedesPermitidas(Long idUsuario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.stream().anyMatch(ROLES_CON_ACCESO_GLOBAL::contains)) {
            return null;
        }

        return null;
    }
}
