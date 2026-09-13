# Preparación S06: integración de Actividades

## Decisión

La entrega S06 requiere CRUD, una operación cabecera-detalle real, consultas, CORS, logs, pruebas y verificación modular. Actividades se prepara por etapas; este documento no autoriza implementar infraestructura ni inventar una operación compuesta.

## Responsabilidades

| Área | Lugar correcto | No corresponde a |
|---|---|---|
| Límite modular | `actividades/package-info.java` | CORS, reglas HTTP o JPA. |
| DTO de CRUD | `actividades/actividad/dto/` | Entidades JPA o controllers. |
| Cabecera-detalle | DTO compuesto y servicio transaccional del dominio aprobado | Copiar DTOs de Venta sin una regla propia. |
| CORS | Configuración transversal con propiedades por entorno | `package-info.java`, DTO o controller. |
| Logs | Configuración transversal y límites de servicio | Entidades o DTOs. |
| Verificación modular | Prueba Spring Modulith cuando vuelva a existir el arranque integrado | Una afirmación documental. |

## Ruta de construcción

1. DTOs de `Actividad` y su validación: completados.
2. Mapper, repository, servicio y controller: completados. El CRUD actual cubre listar, obtener, crear y actualizar, con DTO validado y respuestas HTTP 400/404/409 centralizadas.
3. `Inscripcion` cuenta con entity, DTO, mapper, repository, service y controller; su registro validado verifica primero que la actividad exista, impide duplicados vigentes con 409 en el flujo normal y su cancelación idempotente se publica como transición, no como `DELETE`. La garantía ante carreras concurrentes queda pendiente de migración Oracle; está diseñada pero no activada en `15-activities-concurrency-oracle-design.md`. Definir con el equipo una operación cabecera-detalle real y su dueño antes de construirla. Actividad–Inscripción aplicará solo si la regla y la atomicidad son evidenciables; Asistencia permanece externa.
4. Añadir CORS por propiedades, logs y prueba de límite modular como trabajo transversal de integración.
5. Con Oracle autorizado por BD2, demostrar CRUD, rollback, consulta combinada/reporte y CORS en vivo.

## Criterio de S06 para Actividades

- No se afirmará que CORS funciona hasta contar con configuración por propiedad y prueba HTTP.
- No se afirmará cabecera-detalle hasta probar una transacción atómica con fallo y rollback.
- No se publicará una relación JPA directa con módulos de compañeros; la colaboración usa contratos públicos.
