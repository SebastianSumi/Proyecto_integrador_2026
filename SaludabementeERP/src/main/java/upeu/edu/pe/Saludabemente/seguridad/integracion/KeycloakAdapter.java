package upeu.edu.pe.Saludabemente.seguridad.integracion;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdapter {

    /** Roles de negocio que se gestionan desde Seguridad. */
    private static final Set<String> ROLES_NEGOCIO = Set.of(
            "Administrador", "Coordinador", "Evaluador", "Colaborador"
    );

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.admin.realm}")
    private String realm;

    // -------------------------------------------------------------------
    // Crear
    // -------------------------------------------------------------------

    public void crearUsuario(String email, String rawPassword,
                             String nombres, String apellidos,
                             List<String> roles) {

        RealmResource realmResource = keycloakAdmin.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Si el usuario ya existe → no crear, solo actualizar roles y contraseña
        List<UserRepresentation> existentes = usersResource.searchByUsername(email, true);
        if (!existentes.isEmpty()) {
            String userId = existentes.get(0).getId();
            UserResource userResource = usersResource.get(userId);

            // Actualizar contraseña
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(rawPassword);
            credential.setTemporary(false);
            userResource.resetPassword(credential);

            // Actualizar roles
            List<RoleRepresentation> actuales = userResource.roles().realmLevel().listAll();
            List<RoleRepresentation> aQuitar = actuales.stream()
                    .filter(r -> ROLES_NEGOCIO.contains(r.getName()))
                    .toList();
            if (!aQuitar.isEmpty()) {
                userResource.roles().realmLevel().remove(aQuitar);
            }
            asignarRoles(userResource, realmResource, roles);

            log.info("Usuario ya existía en Keycloak, actualizado: {} (id={})", email, userId);
            return;
        }

        // Si no existe → crear (código original)
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(nombres);
        user.setLastName(apellidos);
        user.setEnabled(true);
        user.setEmailVerified(true);

        Response response = usersResource.create(user);
        if (response.getStatus() != 201) {
            throw new BusinessRuleException(
                    "No se pudo crear el usuario en Keycloak. HTTP " + response.getStatus());
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(rawPassword);
        credential.setTemporary(false);
        usersResource.get(userId).resetPassword(credential);

        asignarRoles(usersResource.get(userId), realmResource, roles);

        log.info("Usuario creado en Keycloak: {} (id={})", email, userId);
    }

    // -------------------------------------------------------------------
    // Actualizar roles
    // -------------------------------------------------------------------

    public void actualizarRoles(String email, List<String> roles) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        UserResource userResource = buscarUsuarioOFallar(email);

        // Quitar solo los roles de negocio (no toca default-roles-*, offline_access, etc.)
        List<RoleRepresentation> actuales = userResource.roles().realmLevel().listAll();
        List<RoleRepresentation> aQuitar = actuales.stream()
                .filter(r -> ROLES_NEGOCIO.contains(r.getName()))
                .toList();
        if (!aQuitar.isEmpty()) {
            userResource.roles().realmLevel().remove(aQuitar);
        }

        // Asignar los nuevos
        asignarRoles(userResource, realmResource, roles);

        log.info("Roles actualizados en Keycloak: {} → {}", email, roles);
    }

    // -------------------------------------------------------------------
    // Actualizar contraseña
    // -------------------------------------------------------------------

    public void actualizarPassword(String email, String rawPassword) {
        UserResource userResource = buscarUsuarioOFallar(email);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(rawPassword);
        credential.setTemporary(false);
        userResource.resetPassword(credential);

        log.info("Contraseña actualizada en Keycloak: {}", email);
    }

    // -------------------------------------------------------------------
    // Habilitar / deshabilitar
    // -------------------------------------------------------------------

    public void deshabilitarUsuario(String email) {
        UserResource userResource = buscarUsuarioOFallar(email);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);
        userResource.update(user);
        log.info("Usuario deshabilitado en Keycloak: {}", email);
    }

    public void habilitarUsuario(String email) {
        UserResource userResource = buscarUsuarioOFallar(email);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(true);
        userResource.update(user);
        log.info("Usuario habilitado en Keycloak: {}", email);
    }

    // -------------------------------------------------------------------
    // Eliminar (para compensación en rollback futuro)
    // -------------------------------------------------------------------

    public void eliminarUsuario(String email) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        List<UserRepresentation> users = realmResource.users()
                .searchByUsername(email, true);
        if (users.isEmpty()) {
            log.warn("No se encontró usuario en Keycloak para eliminar: {}", email);
            return;
        }
        realmResource.users().get(users.get(0).getId()).remove();
        log.info("Usuario eliminado de Keycloak: {}", email);
    }

    // -------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------

    private UserResource buscarUsuarioOFallar(String email) {
        List<UserRepresentation> users = keycloakAdmin.realm(realm)
                .users()
                .searchByUsername(email, true);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Usuario no encontrado en Keycloak: " + email);
        }
        return keycloakAdmin.realm(realm).users().get(users.get(0).getId());
    }

    private void asignarRoles(UserResource userResource, RealmResource realmResource,
                              List<String> roles) {
        if (roles == null || roles.isEmpty()) return;

        List<RoleRepresentation> aAsignar = new ArrayList<>();
        for (String rolNombre : roles) {
            try {
                RoleRepresentation role = realmResource.roles()
                        .get(rolNombre)
                        .toRepresentation();
                aAsignar.add(role);
            } catch (Exception e) {
                throw new BusinessRuleException(
                        "Rol '" + rolNombre + "' no existe en Keycloak");
            }
        }
        userResource.roles().realmLevel().add(aAsignar);
    }
}