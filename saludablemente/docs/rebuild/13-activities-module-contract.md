# Contrato inicial del módulo Actividades

## Decisión de frontera

Actividades es un módulo transaccional de Pedro. Su agregado raíz es `Actividad`, responsable de programar una actividad. Ser transaccional no convierte automáticamente a `Asistencia` en una entidad interna: Asistencias pertenece a otro módulo y solo puede relacionarse mediante un contrato público futuro.

## Estructura objetivo

```text
actividades/
├── actividad/
│   ├── entity/          # Actividad y EstadoActividad
│   ├── dto/             # contratos HTTP futuros
│   ├── mapper/          # conversiones futuras
│   ├── repository/      # persistencia propia futura
│   ├── service/         # reglas y frontera transaccional futura
│   └── controller/      # API futura bajo /api/v1
└── inscripcion/         # registro previo, pendiente de implementación
    ├── entity/
    ├── dto/
    ├── mapper/
    ├── repository/
    ├── service/
    └── controller/
```

## Entidad cerrada

`Actividad` mapea los hechos confirmados: identificador, nombre, fecha, hora de inicio, hora de fin, lugar, estado y creador. `EstadoActividad` inicia en `PROGRAMADA` y contempla `EN_CURSO`, `FINALIZADA` y `CANCELADA`.

El creador se conserva como `creadorId`, no como relación JPA a una entidad de Seguridad inexistente. La futura capa de servicio validará esa dependencia mediante un contrato público. La regla de solapamiento por lugar tampoco vive aún en la entidad: requiere consultas y pertenece a servicio/repository.

`LocalDate` y `LocalTime` preservan la semántica del modelo lógico en Java. No constituyen una decisión de DDL ni resuelven la traducción Oracle de `TIME`, que sigue pendiente.

## Límites actuales

- `Inscripcion` sigue siendo una decisión aprobada dentro de Actividades, pero no forma parte de esta primera capa.
- `Asistencia` pertenece a Francisco; Actividades no accederá a su repository ni lo modelará como hijo interno.
- No se agregan DTO, mapper, repository, service, controller, SQL, Oracle ni configuración en este slice.

## Verificación

`ActividadTest` cubre estado inicial, accesores y mapeo JPA esencial sin Oracle. La evidencia de cierre es prueba focalizada 3/3 PASS y suite Maven 29/29 PASS.
