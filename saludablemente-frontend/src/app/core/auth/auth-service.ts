import { Injectable, inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { normalizarRoles, tieneAlgunRol } from './roles';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(KeycloakService);

  iniciarSesion(returnUrl?: string): void {
    const destino = returnUrl && returnUrl.startsWith('/') ? returnUrl : '/';
    this.keycloak.login({ redirectUri: window.location.origin + destino });
  }

  cerrarSesion(): void {
    this.keycloak.logout(window.location.origin);
  }

  estaAutenticado(): boolean {
    return this.keycloak.isLoggedIn();
  }

  obtenerEmail(): string | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed?.['email'] as
      | string
      | undefined;
  }

  obtenerNombre(): string | undefined {
    return this.keycloak.getKeycloakInstance().tokenParsed?.['name'] as
      | string
      | undefined;
  }

  obtenerRoles(): string[] {
    try {
      return this.keycloak.getUserRoles() ?? [];
    } catch {
      return [];
    }
  }

  obtenerRolesNormalizados(): string[] {
    return normalizarRoles(this.obtenerRoles());
  }

  tieneRol(rol: string): boolean {
    return tieneAlgunRol(this.obtenerRoles(), [rol]);
  }

  tieneAlgunRol(roles: string[]): boolean {
    return tieneAlgunRol(this.obtenerRoles(), roles);
  }
}
