package pe.edu.upeu.saludablemente.personal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "credencial_programa", schema = "SLB_PERSONAL")
public class CredencialPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credencial")
    private Long id;

    @Column(name = "codigo_qr_hash", nullable = false, unique = true, length = 255)
    private String codigoQrHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_credencial", nullable = false, length = 50)
    private TipoCredencial tipoCredencial;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCredencial estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;
}