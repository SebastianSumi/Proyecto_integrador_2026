package pe.edu.upeu.saludablemente.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.saludablemente.auditoria.enums.TipoOperacion;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "BITACORA_TRANSACCIONAL", schema = "SALUD_AUDITORIA")
@IdClass(BitacoraTransaccionalEntity.BitacoraId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraTransaccionalEntity {

    @Id
    @Column(name = "ID_BITACORA", columnDefinition = "RAW(16)")
    private UUID idBitacora;

    @Id
    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "SECUENCIA", nullable = false)
    private Long secuencia;

    @Column(name = "TIPO_EVENTO", nullable = false, length = 80)
    private String tipoEvento;

    @Column(name = "ENTIDAD_AFECTADA", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "ID_ENTIDAD", nullable = false, length = 100)
    private String idEntidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_OPERACION", nullable = false, length = 20)
    private TipoOperacion tipoOperacion;

    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @Column(name = "USUARIO_AUTOR", nullable = false, length = 100)
    private String usuarioAutor;

    @Column(name = "ROL_USUARIO", length = 100)
    private String rolUsuario;

    @Column(name = "TENANT_ID", length = 50)
    private String tenantId;

    @Lob
    @Column(name = "DIFERENCIAL_CAMBIOS", columnDefinition = "CLOB")
    private String diferencialCambios;

    @Lob
    @Column(name = "SNAPSHOT_ANTERIOR", columnDefinition = "CLOB")
    private String snapshotAnterior;

    @Lob
    @Column(name = "SNAPSHOT_POSTERIOR", columnDefinition = "CLOB")
    private String snapshotPosterior;

    @Column(name = "DIRECCION_IP", length = 45)
    private String direccionIp;

    @Column(name = "PUERTO_REMOTO", length = 10)
    private String puertoRemoto;

    @Column(name = "USER_AGENT", length = 255)
    private String userAgent;

    @Column(name = "ENDPOINT_HTTP", length = 200)
    private String endpointHttp;

    @Column(name = "METODO_HTTP", length = 10)
    private String metodoHttp;

    @Column(name = "CODIGO_RESPUESTA_HTTP")
    private Integer codigoRespuestaHttp;

    @Column(name = "HASH_REGISTRO", nullable = false, length = 64)
    private String hashRegistro;

    @Column(name = "HASH_ANTERIOR", length = 64)
    private String hashAnterior;

    @Column(name = "FUERA_HORARIO_LABORAL", length = 1)
    private String fueraHorarioLaboral;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BitacoraId implements Serializable {
        private UUID idBitacora;
        private LocalDateTime fechaRegistro;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BitacoraId that)) return false;
            return Objects.equals(idBitacora, that.idBitacora)
                    && Objects.equals(fechaRegistro, that.fechaRegistro);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idBitacora, fechaRegistro);
        }
    }
}
