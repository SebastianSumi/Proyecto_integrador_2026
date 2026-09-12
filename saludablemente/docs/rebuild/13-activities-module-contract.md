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
└── inscripcion/         # registro previo de una persona en una actividad
    ├── entity/
    ├── dto/
    ├── mapper/
    ├── repository/
    ├── service/
    └── controller/
```

## Entidad revisada

`Actividad` mapea los hechos confirmados: identificador, nombre, fecha, hora de inicio, hora de fin, lugar, estado y creador. Usa `SALUDABLEMENTE_OWNER.ACTIVIDADES`, la misma convención física provisional de `Team`; ese nombre no constituye DDL ni cierra la migración Oracle. `EstadoActividad` inicia en `PROGRAMADA` y contempla `EN_CURSO`, `FINALIZADA` y `CANCELADA`.

El creador se conserva como `creadorId`, no como relación JPA a una entidad de Seguridad inexistente. La futura capa de servicio validará esa dependencia mediante un contrato público. La regla de solapamiento por lugar tampoco vive aún en la entidad: requiere consultas y pertenece a servicio/repository.

`LocalDate` y `LocalTime` preservan la semántica del modelo lógico en Java. No constituyen una decisión de DDL ni resuelven la traducción Oracle de `TIME`, que sigue pendiente.

La entidad no contiene anotaciones Bean Validation ni reglas que consulten persistencia: las restricciones de forma están en `ActividadRequest` y las reglas de negocio en `ActividadService`. El servicio valida `horaInicio < horaFin` antes de consultar solapamientos y devuelve 400 para un intervalo inválido; la regla de solapamiento continúa siendo un conflicto 409.

## Entidad de Inscripción

`Inscripcion` representa el estado actual de la intención previa de participar. Conserva `actividadId` y `personaId` como identificadores escalares: no crea relaciones JPA hacia `Actividad` ni hacia Personal. Sus marcas `inscritaEn` y `canceladaEn` expresan el ciclo vigente o cancelado de la misma inscripción, y `EstadoInscripcion` inicia en `INSCRITA` o puede pasar a `CANCELADA`.

La regla acordada es una sola inscripción vigente por persona y actividad. La cancelación permitirá una futura inscripción; repository y service impondrán esa regla al consultar y actualizar el estado. La entidad no consulta persistencia ni intenta resolver unicidad por sí sola.

## DTO de Inscripción

`InscripcionRequest` acepta exclusivamente `actividadId` y `personaId`, ambos obligatorios y positivos. `InscripcionResponse` expone identificador, IDs, estado y marcas de ciclo; estado, `inscritaEn` y `canceladaEn` permanecen bajo control del backend.

## Límites actuales

- `Inscripcion` es una decisión aprobada dentro de Actividades; entity y DTO están implementados. Mapper, repository, service y controller siguen diferidos.
- `Asistencia` pertenece a Francisco; Actividades no accederá a su repository ni lo modelará como hijo interno.
- `ActividadRequest` y `ActividadResponse` están implementados; request valida nombre, fecha, horarios, lugar y creador, mientras response no expone la entidad JPA.
- `ActividadMapper` usa MapStruct para `ActividadRequest -> Actividad` y `Actividad -> ActividadResponse`; no consulta repositories ni aplica reglas.
- `ActividadRepository` hereda `JpaRepository<Actividad, Long>` y declara solo `existeSolapamiento`, una consulta explícita por lugar, fecha y rango horario con parámetros en orden natural: inicio y fin.
- La prueba comportamental de repository se difiere hasta una infraestructura de persistencia autorizada; no se reemplaza por mocks ni reflexión.
- `ActividadService` expone listar, obtener, crear y actualizar. `ActividadServiceImpl` usa transacciones de escritura, valida el intervalo horario antes de consultar el solapamiento y lanza `ActividadSolapadaException` ante la regla real.
- `ActividadController` publica esas cuatro operaciones en `/api/v1/actividades`; aplica `@Valid` en crear y actualizar para activar las restricciones declaradas en `ActividadRequest` antes de invocar el servicio.
- `GlobalExceptionHandler` traduce `ActividadSolapadaException` a 409 y `HorarioActividadInvalidoException` a 400, sin acoplar esas respuestas al controller. Las validaciones de DTO y recursos ausentes conservan 400 y 404, respectivamente.
- No se agregan SQL, Oracle ni configuración en este slice.

## Paquetes, `package-info` y CORS

El paquete raíz `actividades` agrupa sus submódulos `actividad` e `inscripcion`. Un futuro `actividades/package-info.java` declara el límite de Spring Modulith y, si se necesita colaboración externa, expone solamente un contrato público explícito. No configura CORS, endpoints ni transacciones.

CORS es infraestructura transversal del backend, no una responsabilidad de `dto/`, `package-info.java` ni de un controller. Para S06 se configurará después mediante propiedades por entorno y una configuración global bajo `/api/**`; no se fijarán orígenes ni credenciales en código. Su evidencia será una prueba HTTP y la propiedad visible por entorno.

El paquete `dto/` de Actividad empezará con `ActividadRequest` y `ActividadResponse`. Si una operación cabecera-detalle real queda aprobada, sus DTO compuestos se nombrarán por el dominio —por ejemplo, `ActividadConInscripcionesRequest`— y no se copiarán nombres como `DetalleVentaRequest` o `VentaAgregado` de BOMERP.

## Verificación

`ActividadTest` cubre estado inicial, accesores y mapeo JPA esencial sin Oracle. `InscripcionTest` cubre estado inicial, accesores y mapeo JPA esencial sin Oracle. `ActividadDtoTest` cubre validación de entrada y respuesta pública. `ActividadMapperTest` cubre ambas direcciones y los campos gestionados por entidad. `ActividadServiceImplTest` cubre lectura, no encontrado, crear, actualizar, transacciones, rechazo de solapamiento e intervalo horario inválido. `ActividadControllerTest` cubre las cuatro operaciones HTTP y las respuestas 400, 404 y 409.
