package pe.edu.upeu.saludablemente.exportacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SUSCRIPTOR_WEBHOOK", schema = "SALUD_EXPORTACION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscriptorWebhookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_SUSCRIPTOR", columnDefinition = "RAW(16)")
    private UUID idSuscriptor;

    @Column(name = "NOMBRE_SISTEMA", nullable = false, length = 100)
    private String nombreSistema;

    @Column(name = "URL_ENDPOINT", nullable = false, length = 300)
    private String urlEndpoint;

    @Column(name = "SECRETO_HMAC", nullable = false, length = 128)
    private String secretoHmac;

    @Lob
    @Column(name = "EVENTOS_SUSCRITOS", nullable = false, columnDefinition = "CLOB")
    private String eventosSuscritos;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    @Builder.Default
    private String activo = "S";

    @Column(name = "FECHA_REGISTRO", nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public boolean isActivo() {
        return "S".equalsIgnoreCase(this.activo);
    }
}
