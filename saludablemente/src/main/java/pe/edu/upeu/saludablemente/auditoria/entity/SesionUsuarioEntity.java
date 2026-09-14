package pe.edu.upeu.saludablemente.auditoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SESION_USUARIO", schema = "SALUD_AUDITORIA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionUsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_SESION", columnDefinition = "RAW(16)")
    private UUID idSesion;

    @Column(name = "USUARIO", nullable = false, length = 100)
    private String usuario;

    @Column(name = "DIRECCION_IP", nullable = false, length = 45)
    private String direccionIp;

    @Column(name = "CIUDAD", length = 100)
    private String ciudad;

    @Column(name = "PAIS", length = 100)
    private String pais;

    @Column(name = "CODIGO_PAIS", length = 5)
    private String codigoPais;

    @Column(name = "LATITUD")
    private Double latitud;

    @Column(name = "LONGITUD")
    private Double longitud;

    @Column(name = "USER_AGENT", length = 255)
    private String userAgent;

    @Column(name = "FECHA_LOGIN", nullable = false)
    private LocalDateTime fechaLogin;

    @Column(name = "SESION_ACTIVA", length = 1)
    @Builder.Default
    private String sesionActiva = "S";
}
