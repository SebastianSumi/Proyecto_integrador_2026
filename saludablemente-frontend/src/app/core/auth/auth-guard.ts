import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';

@Injectable({ providedIn: 'root' })
export class AuthGuard extends KeycloakAuthGuard {
  constructor(
    protected override readonly router: Router,
    protected readonly keycloak: KeycloakService
  ) {
    super(router, keycloak);
  }

  async isAccessAllowed(
      _route: ActivatedRouteSnapshot,
      state: RouterStateSnapshot
  ): Promise<boolean> {
      if (!this.authenticated) {
        // Puerta de entrada propia: el usuario ve /login primero y desde ahí
        // va a Keycloak (SSO intacto). Se conserva la URL pedida para volver.
        await this.router.navigate(['/login'], {
          queryParams: state.url && state.url !== '/' ? { returnUrl: state.url } : {},
        });
        return false;
      }
      return true;
  }
}
