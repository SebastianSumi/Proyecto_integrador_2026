package pe.edu.upeu.saludablemente.actividades.actividad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ACTIVITY_PLACE_LOCKS", schema = "SAL_ACTIVITIES")
public class BloqueoLugarActividad {

    @Id
    @Column(name = "PLACE_KEY", length = 100)
    private String placeKey;

    protected BloqueoLugarActividad() {
    }
}
