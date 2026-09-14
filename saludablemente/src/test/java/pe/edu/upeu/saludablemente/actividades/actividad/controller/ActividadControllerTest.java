package pe.edu.upeu.saludablemente.actividades.actividad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadRequest;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResponse;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadAgregado;
import pe.edu.upeu.saludablemente.actividades.actividad.dto.ActividadResumen;
import pe.edu.upeu.saludablemente.actividades.actividad.entity.EstadoActividad;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.CriterioConsultaActividadInvalidoException;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.ActividadSolapadaException;
import pe.edu.upeu.saludablemente.actividades.actividad.exception.HorarioActividadInvalidoException;
import pe.edu.upeu.saludablemente.actividades.actividad.service.ActividadService;
import pe.edu.upeu.saludablemente.actividades.inscripcion.dto.InscripcionDetalleResponse;
import pe.edu.upeu.saludablemente.actividades.inscripcion.entity.EstadoInscripcion;
import pe.edu.upeu.saludablemente.exception.GlobalExceptionHandler;
import pe.edu.upeu.saludablemente.exception.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ActividadControllerTest {

    @Mock
    private ActividadService service;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ActividadController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new SortHandlerMethodArgumentResolver())
                .setValidator(new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()))
                .build();
    }

    @Test
    void listsActivities() throws Exception {
        when(service.findAll()).thenReturn(List.of(response(1L, "Taller de bienestar")));

        mockMvc.perform(get("/api/v1/actividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Taller de bienestar"));

        verify(service).findAll();
    }

    @Test
    void returnsActivityById() throws Exception {
        when(service.findById(7L)).thenReturn(response(7L, "Taller de bienestar"));

        mockMvc.perform(get("/api/v1/actividades/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    void returnsActivityDetailWithEmbeddedEnrollments() throws Exception {
        when(service.findDetalleById(7L)).thenReturn(detalleResponse(7L));

        mockMvc.perform(get("/api/v1/actividades/7/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.inscripciones[0].id").value(31L))
                .andExpect(jsonPath("$.inscripciones[0].personaId").value(8L))
                .andExpect(jsonPath("$.inscripciones[0].estado").value("INSCRITA"))
                .andExpect(jsonPath("$.inscripciones[0].actividad").doesNotExist())
                .andExpect(jsonPath("$.inscripciones[0].actividadId").doesNotExist());

        verify(service).findDetalleById(7L);
    }

    @Test
    void searchesActivitiesWithOptionalFiltersAndSort() throws Exception {
        when(service.search(
                EstadoActividad.PROGRAMADA,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                Sort.by(Sort.Order.desc("nombre"))
        )).thenReturn(List.of(resumen(7L, "Caminata")));

        mockMvc.perform(get("/api/v1/actividades/busqueda")
                        .param("estado", "PROGRAMADA")
                        .param("desde", "2026-10-01")
                        .param("hasta", "2026-10-31")
                        .param("sort", "nombre,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7L))
                .andExpect(jsonPath("$[0].nombre").value("Caminata"));

        verify(service).search(
                EstadoActividad.PROGRAMADA,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                Sort.by(Sort.Order.desc("nombre"))
        );
    }

    @Test
    void searchesActivitiesByDateWhenSortIsOmitted() throws Exception {
        when(service.search(null, null, null, Sort.by("fecha")))
                .thenReturn(List.of(resumen(7L, "Caminata")));

        mockMvc.perform(get("/api/v1/actividades/busqueda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7L));

        verify(service).search(null, null, null, Sort.by("fecha"));
    }

    @Test
    void returnsActivityAggregatesForDateRange() throws Exception {
        when(service.getAggregates(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(new ActividadAgregado(EstadoActividad.PROGRAMADA, 3L)));

        mockMvc.perform(get("/api/v1/actividades/resumen")
                        .param("desde", "2026-10-01")
                        .param("hasta", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$[0].total").value(3));

        verify(service).getAggregates(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    @Test
    void mapsInvalidSearchDateRangeToBadRequest() throws Exception {
        when(service.search(any(), any(), any(), any()))
                .thenThrow(new CriterioConsultaActividadInvalidoException(
                        "La fecha desde no puede ser posterior a la fecha hasta"
                ));

        mockMvc.perform(get("/api/v1/actividades/busqueda")
                        .param("desde", "2026-10-31")
                        .param("hasta", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La fecha desde no puede ser posterior a la fecha hasta"));
    }

    @Test
    void mapsInvalidSearchSortToBadRequest() throws Exception {
        when(service.search(any(), any(), any(), any()))
                .thenThrow(new CriterioConsultaActividadInvalidoException(
                        "No se permite ordenar actividades por: createdAt"
                ));

        mockMvc.perform(get("/api/v1/actividades/busqueda")
                        .param("sort", "createdAt,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No se permite ordenar actividades por: createdAt"));
    }

    @Test
    void createsActivityFromValidRequest() throws Exception {
        when(service.create(any(ActividadRequest.class))).thenReturn(response(10L, "Taller de bienestar"));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));

        verify(service).create(any(ActividadRequest.class));
    }

    @Test
    void updatesActivityFromValidRequest() throws Exception {
        when(service.update(any(Long.class), any(ActividadRequest.class)))
                .thenReturn(response(10L, "Taller actualizado"));

        mockMvc.perform(put("/api/v1/actividades/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Taller actualizado"));

        verify(service).update(any(Long.class), any(ActividadRequest.class));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        ActividadRequest invalidRequest = request();
        invalidRequest.setNombre(" ");

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(service, never()).create(any(ActividadRequest.class));
    }

    @Test
    void mapsInvalidScheduleToBadRequest() throws Exception {
        when(service.create(any(ActividadRequest.class)))
                .thenThrow(new HorarioActividadInvalidoException("La hora de inicio debe ser anterior a la hora de fin"));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("La hora de inicio debe ser anterior a la hora de fin"));
    }

    @Test
    void mapsMissingActivityToNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Actividad with id 99 was not found"));

        mockMvc.perform(get("/api/v1/actividades/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void mapsMissingDetailedActivityToNotFound() throws Exception {
        when(service.findDetalleById(99L))
                .thenThrow(new ResourceNotFoundException("Actividad with id 99 was not found"));

        mockMvc.perform(get("/api/v1/actividades/99/detalle"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void mapsScheduleConflictToConflict() throws Exception {
        when(service.create(any(ActividadRequest.class)))
                .thenThrow(new ActividadSolapadaException("Ya existe una actividad programada en ese lugar y horario"));

        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ya existe una actividad programada en ese lugar y horario"));
    }

    @Test
    void mapsMalformedJsonToBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error de formato en los datos enviados"));

        verify(service, never()).create(any(ActividadRequest.class));
    }

    @Test
    void mapsInvalidPathVariableTypeToBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/actividades/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error de formato en los parámetros enviados"));

        verify(service, never()).findById(any());
    }

    private ActividadRequest request() {
        ActividadRequest request = new ActividadRequest();
        request.setNombre("Taller de bienestar");
        request.setFecha(LocalDate.of(2026, 10, 15));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(11, 0));
        request.setLugar("Sala principal");
        request.setCreadorId(5L);
        return request;
    }

    private ActividadResponse response(Long id, String nombre) {
        return ActividadResponse.builder()
                .id(id)
                .nombre(nombre)
                .fecha(LocalDate.of(2026, 10, 15))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(11, 0))
                .lugar("Sala principal")
                .estado(EstadoActividad.PROGRAMADA)
                .creadorId(5L)
                .build();
    }

    private ActividadResumen resumen(Long id, String nombre) {
        return new ActividadResumen(
                id,
                nombre,
                LocalDate.of(2026, 10, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "Sala principal",
                EstadoActividad.PROGRAMADA
        );
    }

    private ActividadDetalleResponse detalleResponse(Long id) {
        return ActividadDetalleResponse.builder()
                .id(id)
                .nombre("Taller de bienestar")
                .fecha(LocalDate.of(2026, 10, 15))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(11, 0))
                .lugar("Sala principal")
                .estado(EstadoActividad.PROGRAMADA)
                .creadorId(5L)
                .inscripciones(List.of(
                        InscripcionDetalleResponse.builder()
                                .id(31L)
                                .personaId(8L)
                                .estado(EstadoInscripcion.INSCRITA)
                                .build()
                ))
                .build();
    }
}
