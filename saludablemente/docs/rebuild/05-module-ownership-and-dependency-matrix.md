# Matriz de módulos, propiedad y dependencias

**Resultado.** El brief confirma los quince módulos y sus responsables académicos. La asignación de Pedro Eduardo Loayza Huamán queda verificada visualmente: **Actividades** (transaccional), **Teams** y **Metas** (no transaccionales). El orden de entrega siguiente es una **propuesta**, no una aprobación del equipo.

## Lectura de la fuente

| Clasificación | Evidencia |
|---|---|
| Hecho | `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf`, p. 4, tabla de asignación. |
| Hecho | El mismo brief, pp. 4-7, describe las fichas, reglas y relaciones funcionales de cada módulo. |
| Propuesta | Dependencias, contratos públicos y secuencia de construcción de este documento. |
| Pendiente | Contratos Java/REST definitivos, eventos y orden de entrega aprobado. |

## Asignación académica confirmada

| Integrante | Transaccionales | No transaccionales | Estado |
|---|---|---|---|
| Jhon Sebastián Taco Sumi | Evaluación Nutricional, Aptitud Física | Personal | Hecho del brief. |
| **Pedro Eduardo Loayza Huamán** | **Actividades** | **Teams, Metas** | **Hecho del brief, verificado visualmente.** |
| Francisco Roger Apaza Chambi | Asistencias | Noticias, Notificaciones | Hecho del brief. |
| Jéter Josué Sucasaire Panca | Alertas Clínicas, Recomendaciones con IA | Auditoría, Exportación/Interoperabilidad, Perfil/Reporte Personalizado | Hecho del brief; la carga debe validarse con el docente según la nota del PDF. |
| Todo el equipo | Seguridad y Usuarios/Auth (transversal) | - | Hecho del brief. |

## Inventario, colaboración y orden propuesto

“Contrato público” expresa el límite de colaboración que debe implementarse; no es todavía una firma de código aprobada. Ningún módulo accede al repositorio de otro.

| Módulo | Tipo / responsable académico | Depende de | Contrato público candidato | Fase propuesta |
|---|---|---|---|---:|
| Teams | No transaccional / Pedro | - | Consultar team activo y disponibilidad para asignación. | 1 |
| Personal | No transaccional / Jhon | Teams opcional | Consultar persona, estado activo y team; validar identidad de persona. | 2 |
| Seguridad y Usuarios/Auth | Transversal / equipo | Personal, Roles | Identidad autenticada, roles y autorización por operación. | 3 |
| Actividades | Transaccional / Pedro | Seguridad/Usuarios | Consultar estado público de actividad y notificar transición. | 4 |
| Noticias | No transaccional / Francisco | Seguridad/Usuarios | Consultar noticias publicadas y evento de publicación. | 4 |
| Evaluación Nutricional | Transaccional / Jhon | Personal, Seguridad/Usuarios | Registrar evaluación terminada e informar indicadores aprobados. | 5 |
| Aptitud Física | Transaccional / Jhon | Personal, Seguridad/Usuarios | Registrar/consultar aptitud sincronizada con identidad de captura. | 5 |
| Asistencias | Transaccional / Francisco | Actividades, Personal, Seguridad/Usuarios | Consultar actividad vigente y persona; registrar asistencia idempotente. | 6 |
| Metas | No transaccional / Pedro | Personal, Evaluación Nutricional | Consultar/actualizar avance de meta desde indicador autorizado. | 6 |
| Alertas Clínicas | Transaccional / Jéter | Personal, Evaluación Nutricional, Seguridad/Usuarios | Recibir evaluación elegible y publicar alerta atendible. | 7 |
| Recomendaciones con IA | Transaccional / Jéter | Personal, Evaluación Nutricional, Aptitud Física | Solicitar contexto mínimo aprobado y publicar recomendación vigente. | 7 |
| Notificaciones | No transaccional / Francisco | Personal, Seguridad/Usuarios; eventos de módulos | Registrar evento de origen y consultar historial por persona. | 8 |
| Perfil/Reporte Personalizado | No transaccional / Jéter | Personal, Evaluación, Aptitud, Alertas, Recomendaciones, Actividades | Componer vista autorizada y conservar snapshot de reporte. | 8 |
| Auditoría | No transaccional / Jéter | Seguridad/Usuarios; operaciones sensibles | Registrar evento inmutable de auditoría. | 9 |
| Exportación/Interoperabilidad | No transaccional / Jéter | Seguridad/Usuarios; datos autorizados | Solicitar exportación, registrar filtros y entregar archivo. | 9 |

### Límites explícitos

- La dependencia de una fila es un **contrato**, no una autorización para consultar tablas ajenas.
- Los módulos de Perfil, Auditoría, Exportación y Recomendaciones agregan datos de otros módulos; el brief los caracteriza como capas de reglas, agregación o integración, no como permiso de acoplamiento directo.
- `Notificaciones` necesita definir qué módulos emiten eventos y qué garantía de entrega se exige; el brief no lo especifica.
- La secuencia no autoriza provisioning Oracle, DDL, código ni eliminación de legado.

## Orden de construcción antes de provisioning

1. Acordar esta matriz, el contrato de cada dependencia y la herramienta de migraciones.
2. Crear la ficha y la capa de dominio del módulo de fase 1; revisar su modelo lógico y pruebas.
3. Aprobar la primera traducción Oracle/migración correspondiente al módulo ya diseñado.
4. Solo con autorización independiente, provisionar o ejecutar la migración; después continuar capa por capa.

## Pendientes de decisión

- [ ] Aprobar el orden de fases o cambiarlo con una justificación de dependencia.
- [ ] Definir firmas de servicios públicos, endpoints, eventos y políticas de consistencia.
- [ ] Determinar si Roles es parte del módulo transversal o un catálogo técnico inicial.
- [ ] Confirmar si Actividades debe integrarse con Notificaciones de forma síncrona o mediante evento.
- [ ] Acordar el alcance, proveedor y minimización de datos de Recomendaciones con IA.

## Referencias

- `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf`, p. 4: asignación de responsables; pp. 4-7: fichas y reglas de módulos; p. 8: relaciones lógicas.
- `docs/rebuild/02-module-delivery-playbook.md`: secuencia de capas y definición de terminado.
- `docs/rebuild/06-logical-data-model-and-oracle-translation.md`: relaciones, restricciones y traducción Oracle propuesta.