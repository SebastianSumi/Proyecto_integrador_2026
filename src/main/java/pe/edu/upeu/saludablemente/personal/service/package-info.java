/**
 * PersonaService es la unica forma en que otros modulos (aptitudfisica,
 * nutricional) pueden leer o modificar personas - nunca accediendo a
 * PersonaRepository ni a la entidad Persona directamente.
 */
@org.springframework.modulith.NamedInterface("persona-service")
package pe.edu.upeu.saludablemente.personal.service;