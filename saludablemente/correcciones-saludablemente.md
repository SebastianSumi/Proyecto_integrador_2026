# Correcciones requeridas en `saludablemente` respecto al proyecto modelo `bomerp`

## Contexto para quien ejecute estas correcciones

`saludablemente` es un backend Spring Boot 4.0.7 + Spring Modulith
(`pe.edu.upeu.saludablemente`) con tres módulos: `personal`, `aptitudfisica`,
`nutricional`. Debe seguir exactamente las mismas convenciones que el
proyecto modelo `bomerp` (`pe.edu.upeu.bomerp`, módulos `catalogo` y
`ventas`), que es la referencia arquitectónica correcta y ya verificada
(13 tests en verde, incluido `ModularityTests`).

Reglas del modelo `bomerp` que `saludablemente` debe cumplir:

1. **Regla de dependencia entre módulos (la más importante):** un módulo
   solo puede invocar el `Service` público de otro módulo — nunca su
   `Repository` ni su `Entity`. Se declara con `@NamedInterface` en un
   `package-info.java` sobre el paquete `service` (y opcionalmente sobre
   `dto`), y se verifica automáticamente con un test JUnit que llama a
   `ApplicationModules.of(Application.class).verify()`.
2. Entidades JPA con `@Getter @Setter @NoArgsConstructor` de Lombok —
   nunca `@Data` ni `@Builder` sobre una entidad `@Entity`.
3. Todo campo de PK se llama `id` (no `idPersona`, no `idEvaluacion`),
   para que las derived queries de Spring Data (`findByCategoriaId`,
   etc.) funcionen sin fricción al navegar relaciones.
4. `List<T>` siempre con genéricos explícitos, nunca raw types.
5. Fetch `LAZY` explícito en toda relación, nunca depender del valor por
   defecto de JPA (que es `EAGER` para `@OneToOne`/`@ManyToOne`).
6. `@EntityGraph` en las consultas de listado que sí necesitan traer una
   relación, para evitar N+1.
7. Vocabularios controlados (estados, tipos, categorías fijas) como
   `enum` Java con `@Enumerated(EnumType.STRING)`, nunca `String` libre.
8. Referencias entre módulos a nivel de entidad se hacen con un campo
   `Long <nombre>Id` plano, **nunca** con `@ManyToOne` hacia la entidad
   de otro módulo (esto en `saludablemente` ya está bien hecho, ver
   sección "Cosas que NO hay que tocar").
9. Cada recurso tiene tests: al menos un `@WebMvcTest` de su controller
   con `@MockitoBean` del service, más el test de modularidad a nivel de
   aplicación.

**Instrucción explícita: ignorar `oracle/S01_02_tablas.sql` por completo.**
No comparar entidades contra ese archivo ni "corregirlas" para que
coincidan con él — ese archivo no forma parte del alcance de esta
corrección y será reemplazado aparte.

A continuación, cada diferencia encontrada, con archivo, código actual,
el problema exacto, y la corrección exacta a aplicar.

---

## 1. Violación de la regla de dependencia entre módulos (crítico)

### 1.1 `aptitudfisica/service/EvaluacionAptitudServiceImpl.java`

**Código actual:**
```java
import pe.edu.upeu.saludablemente.personal.repository.PersonaRepository;
...
private final PersonaRepository personaRepository;
...
public List<EvaluacionAptitudResponseDto> listarPorPersona(Long idPersona) {
    personaRepository.findById(idPersona)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + idPersona));
    ...
}

public EvaluacionAptitudResponseDto registrarEvaluacion(EvaluacionAptitudRequestDto request) {
    personaRepository.findById(request.getIdPersona())
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + request.getIdPersona()));
    ...
}
```

**Problema:** el módulo `aptitudfisica` inyecta y usa directamente el
`Repository` del módulo `personal`. Esto viola la regla de dependencia
entre módulos: ningún módulo debe conocer el `Repository` ni la
`Entity` de otro módulo, solo su `Service` público.

**Corrección exacta:**
1. Reemplazar el campo:
   ```java
   private final PersonaRepository personaRepository;
   ```
   por:
   ```java
   private final pe.edu.upeu.saludablemente.personal.service.PersonaService personaService;
   ```
2. Reemplazar cada `personaRepository.findById(x).orElseThrow(...)` por
   una llamada a `personaService.obtener(x)`. Nótese que
   `PersonaService.obtener(Long)` ya lanza `ResourceNotFoundException`
   internamente si no existe (ver
   `personal/service/PersonaServiceImpl.buscarOFallar`), así que el
   `orElseThrow` ya no hace falta — basta con:
   ```java
   personaService.obtener(idPersona); // ya lanza ResourceNotFoundException si no existe
   ```
3. Quitar el import de `PersonaRepository` y de la entidad `Persona` si
   quedó alguno sin usar.

### 1.2 `nutricional/service/EvaluacionNutricionalServiceImpl.java`

**Código actual:**
```java
import pe.edu.upeu.saludablemente.personal.entity.Persona;
import pe.edu.upeu.saludablemente.personal.repository.PersonaRepository;
...
private final PersonaRepository personaRepository;
...
public List<EvaluacionNutricionalResponseDto> listarPorPersona(Long idPersona) {
    personaRepository.findById(idPersona)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + idPersona));
    ...
}

public EvaluacionNutricionalResponseDto registrarAntropometria(EvaluacionNutricionalRequestDto request) {
    Persona persona = personaRepository.findById(request.getIdPersona())
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + request.getIdPersona()));
    ...
    calcularDiagnosticoAntropometrico(detalle, persona);
    ...
}

private void calcularDiagnosticoAntropometrico(DetalleAntropometrico detalle, Persona persona) {
    ...
    diagnosticoPerimetroAbdominal(detalle.getPerimetroAbdominalCm(), persona.getSexo());
    ...
    diagnosticoGrasa(detalle.getPorcentajeGrasa(), persona.getSexo());
    ...
    diagnosticoMasaMuscular(detalle.getPorcentajeMasaMuscular(), persona.getSexo());
}
```

**Problema:** es el mismo caso que 1.1, pero además la entidad `Persona`
(no un DTO) se pasa como parámetro y se usa `persona.getSexo()`
directamente dentro de la lógica de otro módulo. Esto es una fuga de
encapsulación aún más directa que la del módulo `aptitudfisica`.

**Corrección exacta:**
1. Reemplazar el campo `PersonaRepository personaRepository` por
   `PersonaService personaService` (mismo import que en 1.1).
2. En `registrarAntropometria`, cambiar:
   ```java
   Persona persona = personaRepository.findById(request.getIdPersona())
           .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada: " + request.getIdPersona()));
   ```
   por:
   ```java
   PersonaResponseDto persona = personaService.obtener(request.getIdPersona());
   ```
   (import `pe.edu.upeu.saludablemente.personal.dto.PersonaResponseDto`).
3. Cambiar la firma de `calcularDiagnosticoAntropometrico` para que
   reciba `PersonaResponseDto persona` en vez de `Persona persona`.
   `PersonaResponseDto` ya expone `getSexo()`, así que el resto del
   cuerpo del método no cambia.
4. En `listarPorPersona`, aplicar el mismo cambio que en 1.1
   (`personaService.obtener(idPersona)` en vez de
   `personaRepository.findById(...).orElseThrow(...)`).
5. Quitar los imports de `Persona` y `PersonaRepository`.

### 1.3 Declarar la API pública del módulo `personal` con `@NamedInterface`

**Falta por completo.** En `bomerp`, el paquete `service` y el paquete
`dto` del módulo `producto` tienen cada uno un `package-info.java` como
este (ejemplo real de `catalogo/producto/service/package-info.java`):
```java
/**
 * ProductoService es la unica forma en que otros modulos (ventas, desde S4)
 * pueden leer o modificar productos - nunca accediendo a ProductoRepository
 * ni a la entidad Producto directamente (ADR-002).
 */
@org.springframework.modulith.NamedInterface("producto-service")
package pe.edu.upeu.bomerp.catalogo.producto.service;
```
y sobre `dto`:
```java
/**
 * ProductoResponse es el unico tipo de catalogo.producto que otros modulos
 * pueden recibir de vuelta - nunca la entidad Producto (ADR-002).
 */
@org.springframework.modulith.NamedInterface("producto-dto")
package pe.edu.upeu.bomerp.catalogo.producto.dto;
```

**Corrección exacta:** crear estos dos archivos nuevos, adaptados al
proyecto `saludablemente`:

`src/main/java/pe/edu/upeu/saludablemente/personal/service/package-info.java`:
```java
/**
 * PersonaService es la unica forma en que otros modulos (aptitudfisica,
 * nutricional) pueden leer o modificar personas - nunca accediendo a
 * PersonaRepository ni a la entidad Persona directamente.
 */
@org.springframework.modulith.NamedInterface("persona-service")
package pe.edu.upeu.saludablemente.personal.service;
```

`src/main/java/pe/edu/upeu/saludablemente/personal/dto/package-info.java`:
```java
/**
 * PersonaResponseDto es el unico tipo de personal que otros modulos
 * pueden recibir de vuelta - nunca la entidad Persona.
 */
@org.springframework.modulith.NamedInterface("persona-dto")
package pe.edu.upeu.saludablemente.personal.dto;
```

### 1.4 Crear el test de modularidad

**Falta por completo.** No existe ningún archivo bajo `src/test/java`.
En `bomerp` existe
`src/test/java/pe/edu/upeu/bomerp/ModularityTests.java`:
```java
package pe.edu.upeu.bomerp;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(BomerpBackendApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        new org.springframework.modulith.docs.Documenter(modules)
                .writeDocumentation();
    }
}
```

**Corrección exacta:** crear
`src/test/java/pe/edu/upeu/saludablemente/ModularityTests.java` con el
mismo contenido, cambiando `BomerpBackendApplication.class` por
`SaludablementeApplication.class` y el paquete raíz. Este test debe
compilarse y pasar **después** de aplicar las correcciones 1.1 y 1.2 —
si se ejecuta antes, fallará señalando exactamente esas dos
violaciones, lo cual sirve como confirmación de que el problema
existía.

---

## 2. Bugs de derived queries de Spring Data (crítico — falla al arrancar el contexto)

Estos tres métodos lanzarán `PropertyReferenceException` al arrancar la
aplicación (Spring Data valida las derived queries en el arranque,
construyendo el repositorio):

### 2.1 `aptitudfisica/repository/EvaluacionAptitudRepository.java`
```java
List<EvaluacionAptitud> findByPersonaId(Long idPersona);
```
**Problema:** la entidad `EvaluacionAptitud` tiene un campo llamado
`idPersona` (un `Long` plano, sin relación `@ManyToOne`). Spring Data
no puede resolver `findByPersonaId` contra un campo llamado `idPersona`
— el orden de las palabras no coincide y no existe ninguna propiedad
`persona` ni `personaId` en la entidad.

**Corrección exacta:** renombrar el método a
```java
List<EvaluacionAptitud> findByIdPersona(Long idPersona);
```
Esto sí coincide exactamente con el nombre del campo `idPersona`.
Actualizar también la llamada en
`EvaluacionAptitudServiceImpl.listarPorPersona`:
```java
evaluacionAptitudRepository.findByIdPersona(idPersona)
```

### 2.2 `nutricional/repository/EvaluacionNutricionalRepository.java`
```java
List<EvaluacionNutricional> findByPersonaId(Long idPersona);
```
**Problema y corrección:** exactamente igual que 2.1 —
`EvaluacionNutricional` también tiene el campo `idPersona` plano.
Renombrar a `findByIdPersona(Long idPersona)` y actualizar la llamada en
`EvaluacionNutricionalServiceImpl.listarPorPersona`.

### 2.3 `personal/repository/PreferenciaComunicacionRepository.java`
```java
Optional<PreferenciaComunicacion> findByPersonaId(Long idPersona);
```
**Problema:** aquí `PreferenciaComunicacion` sí tiene una relación
`persona` (`@OneToOne`), así que Spring Data intenta navegar
`persona.id` — pero la propiedad de la PK en `Persona` se llama
`idPersona`, no `id` (ver punto 3 de este documento, que unifica el
nombre de la PK a `id` en todas las entidades). Una vez aplicado el
punto 3, este método **si funcionará tal como está escrito**
(`findByPersonaId` navegando `persona.id`), así que la corrección aquí
depende de que el punto 3 se aplique primero.

**Corrección exacta:** no renombrar este método. Simplemente verificar,
después de aplicar el punto 3 (renombrar `idPersona` a `id` en la
entidad `Persona`), que `findByPersonaId(Long idPersona)` sigue
compilando y resolviendo correctamente contra `persona.id`. Si por
alguna razón se decide no aplicar el punto 3, entonces este método debe
cambiar a una `@Query` explícita:
```java
@Query("SELECT p FROM PreferenciaComunicacion p WHERE p.persona.idPersona = :idPersona")
Optional<PreferenciaComunicacion> findByPersonaId(@Param("idPersona") Long idPersona);
```
pero la opción preferida es aplicar el punto 3 y dejar la derived query
tal cual.

---

## 3. Nombre de campo de la PK: usar siempre `id`

**Problema general:** todas las entidades de `saludablemente` nombran
su clave primaria con un prefijo (`idPersona`, `idEvaluacion`,
`idPrueba`, `idCredencial`, `idPreferencia`, `idDetalleAptitud`,
`idAntropometrico`, `idBioquimico`, `idEvaluacionAptitud`). En `bomerp`,
toda entidad nombra su PK simplemente `id`:
```java
// bomerp/catalogo/categoria/entity/Categoria.java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "ID")
private Long id;
```
Esta diferencia no es solo estética: rompe la resolución automática de
derived queries que navegan relaciones (ver sección 2) cada vez que el
nombre de la PK no es `id`.

**Corrección exacta:** en cada una de las siguientes entidades, renombrar
el campo de la PK a `id` (manteniendo el `@Column(name = "...")` con el
nombre de columna real si aplica — el mapeo de columna no cambia, solo
el nombre del campo Java y su getter/setter):

| Archivo | Campo actual | Campo nuevo |
|---|---|---|
| `personal/entity/Persona.java` | `idPersona` | `id` |
| `personal/entity/CredencialPrograma.java` | `idCredencial` | `id` |
| `personal/entity/PreferenciaComunicacion.java` | `idPreferencia` | `id` |
| `aptitudfisica/entity/EvaluacionAptitud.java` | `idEvaluacionAptitud` | `id` |
| `aptitudfisica/entity/DetallePruebaFisica.java` | `idDetalleAptitud` | `id` |
| `aptitudfisica/entity/CatalogoPrueba.java` | `idPrueba` | `id` |
| `nutricional/entity/EvaluacionNutricional.java` | `idEvaluacion` | `id` |
| `nutricional/entity/DetalleAntropometrico.java` | `idAntropometrico` | `id` |
| `nutricional/entity/DetalleBioquimico.java` | `idBioquimico` | `id` |

**Importante:** este cambio se propaga a:
- Los getters/setters generados por Lombok (`getIdPersona()` →
  `getId()`, etc.) — cualquier código que los use debe actualizarse
  (services, mappers, controllers, dtos que reflejen ese nombre).
- Los DTOs de respuesta (`PersonaResponseDto.idPersona`,
  `EvaluacionAptitudResponseDto.idEvaluacionAptitud`, etc.) — aquí sí es
  aceptable mantener el nombre "expresivo" en el DTO (`idPersona`,
  `idEvaluacionAptitud`) porque el DTO es el contrato público de la API
  y bomerp también usa nombres expresivos en sus DTOs cuando conviene;
  lo que debe ser `id` es el campo de la **entidad JPA**, no
  necesariamente el del DTO. Si se decide unificar también el DTO,
  ajustar el mapper (`@Mapping(target = "id", source = "idPersona")` o
  similar) para que MapStruct siga resolviendo el mapeo automáticamente.
- Las referencias cruzadas entre módulos que ya usan `Long idPersona`
  como campo plano (`EvaluacionAptitud.idPersona`,
  `EvaluacionNutricional.idPersona`) **no se tocan** — esas NO son la
  PK de esas entidades, son una referencia foránea a otro módulo
  expresada como valor plano, y ese patrón ya es correcto (ver sección
  "Cosas que NO hay que tocar").

---

## 4. `@Data` de Lombok sobre entidades JPA (serio)

**Problema:** las 9 entidades de `saludablemente` usan:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
```
`@Data` genera `equals()`, `hashCode()` y `toString()` sobre **todos**
los campos, incluidas las relaciones bidireccionales gestionadas por
Hibernate. Esto genera dos riesgos reales:
1. **`StackOverflowError`** en `toString()` o `equals()` cuando hay un
   ciclo bidireccional, por ejemplo `Persona.preferenciaComunicacion`
   (que apunta de vuelta a `Persona`) o
   `Persona.credenciales` → `CredencialPrograma.persona` (que apunta de
   vuelta a `Persona`). Cualquier log, depuración o serialización
   accidental de una de estas entidades puede colgar el hilo.
2. **`equals`/`hashCode` inconsistentes** para entidades gestionadas por
   JPA: el estándar recomendado (y el que sigue implícitamente `bomerp`
   al no generar estos métodos en absoluto sobre sus entidades) es no
   basar la igualdad en todos los campos mutables de una entidad.

`bomerp` usa siempre, en las 3 entidades de sus 2 módulos:
```java
@Getter
@Setter
@NoArgsConstructor
```
Nunca `@Data`, nunca `@Builder`, nunca `@AllArgsConstructor` sobre una
clase anotada con `@Entity`.

**Corrección exacta:** en las 9 entidades listadas en la tabla de la
sección 3, reemplazar:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
```
por:
```java
@Getter
@Setter
@NoArgsConstructor
@Entity
```
Esto implica:
- Quitar los imports de `lombok.Data`, `lombok.AllArgsConstructor` y
  `lombok.Builder` en cada entidad, y agregar `lombok.Getter` y
  `lombok.Setter`.
- **Todo el código que construye estas entidades con `.builder()...build()`
  debe reescribirse a construcción imperativa** (`new Entidad()` +
  setters). Los puntos exactos donde esto ocurre son:
  - `PersonaServiceImpl.createPersona`: construcción de
    `PreferenciaComunicacion.builder()...build()` y
    `CredencialPrograma.builder()...build()`.
  - `AptitudFisicaMapper.toDetalle` (método `default`): construcción de
    `CatalogoPrueba.builder().idPrueba(dto.getIdPrueba()).build()`.
  Reescribir cada uno de estos bloques como, por ejemplo:
  ```java
  PreferenciaComunicacion preferencia = new PreferenciaComunicacion();
  preferencia.setCanalPreferido("WhatsApp");
  preferencia.setHorarioContactoInicio(LocalTime.of(8, 0));
  preferencia.setHorarioContactoFin(LocalTime.of(18, 0));
  preferencia.setAceptaRecordatorios(true);
  preferencia.setPersona(persona);
  ```
  y de forma análoga para `CredencialPrograma` y para el `CatalogoPrueba`
  "stub" dentro del mapper.
- Los DTOs (`PersonaResponseDto`, `CredencialProgramaDto`, etc.) **sí
  pueden conservar** `@Builder`/`@AllArgsConstructor` — esa es la
  convención correcta de `bomerp` para DTOs de respuesta
  (`CategoriaResponse`, `ProductoResponse`, `VentaResponse` los usan).
  Este punto solo aplica a las clases `@Entity`.

---

## 5. Raw types (`List` sin genéricos)

**Archivos y campos afectados:**
- `personal/entity/Persona.java`:
  ```java
  @Builder.Default
  @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
  private List credenciales = new ArrayList<>();
  ```
- `aptitudfisica/entity/EvaluacionAptitud.java`:
  ```java
  @Builder.Default
  @OneToMany(mappedBy = "evaluacionAptitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List detalles = new ArrayList<>();
  ```
- `aptitudfisica/entity/CatalogoPrueba.java`:
  ```java
  @Builder.Default
  @OneToMany(mappedBy = "catalogoPrueba", fetch = FetchType.LAZY)
  private List detalles = new ArrayList<>();
  ```

**Problema:** los tres campos son `List` crudo (raw type), sin
parámetro de tipo. Esto desactiva el chequeo de tipos del compilador
para esa colección — cualquier `add()` sobre ella acepta cualquier
`Object` sin error de compilación, y cualquier iteración requiere casts
implícitos inseguros. `bomerp` siempre declara colecciones con genérico
explícito, por ejemplo `Venta.detalles`:
```java
@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DetalleVenta> detalles = new ArrayList<>();
```

**Corrección exacta:**
- `Persona.credenciales` → `private List<CredencialPrograma> credenciales = new ArrayList<>();`
- `EvaluacionAptitud.detalles` → `private List<DetallePruebaFisica> detalles = new ArrayList<>();`
- `CatalogoPrueba.detalles` → `private List<DetallePruebaFisica> detalles = new ArrayList<>();`

(Nota: esta corrección es independiente del punto 4 sobre `@Builder` —
aplica igual si se conserva o se quita `@Builder.Default` en el camino
de la corrección 4; en cualquier caso el `@Builder.Default` debe
desaparecer junto con `@Builder` según la sección 4).

---

## 6. Fetch por defecto no controlado (`EAGER` implícito)

**Archivo:** `personal/entity/Persona.java`
```java
@OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
private PreferenciaComunicacion preferenciaComunicacion;
```

**Problema:** un `@OneToOne` sin `fetch` explícito es **`EAGER` por
defecto** según la especificación JPA. Esto significa que cada vez que
se carga una `Persona` (por ejemplo en `PersonaRepository.findAll()`),
Hibernate carga también su `PreferenciaComunicacion` — sin que quede
declarado en el código, y sin optimizar con un join explícito. Es
justo el comportamiento que el modelo evita deliberadamente. De hecho,
en el propio `saludablemente`, las relaciones equivalentes del módulo
`nutricional` (`DetalleAntropometrico.evaluacionNutricional`,
`DetalleBioquimico.evaluacionNutricional`,
`EvaluacionNutricional.detalleAntropometrico`,
`EvaluacionNutricional.detalleBioquimico`) sí declaran
`fetch = FetchType.LAZY` explícito — la inconsistencia es interna al
propio proyecto, no solo respecto al modelo.

**Corrección exacta:**
```java
@OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private PreferenciaComunicacion preferenciaComunicacion;
```

---

## 7. Falta de `@EntityGraph` para evitar N+1

**Archivo:** `personal/repository/PersonaRepository.java`
```java
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByCelularAndActivoTrue(String celular);
    List<Persona> findByActivoTrue();
}
```

**Problema:** `PersonaMapper.toResponse` incluye
`preferenciaComunicacion` en la respuesta
(`PersonaResponseDto.preferenciaComunicacion`). Sin un `@EntityGraph`
que la traiga en la misma consulta, listar personas
(`PersonaServiceImpl.listar`) genera una consulta adicional por cada
persona para resolver esa relación (N+1), incluso si se corrige el
punto 6 a `LAZY` (LAZY sin `@EntityGraph` sigue disparando una consulta
extra por fila cuando el mapper accede al getter). `bomerp` resuelve
esto explícitamente en `ProductoRepository`:
```java
@Override
@EntityGraph(attributePaths = "categoria")
List<Producto> findAll();
```

**Corrección exacta:** en `PersonaRepository`, sobrescribir `findAll()`
y `findByActivoTrue()` con `@EntityGraph`:
```java
@Override
@EntityGraph(attributePaths = "preferenciaComunicacion")
List<Persona> findAll();

@EntityGraph(attributePaths = "preferenciaComunicacion")
List<Persona> findByActivoTrue();
```

---

## 8. Vocabularios controlados como `String` libre en vez de `enum`

**Campos afectados y su archivo:**
- `personal/entity/Persona.java` → `private String sexo;`
- `personal/entity/CredencialPrograma.java` → `private String tipoCredencial;` y `private String estado;`
- `personal/entity/PreferenciaComunicacion.java` → `private String canalPreferido;`
- `nutricional/entity/EvaluacionNutricional.java` → `private String estadoEvaluacion;`

**Problema:** `bomerp` modela cualquier vocabulario cerrado como `enum`
con `@Enumerated(EnumType.STRING)`, por ejemplo
`ventas/venta/entity/EstadoVenta.java`:
```java
public enum EstadoVenta {
    REGISTRADA
}
```
usado en `Venta.java` como:
```java
@Enumerated(EnumType.STRING)
@Column(name = "ESTADO", nullable = false, length = 20)
private EstadoVenta estado;
```
En `saludablemente`, ninguno de estos campos tiene una restricción de
valores permitidos a nivel de tipo. Solo `sexo` tiene un `@Pattern` en
el DTO de request (`PersonaRequestDto`) — pero eso valida la entrada,
no impide que la entidad reciba cualquier valor por otras vías (por
ejemplo, `PersonaServiceImpl.desactivarYAnonimizar` hace
`credencial.setEstado("ANONIMIZADA")` con un string literal que ni
siquiera está definido como constante en ningún lado más que ese punto
de uso).

**Corrección exacta:** crear los siguientes enums y aplicarlos:

`personal/entity/Sexo.java` (nuevo archivo):
```java
package pe.edu.upeu.saludablemente.personal.entity;

public enum Sexo {
    M, F
}
```
En `Persona.java`:
```java
@Enumerated(EnumType.STRING)
@Column(name = "sexo", nullable = false, length = 10)
private Sexo sexo;
```

`personal/entity/EstadoCredencial.java` (nuevo archivo):
```java
package pe.edu.upeu.saludablemente.personal.entity;

public enum EstadoCredencial {
    ACTIVA, ANONIMIZADA
}
```
En `CredencialPrograma.java`:
```java
@Enumerated(EnumType.STRING)
@Column(name = "estado", nullable = false, length = 20)
private EstadoCredencial estado;
```
Y en `PersonaServiceImpl`, reemplazar los literales
`"ACTIVA"`/`ESTADO_CREDENCIAL_ACTIVA` y `"ANONIMIZADA"` por
`EstadoCredencial.ACTIVA` / `EstadoCredencial.ANONIMIZADA`. La constante
`ESTADO_CREDENCIAL_ACTIVA` puede eliminarse.

`personal/entity/TipoCredencial.java` (nuevo archivo, valor mínimo
según el código actual — ajustar si existen más tipos en el dominio
real):
```java
package pe.edu.upeu.saludablemente.personal.entity;

public enum TipoCredencial {
    GENERAL
}
```
En `CredencialPrograma.java`:
```java
@Enumerated(EnumType.STRING)
@Column(name = "tipo_credencial", nullable = false, length = 50)
private TipoCredencial tipoCredencial;
```
Y en `PersonaServiceImpl.createPersona`, reemplazar
`.tipoCredencial("GENERAL")` por `.tipoCredencial(TipoCredencial.GENERAL)`
(o el setter equivalente tras aplicar la corrección 4).

`nutricional/entity/EstadoEvaluacionNutricional.java` (nuevo archivo):
```java
package pe.edu.upeu.saludablemente.nutricional.entity;

public enum EstadoEvaluacionNutricional {
    EN_PROCESO, COMPLETA
}
```
En `EvaluacionNutricional.java`:
```java
@Enumerated(EnumType.STRING)
@Column(name = "estado_evaluacion", nullable = false, length = 30)
private EstadoEvaluacionNutricional estadoEvaluacion;
```
Y en `EvaluacionNutricionalServiceImpl`, reemplazar las constantes
`ESTADO_EN_PROCESO`/`ESTADO_COMPLETA` (`String`) por el enum
correspondiente en cada asignación.

`canalPreferido` en `PreferenciaComunicacion` puede dejarse como
`String` si el dominio real admite canales no predefinidos (texto
libre tipo "Telegram", "correo personal", etc.) — evaluar con el
equipo si es realmente un vocabulario cerrado. Si lo es (WhatsApp,
SMS, Email, Llamada, por ejemplo), aplicar el mismo patrón:
```java
package pe.edu.upeu.saludablemente.personal.entity;

public enum CanalComunicacion {
    WHATSAPP, SMS, EMAIL, LLAMADA
}
```

Todos los DTOs correspondientes (`PersonaRequestDto.sexo`,
`CredencialProgramaDto.estado`, `CredencialProgramaDto.tipoCredencial`,
etc.) deben actualizarse para usar el tipo `enum` en vez de `String`
donde el mapper lo requiera, o mantenerse como `String` en el DTO y
dejar que MapStruct convierta automáticamente entre `String` y `enum`
(MapStruct soporta esta conversión sin configuración adicional cuando
los nombres del enum coinciden exactamente con los valores string
esperados).

---

## 9. `columnDefinition = "TEXT"` no es válido en Oracle

**Archivos:**
- `aptitudfisica/entity/CatalogoPrueba.java`:
  ```java
  @Column(name = "descripcion", columnDefinition = "TEXT")
  private String descripcion;
  ```
- `nutricional/entity/EvaluacionNutricional.java`:
  ```java
  @Column(name = "observaciones", columnDefinition = "TEXT")
  private String observaciones;
  ```

**Problema:** `TEXT` es un tipo de columna de MySQL/PostgreSQL. Oracle
no lo reconoce; el tipo equivalente en Oracle es `CLOB` (para texto
largo) o simplemente `VARCHAR2(n)` si el contenido cabe en 4000
caracteres. Con el dialecto Oracle de Hibernate, dejar `TEXT` tal cual
generará SQL DDL inválido si se usa `ddl-auto: create`/`update`, o un
error de validación de esquema si se usa `validate` contra una columna
real creada con otro tipo.

**Corrección exacta:** cambiar a `CLOB` (recomendado para descripciones
u observaciones de longitud variable/larga) o eliminar
`columnDefinition` y usar `length` si el campo tiene un máximo
razonable:
```java
@Lob
@Column(name = "descripcion")
private String descripcion;
```
(usar `@Lob` de `jakarta.persistence.Lob`, que Hibernate traduce a
`CLOB` en el dialecto Oracle). Aplicar el mismo cambio en
`observaciones` de `EvaluacionNutricional`.

---

## 10. `CatalogoPrueba` sin `Service` ni `Controller`

**Problema:** existe `CatalogoPruebaDto` y `CatalogoPruebaRepository`,
pero no hay `CatalogoPruebaService`/`CatalogoPruebaServiceImpl` ni
`CatalogoPruebaController`. No hay forma de administrar el catálogo de
pruebas físicas vía API — hoy solo se puede leer indirectamente a
través de `EvaluacionAptitudServiceImpl` (que sí usa
`CatalogoPruebaRepository` internamente, lo cual es correcto porque
está dentro del mismo módulo `aptitudfisica`).

En `bomerp`, la entidad de referencia equivalente (`Categoria`) sí
expone CRUD REST completo
(`catalogo/categoria/{controller,service}`).

**Corrección exacta:** crear, siguiendo exactamente el patrón de
`bomerp/catalogo/categoria`:

`aptitudfisica/service/CatalogoPruebaService.java`:
```java
package pe.edu.upeu.saludablemente.aptitudfisica.service;

import pe.edu.upeu.saludablemente.aptitudfisica.dto.CatalogoPruebaDto;
import java.util.List;

public interface CatalogoPruebaService {
    List<CatalogoPruebaDto> listar(boolean soloActivos);
    CatalogoPruebaDto obtener(Long id);
    CatalogoPruebaDto crear(CatalogoPruebaDto request);
    CatalogoPruebaDto actualizar(Long id, CatalogoPruebaDto request);
    void eliminar(Long id);
}
```
`aptitudfisica/service/CatalogoPruebaServiceImpl.java`: implementación
análoga a `CategoriaServiceImpl` de `bomerp` (mismo patrón
`buscarOFallar` + `@Transactional`), usando `CatalogoPruebaRepository`
y `AptitudFisicaMapper.toCatalogoPruebaDto` (agregar también un
`toEntity` en el mapper si no existe, con
`@Mapping(target = "id", ignore = true)` sobre el id, y
`@Mapping(target = "detalles", ignore = true)`).

`aptitudfisica/controller/CatalogoPruebaController.java`: CRUD REST
análogo a `CategoriaController` de `bomerp`, montado en
`/api/v1/catalogo-pruebas`.

---

## 11. Ausencia total de tests

**Problema:** no existe `src/test/java` en el proyecto. Faltan, como
mínimo, los equivalentes a los que sí existen en `bomerp`:
- `ModularityTests` (ver sección 1.4).
- `@WebMvcTest` por cada controller, con `@MockitoBean` del service
  correspondiente y casos de: creación válida (201/200), validación
  fallida (400), recurso no encontrado (404). Ver
  `bomerp/src/test/java/.../CategoriaControllerTest.java` y
  `ProductoControllerTest.java` como plantilla exacta a replicar para
  `PersonaControllerTest`, `EvaluacionAptitudControllerTest` y
  `EvaluacionNutricionalControllerTest`.
- Un `SaludablementeApplicationTests` mínimo con `@SpringBootTest` y
  `contextLoads()`, análogo a `BomerpBackendApplicationTests`.

`src/test/resources/application-test.yml` ya existe (configurado con H2
en modo Oracle), pero no lo usa ningún test — queda huérfano hasta que
se agreguen tests de integración que lo requieran.

**Corrección exacta:** crear los archivos de test siguiendo
exactamente la estructura y estilo de los tests existentes en
`bomerp` (mismos imports, mismas anotaciones — `@WebMvcTest`,
`@MockitoBean`, `MockMvc`, `ObjectMapper` de `tools.jackson.databind`).

---

## 12. Filtro de correlación (`CorrelationIdFilter`) ausente pero referenciado en logs

**Problema:** `logback-spring.xml` de `saludablemente` incluye:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] %-5level %logger{36} - %msg%n
```
Este patrón espera un valor `traceId` en el MDC (Mapped Diagnostic
Context) de SLF4J. En `bomerp`, ese valor lo pone
`filter/CorrelationIdFilter.java`:
```java
package pe.edu.upeu.bomerp.filter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String TRACE_ID_HEADER = "X-Trace-ID";
    public static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        try {
            MDC.put(MDC_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
```
`saludablemente` no tiene ningún archivo bajo un paquete `filter`, así
que `%X{traceId}` siempre imprimirá vacío (`[]`) en cada línea de log —
el patrón de log se copió del proyecto modelo, pero el componente que
lo alimenta nunca se creó.

**Corrección exacta:** crear
`src/main/java/pe/edu/upeu/saludablemente/filter/CorrelationIdFilter.java`
con el mismo contenido de `bomerp`, cambiando el paquete a
`pe.edu.upeu.saludablemente.filter`.

---

## 13. Paquete `exception` sin declarar como módulo abierto de Modulith

**Problema:** en `bomerp`,
`exception/package-info.java` declara:
```java
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package pe.edu.upeu.bomerp.exception;
```
Esto marca el paquete `exception` (y, de forma análoga, se podría hacer
con `filter`) como accesible desde cualquier módulo sin que
`ModularityTests.verify()` lo reporte como una violación de límites.
`saludablemente` no tiene ningún `package-info.java` en su paquete
`exception`. Sin este archivo, en cuanto se agregue el
`ModularityTests` de la sección 1.4, Spring Modulith podría marcar como
sospechosas las referencias cruzadas a `ResourceNotFoundException` y
`BusinessRuleException` desde los tres módulos de negocio.

**Corrección exacta:** crear
`src/main/java/pe/edu/upeu/saludablemente/exception/package-info.java`:
```java
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package pe.edu.upeu.saludablemente.exception;
```
Aplicar el mismo tratamiento al paquete `filter` una vez creado (punto
12), si `ModularityTests.verify()` lo exige tras ejecutarlo.

---

## 14. Exposición de Actuator incompleta respecto a la dependencia declarada

**Archivo:** `src/main/resources/application-dev.yml`
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```
**Problema:** el `pom.xml` incluye
`io.micrometer:micrometer-registry-prometheus` (igual que `bomerp`),
pero a diferencia de `bomerp` (que expone
`health,info,metrics,prometheus`), aquí no se expone el endpoint
`prometheus`. La dependencia está presente pero inutilizable vía HTTP.

**Corrección exacta:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

---

## 15. Higiene del entregable (no es código, pero afecta la corrección)

Si quien reciba este documento también recibe el proyecto como archivo
comprimido, verificar que no incluya:
- `target/` (clases compiladas, se regeneran con `mvnw clean package`).
- `.idea/` (configuración de IDE, ya listada en `.gitignore` del propio
  proyecto pero incluida de todas formas en el zip revisado).
- `logs/` (archivos de log de ejecuciones previas).

Estas carpetas ya están correctamente listadas en el `.gitignore` del
proyecto — el problema no es la configuración de Git, sino que el
entregable se generó comprimiendo la carpeta de trabajo local en vez de
exportar desde un checkout limpio del repositorio.

---

## Cosas que NO hay que tocar (ya están bien hechas)

Para evitar que se "corrija" algo que en realidad ya sigue el modelo
correctamente:

- **La referencia a `Persona` desde `EvaluacionAptitud.idPersona` y
  `EvaluacionNutricional.idPersona` como `Long` plano (sin
  `@ManyToOne`) es el patrón correcto**, igual que
  `DetalleVenta.productoId` en `bomerp`. No convertir estos campos en
  relaciones JPA hacia la entidad `Persona` — eso sería reintroducir el
  acoplamiento entre módulos que la sección 1 elimina. El único cambio
  legítimo cerca de este campo es el de nombre de la derived query
  (sección 2.1 y 2.2), no el tipo de dato ni la relación.
- El refactor de `GlobalExceptionHandler` con el método privado
  `construir(...)` compartido es una mejora legítima sobre `bomerp`
  (que repite el armado del `Map` de respuesta tres veces) — conservar
  tal cual.
- `BusinessRuleException` como excepción genérica de regla de negocio
  (en vez de una excepción específica por regla, como
  `StockInsuficienteException` en `bomerp`) es una decisión razonable
  dado que aquí hay varias reglas de negocio distintas (celular
  duplicado, credencial inactiva, prueba inexistente en catálogo,
  estatura/peso inválidos). Conservar tal cual.
- La estructura de paquetes
  `modulo/recurso/{controller,dto,entity,repository,service,mapper}`,
  la separación interfaz + `Impl` en los services, el uso de
  MapStruct con `@Mapper(componentModel = "spring")`, el uso de
  `@RequiredArgsConstructor` para inyección por constructor, y el hecho
  de que ninguna entidad JPA se devuelva directamente desde un
  controller (siempre a través de un DTO) — todo esto ya sigue el
  modelo correctamente y no requiere cambios.
