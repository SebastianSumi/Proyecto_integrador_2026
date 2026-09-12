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
- `ActividadRequest` y `ActividadResponse` están implementados; request valida nombre, fecha, horarios, lugar y creador, mientras response no expone la entidad JPA.
- `ActividadMapper` usa MapStruct para `ActividadRequest -> Actividad` y `Actividad -> ActividadResponse`; no consulta repositories ni aplica reglas.
- `ActividadRepository` hereda `JpaRepository<Actividad, Long>` y declara solo `existeSolapamiento`, una consulta explícita por lugar, fecha y rango horario con parámetros en orden natural: inicio y fin.
- La prueba comportamental de repository se difiere hasta una infraestructura de persistencia autorizada; no se reemplaza por mocks ni reflexión.
- `ActividadService` expone listar, obtener, crear y actualizar. `ActividadServiceImpl` usa transacciones de escritura, consulta el solapamiento y lanza `ActividadSolapadaException` ante la regla real; su traducción HTTP queda para el controller/handler.
- No se agregan controller, SQL, Oracle ni configuración en este slice.

## Paquetes, `package-info` y CORS

El paquete raíz `actividades` agrupa sus submódulos `actividad` e `inscripcion`. Un futuro `actividades/package-info.java` declara el límite de Spring Modulith y, si se necesita colaboración externa, expone solamente un contrato público explícito. No configura CORS, endpoints ni transacciones.

CORS es infraestructura transversal del backend, no una responsabilidad de `dto/`, `package-info.java` ni de un controller. Para S06 se configurará después mediante propiedades por entorno y una configuración global bajo `/api/**`; no se fijarán orígenes ni credenciales en código. Su evidencia será una prueba HTTP y la propiedad visible por entorno.

El paquete `dto/` de Actividad empezará con `ActividadRequest` y `ActividadResponse`. Si una operación cabecera-detalle real queda aprobada, sus DTO compuestos se nombrarán por el dominio —por ejemplo, `ActividadConInscripcionesRequest`— y no se copiarán nombres como `DetalleVentaRequest` o `VentaAgregado` de BOMERP.

## Verificación

`ActividadTest` cubre estado inicial, accesores y mapeo JPA esencial sin Oracle. `ActividadDtoTest` cubre validación de entrada y respuesta pública. `ActividadMapperTest` cubre ambas direcciones y los campos gestionados por entidad. `ActividadServiceImplTest` cubre lectura, no encontrado, crear, actualizar, transacciones y rechazo de solapamiento. La evidencia de cierre es prueba focalizada 8/8 PASS y suite Maven 42/42 PASS.
