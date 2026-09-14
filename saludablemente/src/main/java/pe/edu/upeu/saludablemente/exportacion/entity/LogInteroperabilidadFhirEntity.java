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
@Table(name = "LOG_INTEROPERABILIDAD_FHIR", schema = "SALUD_EXPORTACION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogInteroperabilidadFhirEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_LOG", columnDefinition = "RAW(16)")
    private UUID idLog;

    @Column(name = "ID_SISTEMA_CLIENTE", nullable = false, length = 100)
    private String idSistemaCliente;

    @Column(name = "RECURSO_SOLICITADO", nullable = false, length = 50)
    private String recursoSolicitado;

    @Lob
    @Column(name = "PARAMETROS_CONSULTA", columnDefinition = "CLOB")
    private String parametrosConsulta;

    @Column(name = "CODIGO_HTTP_RESPUESTA", nullable = false)
    private Integer codigoHttpRespuesta;

    @Column(name = "DIRECCION_IP", nullable = false, length = 45)
    private String direccionIp;

    @Column(name = "TIEMPO_RESPUESTA_MS", nullable = false)
    private Integer tiempoRespuestaMs;

    @Column(name = "FECHA_PETICION", nullable = false)
    @Builder.Default
    private LocalDateTime fechaPeticion = LocalDateTime.now();
}
