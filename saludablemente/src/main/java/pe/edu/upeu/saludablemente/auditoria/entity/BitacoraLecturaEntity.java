package pe.edu.upeu.saludablemente.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "BITACORA_LECTURA", schema = "SALUD_AUDITORIA")
@IdClass(BitacoraLecturaEntity.BitacoraLecturaId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraLecturaEntity {

    @Id
    @Column(name = "ID_LECTURA", columnDefinition = "RAW(16)")
    private UUID idLectura;

    @Id
    @Column(name = "FECHA_ACCESO", nullable = false)
    private LocalDateTime fechaAcceso;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "TIPO_ENTIDAD", nullable = false, length = 50)
    private String tipoEntidad;

    @Column(name = "ID_ENTIDAD", nullable = false, length = 100)
    private String idEntidad;

    @Column(name = "USUARIO_LECTOR", nullable = false, length = 100)
    private String usuarioLector;

    @Column(name = "DIRECCION_IP", length = 45)
    private String direccionIp;

    @Column(name = "USER_AGENT", length = 255)
    private String userAgent;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BitacoraLecturaId implements Serializable {
        private UUID idLectura;
        private LocalDateTime fechaAcceso;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BitacoraLecturaId that)) return false;
            return Objects.equals(idLectura, that.idLectura)
                    && Objects.equals(fechaAcceso, that.fechaAcceso);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idLectura, fechaAcceso);
        }
    }
}
