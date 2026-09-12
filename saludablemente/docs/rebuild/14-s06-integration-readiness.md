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
2. Mapper, repository y servicio: completados. Falta controller del CRUD y su contrato HTTP.
3. Definir con el equipo una operación cabecera-detalle real y su dueño: Actividad–Inscripción solo si la regla y la atomicidad son evidenciables; Asistencia permanece externa.
4. Añadir CORS por propiedades, logs y prueba de límite modular como trabajo transversal de integración.
5. Con Oracle autorizado por BD2, demostrar CRUD, rollback, consulta combinada/reporte y CORS en vivo.

## Criterio de S06 para Actividades

- No se afirmará que CORS funciona hasta contar con configuración por propiedad y prueba HTTP.
- No se afirmará cabecera-detalle hasta probar una transacción atómica con fallo y rollback.
- No se publicará una relación JPA directa con módulos de compañeros; la colaboración usa contratos públicos.
