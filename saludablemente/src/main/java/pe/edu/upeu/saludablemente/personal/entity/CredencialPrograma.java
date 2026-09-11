package pe.edu.upeu.saludablemente.personal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "credencial_programa")
public class CredencialPrograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCredencial;

    @Column(unique = true)
    private String codigoQrHash;

    private String tipoCredencial;

    private LocalDateTime fechaEmision;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;
}