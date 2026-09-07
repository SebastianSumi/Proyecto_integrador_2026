package pe.edu.upeu.saludablemente.actividades.actividad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pe.edu.upeu.saludablemente.exception.ConflictException;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "ACTIVITIES", schema = "SAL_ACTIVITIES")
public class Actividad {

    private static final Map<EstadoActividad, Set<EstadoActividad>> ALLOWED_TRANSITIONS = Map.of(
            EstadoActividad.SCHEDULED, EnumSet.of(EstadoActividad.IN_PROGRESS, EstadoActividad.CANCELLED),
            EstadoActividad.IN_PROGRESS, EnumSet.of(EstadoActividad.FINISHED, EstadoActividad.CANCELLED),
            EstadoActividad.FINISHED, EnumSet.noneOf(EstadoActividad.class),
            EstadoActividad.CANCELLED, EnumSet.noneOf(EstadoActividad.class)
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 100)
    private String place;

    @Column(name = "PLACE_KEY", nullable = false, length = 100)
    private String placeKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoActividad state = EstadoActividad.SCHEDULED;

    @Column(name = "CREATOR_USER_ID", nullable = false)
    private Long creatorUserId;

    @Version
    @Column(nullable = false)
    private long version;

    protected Actividad() {
    }

    public Actividad(String name, String description, LocalDateTime startAt, LocalDateTime endAt,
                    String place, String placeKey, Long creatorUserId) {
        updateDetails(name, description, startAt, endAt, place, placeKey);
        this.creatorUserId = creatorUserId;
    }

    public void updateDetails(String name, String description, LocalDateTime startAt, LocalDateTime endAt,
                              String place, String placeKey) {
        if (state != null && state != EstadoActividad.SCHEDULED) {
            throw new ConflictException("Only scheduled activities can be edited");
        }
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.name = name;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.place = place;
        this.placeKey = placeKey;
    }

    public void changeState(EstadoActividad requestedState) {
        if (state == requestedState) {
            return;
        }
        if (!ALLOWED_TRANSITIONS.get(state).contains(requestedState)) {
            throw new ConflictException("Cannot change activity state from " + state + " to " + requestedState);
        }
        state = requestedState;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public String getPlace() { return place; }
    public String getPlaceKey() { return placeKey; }
    public EstadoActividad getState() { return state; }
    public Long getCreatorUserId() { return creatorUserId; }
}
