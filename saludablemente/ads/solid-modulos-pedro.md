# Principios SOLID en los módulos de Pedro

SOLID guía decisiones sobre responsabilidades, extensibilidad y dependencias. **No** obliga a crear una interfaz por clase ni a multiplicar capas: una abstracción vale cuando protege un límite o una variación real. Los fragmentos incorrectos de este documento enseñan el problema; no se presentan como hallazgos del repositorio.

## Lectura rápida

| Principio | Pregunta de diseño | Evidencia favorable actual |
|---|---|---|
| S - Responsabilidad única | ¿Esta clase tiene un motivo principal para cambiar? | Separación entre controller, DTO, service, mapper, repository y entity |
| O - Abierto/cerrado | ¿Una variación justificada puede agregarse sin romper el núcleo? | Transiciones encapsuladas y contratos separados |
| L - Sustitución de Liskov | ¿Una implementación conserva las promesas de su contrato? | Repositorios Spring Data usados sin invalidar su semántica |
| I - Segregación de interfaces | ¿Cada consumidor recibe solo las capacidades que necesita? | Repositorios y DTOs específicos por subdominio |
| D - Inversión de dependencias | ¿La política recibe colaboradores en vez de construir infraestructura? | Inyección por constructor en los servicios |

## S - Principio de responsabilidad única

### Propósito

Una unidad debe tener una razón principal para cambiar. Validar HTTP, ejecutar reglas de negocio, mapear y persistir responden a cambios diferentes.

### Diseño incorrecto

> **Ejemplo didáctico — no corresponde necesariamente al código actual.**

```java
@RestController
class ActividadController {
    RespuestaActividad crear(SolicitudActividad request) {
        validarHorario(request);
        normalizarLugar(request);
        guardarConJdbc(request);
        enviarNotificacion(request);
        return construirRespuesta(request);
    }
}
```

### Por qué falla

El Controller cambia si cambia HTTP, la regla de horarios, Oracle, el formato de respuesta o el canal de notificación. Además, probar una regla obliga a cargar dependencias ajenas.

### Diseño correcto

```java
@RestController
class ActividadController {
    private final ActividadService actividadService;

    RespuestaActividad crear(SolicitudActividad request) {
        return actividadService.create(request);
    }
}
```

### Aplicación al proyecto

- `ActividadController` adapta HTTP.
- `SolicitudActividad` valida la forma de entrada.
- `ActividadService` coordina horario, solapamiento y transacción.
- `ActividadMapper` traduce DTO y entidad.
- Los repositorios encapsulan consultas y locks.
- `Actividad` protege su ciclo de estados.

Esta separación ya existe. SRP no significa reducir cada clase a un método; significa mantener juntos los cambios que responden a la misma responsabilidad.

## O - Principio abierto/cerrado

### Propósito

El comportamiento estable debería aceptar extensiones justificadas sin exigir editar cada consumidor ni abrir puntos de extensión “por si acaso”.

### Diseño incorrecto

> **Ejemplo didáctico — no corresponde necesariamente al código actual.**

```java
void avisarCambio(EstadoActividad estado) {
    if (estado == EstadoActividad.CANCELLED) {
        enviarCorreo();
    } else if (estado == EstadoActividad.IN_PROGRESS) {
        enviarMensajeInterno();
    }
}
```

### Por qué falla

Cada nuevo canal obliga a modificar la política central. La clase acumula condicionales y conoce detalles de infraestructura.

### Diseño correcto

```java
public interface PublicadorCambioActividad {
    void publicar(CambioEstadoActividad cambio);
}

final class NotificadorCambioActividad {
    private final List<PublicadorCambioActividad> publicadores;

    void notificar(CambioEstadoActividad cambio) {
        publicadores.forEach(p -> p.publicar(cambio));
    }
}
```

### Aplicación al proyecto

`Actividad` ya concentra las transiciones permitidas y puede evolucionar esa política sin alterar Controllers o repositorios. El ejemplo del publicador representa una **posible integración futura** con Notificaciones; esas clases no existen actualmente y no deben agregarse hasta definir el contrato real.

OCP tampoco justifica una estrategia para cada `if`. Si no hay una variación conocida, una solución directa es más clara y menos costosa.

## L - Principio de sustitución de Liskov

### Propósito

Una implementación debe cumplir las precondiciones, poscondiciones y efectos prometidos por su contrato. Sustituirla no debe sorprender al consumidor.

### Diseño incorrecto

> **Ejemplo didáctico — no corresponde necesariamente al código actual.**

```java
class InscripcionRepositorySoloLectura implements InscripcionRepository {
    @Override
    public Inscripcion save(Inscripcion value) {
        throw new UnsupportedOperationException("Solo lectura");
    }
}
```

### Por qué falla

Aunque compile, rompe la expectativa de `JpaRepository`: un consumidor del contrato supone que `save` funciona. La sustitución cambia una capacidad esencial.

### Diseño correcto

```java
interface ConsultaInscripciones {
    List<Inscripcion> buscarPorActividad(Long actividadId);
}

interface EscrituraInscripciones {
    Inscripcion guardar(Inscripcion inscripcion);
}
```

Si realmente existe una variante de solo lectura, debe recibir un contrato que prometa solo consultas. No debe fingir que implementa operaciones que rechazará.

### Aplicación al proyecto

`TeamRepository`, `ActividadRepository` e `InscripcionRepository` extienden Spring Data y agregan consultas sin invalidar el comportamiento heredado. Los mocks de pruebas pueden sustituir esos contratos respetando sus resultados esperados.

No se comprobó una violación LSP en los tres subdominios. Crear una jerarquía artificial de servicios únicamente para “aplicar LSP” agregaría complejidad sin mejorar el diseño.

## I - Principio de segregación de interfaces

### Propósito

Un consumidor no debería depender de operaciones que no necesita. Los contratos deben representar capacidades cohesionadas.

### Diseño incorrecto

> **Ejemplo didáctico — no corresponde necesariamente al código actual.**

```java
interface GestionSaludService {
    TeamResponse crearEquipo(TeamRequest request);
    RespuestaActividad crearActividad(SolicitudActividad request);
    RespuestaInscripcion inscribir(Long actividadId, Long personaId);
    void enviarNotificacion(Long personaId);
}
```

### Por qué falla

Teams, Actividades, Inscripciones y Notificaciones quedarían unidos por una interfaz “Dios”. Un cambio en un subdominio impactaría consumidores que no lo usan.

### Diseño correcto

```java
public interface ConsultaEquipo {
    boolean existeActivo(Long equipoId);
}

public interface RegistroInscripcion {
    RespuestaInscripcion inscribir(Long actividadId, Long personaId);
}
```

Estos contratos pequeños solo tendrían sentido cuando exista un consumidor modular real.

### Aplicación al proyecto

- `TeamRepository` agrega búsquedas de Teams.
- `ActividadRepository` contiene estado, bloqueo de cabecera y solapamiento.
- `InscripcionRepository` contiene búsquedas por actividad, persona y estado.
- Los DTOs de cada recurso evitan un modelo público universal.

Cuando Personal consulte Teams, debería depender de un servicio público mínimo, no de `TeamRepository`. Esa integración aún no existe: el ejemplo muestra la dirección correcta, no una clase faltante que deba crearse automáticamente.

## D - Principio de inversión de dependencias

### Propósito

La política de negocio debe recibir sus colaboradores mediante abstracciones o contratos estables, en lugar de crear infraestructura concreta.

### Diseño incorrecto

> **Ejemplo didáctico — no corresponde necesariamente al código actual.**

```java
class TeamService {
    private final OracleTeamRepository repository;

    TeamService() {
        this.repository = new OracleTeamRepository();
    }
}
```

### Por qué falla

El servicio conoce la implementación Oracle, controla su propia composición y dificulta las pruebas o una sustitución futura.

### Diseño correcto

```java
@Service
class TeamService {
    private final TeamRepository repository;
    private final TeamMapper mapper;

    TeamService(TeamRepository repository, TeamMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
```

Spring compone el objeto y Spring Data aporta la implementación del repositorio. El servicio conserva la política y no instancia infraestructura.

### Aplicación al proyecto

`TeamService`, `ActividadService` e `InscripcionService` ya usan inyección por constructor. Esto permite probar reglas con colaboradores controlados y mantiene la composición fuera de la política.

### ¿Cuándo usar `Service` + `ServiceImpl`?

| Situación | Decisión razonable |
|---|---|
| Servicio interno con una implementación y sin consumidores externos | Mantener una clase concreta inyectada por constructor |
| Contrato público entre módulos | Considerar una interfaz mínima en el paquete expuesto |
| Dos implementaciones reales, por ejemplo local y remota | Usar una interfaz y estrategias explícitas |
| Solo “para cumplir SOLID” o “para poder mockear” | Evitar el par; duplica la API y agrega ceremonia |

Las clases actuales `TeamService`, `ActividadService` e `InscripcionService` son concretas y coherentes con su uso interno. Crear interfaces idénticas y renombrarlas `*ServiceImpl` no resolvería una brecha comprobada. La interfaz aparece cuando existe un contrato que necesita estabilidad o sustitución, no antes.

## Síntesis para la sustentación

1. **S:** las capas separan motivos de cambio.
2. **O:** se extienden variaciones reales sin diseñar abstracciones especulativas.
3. **L:** cada implementación conserva las promesas del contrato que adopta.
4. **I:** los consumidores reciben capacidades pequeñas y cohesionadas.
5. **D:** la política recibe dependencias; Spring resuelve la infraestructura.

La idea central es simple: SOLID mejora límites y comportamiento. Contar interfaces o clases no demuestra calidad arquitectónica.

## Fuentes verificadas

- `docs/project-architecture.md` y `docs/pedro-module-roadmap.md`.
- Implementación actual bajo `src/main/java/pe/edu/upeu/saludablemente/teams` y `src/main/java/pe/edu/upeu/saludablemente/actividades`.
- Pruebas enfocadas bajo `src/test/java/pe/edu/upeu/saludablemente/teams` y `src/test/java/pe/edu/upeu/saludablemente/actividades`.
- Brief y material docente usados como referencias de alcance y enseñanza; no como evidencia automática de defectos.
