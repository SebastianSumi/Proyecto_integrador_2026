# Integrar los módulos de Pedro sin romper el backend

Esta guía sirve para integrar Teams, Actividades, Inscripciones, Metas y su soporte transversal en la rama compartida. Primero se preserva un backend arrancable y modular; después se completan las decisiones que dependen del equipo o de BD2.

## Ruta rápida de merge

1. Resolver los conflictos conservando **un solo** `SaludablementeApplication` en el paquete raíz.
2. Conservar **una sola** política CORS transversal en `config/CorsConfig.java`; no trasladarla a controllers.
3. Fusionar `application.properties` sin credenciales y configurar cada ambiente mediante variables `CORS_*` y las propiedades de datasource autorizadas.
4. Conservar `SaludablementeApplicationTest` y ejecutar `mvn test` más `git diff --check` después del merge.
5. Con Oracle autorizado, aplicar y registrar V001/V002 si el responsable de BD2 lo aprueba; luego ejecutar la evidencia en vivo.

## Contenido que debe conservarse

| Elemento | Decisión de integración | Motivo |
|---|---|---|
| Bootstrap | Un único `SaludablementeApplication` bajo `pe.edu.upeu.saludablemente`. | Detecta los componentes actuales y permite el arranque Spring Boot. |
| CORS | Un `CorsFilter` para `/api/**`, configurable por `app.cors.*` y `CORS_*`. | Evita políticas contradictorias y orígenes codificados en controllers. |
| OpenAPI | `OpenApiConfig` con los metadatos de Swagger: título, versión y descripción. | Mejora la presentación del contrato; Springdoc sigue descubriendo endpoints automáticamente. |
| Modularidad | Cada módulo conserva sus capas y solo consume servicios públicos de otros módulos. | Prohíbe acceder al repository de otro módulo. |
| Verificación | `SaludablementeApplicationTest` con `ApplicationModules.of(...).verify()`. | Detecta dependencias físicas inválidas en la topología actual. |

## Checklist obligatorio antes de aprobar el merge

- [ ] Existe un solo bootstrap `@SpringBootApplication`.
- [ ] Existe una sola configuración CORS transversal y ningún `@CrossOrigin` duplicado.
- [ ] `application.properties` no contiene usuarios, contraseñas, URLs privadas ni secretos.
- [ ] `mvn test` pasa y `git diff --check` no informa errores.
- [ ] Los módulos no usan repositories ajenos.
- [ ] La prueba de Spring Modulith permanece incluida y verde.
- [ ] Los cambios Oracle fueron revisados por el responsable BD2; no se ejecutan desde el backend.

## Trabajo compartido o diferido

| Tema | Estado | Acción posterior al merge |
|---|---|---|
| Oracle BD2 y Swagger | Externo al código de Pedro. | Configurar datasource autorizado, arrancar backend y demostrar CRUD desde Swagger. |
| CORS de ambiente | El valor local es Angular `http://localhost:4200`, sin credenciales. | Reemplazar mediante `CORS_*` con el origen y política confirmados del ambiente compartido. |
| V001/V002 | Scripts manuales preparados, no activos por sí mismos. | El responsable Oracle decide, aplica y registra la ejecución; después se realiza prueba concurrente real. |
| Cabecera–detalle | No está implementado porque el agregado real no fue definido. | El equipo debe acordar dueño, DTO compuesto, operación transaccional, cálculo y rollback; no renombrar Actividad–Inscripción para simularlo. |
| Límites Modulith futuros | La verificación cubre la topología actual. | Acordar fronteras explícitas antes de agregar módulos o dependencias nuevas. |

## Qué no hacer

- No crear un segundo bootstrap, `CorsConfig` ni configuración Swagger.
- No agregar credenciales a Git ni ejecutar SQL automático desde la aplicación.
- No crear relaciones JPA ni repositorios cruzados entre módulos por conveniencia.
- No declarar cerrada la evidencia S06 hasta probar Oracle, Swagger, CORS y logs con el backend iniciado.

## Referencias

- [Matriz de trazabilidad S06](rebuild/18-s06-evaluation-traceability.md)
- [Preparación de integración](rebuild/14-s06-integration-readiness.md)
- [Scripts Oracle manuales](../database/oracle/manual-migrations/README.md)
