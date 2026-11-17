import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';

export const keycloakTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  if (!keycloak.isLoggedIn()) {
    return next(req);
  }

  // Renueva el token si está por expirar (por defecto 20 s de margen).
  // Sin esto, getToken() devuelve el token caducado y el backend responde 401.
  return from(
    keycloak
      .updateToken()
      .catch(() => false)
      .then(() => keycloak.getToken())
  ).pipe(
    switchMap((token) => {
      if (!token) return next(req);
      return next(
        req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
      );
    })
  );
};
