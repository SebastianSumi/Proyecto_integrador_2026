# Import de realms Keycloak — regla de nombres (OBLIGATORIA)

`docker-compose.yml` arranca Keycloak con `start-dev --import-realm`, que importa
todo `*.json` de este directorio.

**Regla (Keycloak 26+):** el archivo DEBE llamarse `<realm>-realm.json`, donde
`<realm>` es el valor del campo `"realm"` dentro del JSON.

- Correcto: `saludablemente-realm-dev-realm.json` con `"realm": "saludablemente-realm-dev"`.
- Incorrecto (rompe el arranque): `saludablemente-realm.json` con el mismo contenido.

Si no coincide, Keycloak sale con:

```text
ERROR: File name / realm name mismatch. saludablemente-realm.json,
contains realm saludablemente-realm-dev.
File name should be saludablemente-realm-dev-realm.json
```

y el contenedor entra en `Restarting`. Como el frontend inicializa Keycloak en el
`APP_INITIALIZER`, Angular nunca arranca y la página queda **en blanco** (ni el
login). En Keycloak ≤25 cualquier nombre funcionaba; por eso rompió al subir
la imagen a 26.x sin renombrar el archivo.

## Verificación previa (preflight)

Antes de `docker compose up`, ejecuta desde la raíz del backend:

```powershell
powershell -File keycloak\check-import.ps1
```

Si falla, renombra el archivo como indica el mensaje y vuelve a levantar
(`docker compose up -d --force-recreate keycloak`).
