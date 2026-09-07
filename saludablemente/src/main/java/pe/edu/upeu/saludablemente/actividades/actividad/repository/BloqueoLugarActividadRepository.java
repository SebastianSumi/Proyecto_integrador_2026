package pe.edu.upeu.saludablemente.actividades.actividad.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.BloqueoLugarActividad;

public interface BloqueoLugarActividadRepository extends JpaRepository<BloqueoLugarActividad, String> {

    @Modifying
    @Query(value = """
            MERGE INTO SAL_ACTIVITIES.ACTIVITY_PLACE_LOCKS target
            USING (SELECT :placeKey AS PLACE_KEY FROM DUAL) source
               ON (target.PLACE_KEY = source.PLACE_KEY)
            WHEN NOT MATCHED THEN INSERT (PLACE_KEY) VALUES (source.PLACE_KEY)
            """, nativeQuery = true)
    void ensureExists(@Param("placeKey") String placeKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lock from BloqueoLugarActividad lock where lock.placeKey = :placeKey")
    BloqueoLugarActividad lockByPlaceKey(@Param("placeKey") String placeKey);
}
