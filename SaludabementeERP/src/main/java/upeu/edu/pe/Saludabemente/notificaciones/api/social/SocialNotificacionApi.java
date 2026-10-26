package upeu.edu.pe.Saludabemente.notificaciones.api.social;

/** Puente a RabbitMQ existente. El productor invoca después del commit y excluye autoavisos.
 * Publicación no equivale a entrega confirmada; deduplicación/reintentos se verifican en F14.
 */
public interface SocialNotificacionApi {
    void enviar(SocialAvisoDto aviso);
}
