import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge, TuiButtonLoading } from '@taiga-ui/kit';
import { AuthService } from '../auth/auth-service';
import { homePorRol } from '../auth/roles';
import { ThemeService } from '../theme/theme-service';

@Component({
  selector: 'app-login',
  imports: [RouterLink, TuiBadge, TuiButton, TuiButtonLoading, TuiIcon],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  // Aplica el tema guardado o el del sistema (claro/oscuro) también fuera del layout.
  private readonly tema = inject(ThemeService);
  protected readonly ingresando = signal(false);

  protected esOscuro(): boolean {
    return this.tema.esOscuro();
  }

  protected alternarTema(): void {
    this.tema.alternar();
  }

  protected ingresar(): void {
    if (this.auth.estaAutenticado()) {
      this.router.navigate([homePorRol(this.auth.obtenerRoles())]);
      return;
    }
    this.ingresando.set(true);
    const raw = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
    // Colaborador siempre aterriza en /colaborador; admin respeta returnUrl admin.
    const esAdmin = this.auth.tieneAlgunRol(['Administrador']);
    const destino =
      !esAdmin && raw.startsWith('/admin') ? homePorRol(this.auth.obtenerRoles()) : raw;
    this.auth.iniciarSesion(destino);
  }
}
