package pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.saludablemente.recomendaciones_ia.ciclo_vida_recomendacion.entity.RecomendacionIAEntity;
import pe.edu.upeu.saludablemente.recomendaciones_ia.shared.enums.EstadoInferencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecomendacionIARepository extends JpaRepository<RecomendacionIAEntity, UUID> {

    Optional<RecomendacionIAEntity> findByIdPersonaAndVigenteTrue(Long idPersona);

    List<RecomendacionIAEntity> findByIdPersonaOrderByFechaGeneracionDesc(Long idPersona);

    @Modifying
    @Transactional
    @Query("UPDATE RecomendacionIAEntity r SET r.vigente = false WHERE r.idPersona = :idPersona AND r.vigente = true")
    void desactivarRecomendacionesVigentes(@Param("idPersona") Long idPersona);

    @Query("SELECT r FROM RecomendacionIAEntity r WHERE r.estadoInferencia = :estado AND r.fechaGeneracion < :fechaLimite")
    List<RecomendacionIAEntity> findByEstadoInferenciaAndFechaGeneracionBefore(
            @Param("estado") EstadoInferencia estado,
            @Param("fechaLimite") LocalDateTime fechaLimite
    );

    long countByEstadoInferencia(EstadoInferencia estado);

    @Query("SELECT COUNT(r) FROM RecomendacionIAEntity r WHERE r.vigente = true AND r.leida = false")
    long countRecomendacionesNoLeidas();

    List<RecomendacionIAEntity> findByEstadoInferencia(EstadoInferencia estado);
}