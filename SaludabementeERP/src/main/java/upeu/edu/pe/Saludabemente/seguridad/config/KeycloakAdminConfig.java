package upeu.edu.pe.Saludabemente.seguridad.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cliente administrativo de Keycloak.
 *
 * <p>Crea un bean {@link Keycloak} autenticado con el flujo
 * {@code client_credentials} del cliente {@code saludablemente-backend}.
 * El cliente debe tener el rol {@code Service accounts} habilitado en
 * Keycloak y los roles de {@code realm-management} asignados.</p>
 *
 * <p>Este bean es la única puerta de entrada a Keycloak desde el backend.
 * Solo lo consume {@code KeycloakAdapter}. Ningún otro servicio del
 * módulo Seguridad lo inyecta directamente.</p>
 */
@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}