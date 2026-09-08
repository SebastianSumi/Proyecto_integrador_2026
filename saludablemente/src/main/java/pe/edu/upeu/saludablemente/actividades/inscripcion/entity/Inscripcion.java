package pe.edu.upeu.saludablemente.actividades.inscripcion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "ACTIVITY_ENROLLMENTS", schema = "SAL_ACTIVITIES",
        uniqueConstraints = @UniqueConstraint(name = "UK_ACTIVITY_ENROLLMENT_PERSON",
                columnNames = {"ACTIVITY_ID", "PERSON_ID"}))
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ACTIVITY_ID", nullable = false)
    private Long activityId;

    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion state;

    @Column(name = "ENROLLED_AT", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    protected Inscripcion() {
    }

    public Inscripcion(Long activityId, Long personId, LocalDateTime enrolledAt) {
        this.activityId = activityId;
        this.personId = personId;
        reactivate(enrolledAt);
    }

    public void reactivate(LocalDateTime at) {
        state = EstadoInscripcion.ENROLLED;
        enrolledAt = at;
        cancelledAt = null;
    }

    public void cancel(LocalDateTime at) {
        if (state == EstadoInscripcion.CANCELLED) {
            return;
        }
        state = EstadoInscripcion.CANCELLED;
        cancelledAt = at;
    }

    public Long getId() { return id; }
    public Long getActivityId() { return activityId; }
    public Long getPersonId() { return personId; }
    public EstadoInscripcion getState() { return state; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
}
