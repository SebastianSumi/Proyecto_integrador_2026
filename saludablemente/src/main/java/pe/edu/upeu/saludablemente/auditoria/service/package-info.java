/**
 * Interfaz publica del modulo de Auditoria.
 * Otros modulos pueden inyectar AuditoriaService y sus dependencias
 * para registrar cambios, lecturas, inferencias IA y descargas de reportes.
 *
 * Ningun modulo externo debe acceder a:
 * - auditoria.entity
 * - auditoria.repository
 * - auditoria.mapper
 * - auditoria.detector
 * - auditoria.storage
 */
@org.springframework.modulith.NamedInterface("auditoria-service")
package pe.edu.upeu.saludablemente.auditoria.service;
