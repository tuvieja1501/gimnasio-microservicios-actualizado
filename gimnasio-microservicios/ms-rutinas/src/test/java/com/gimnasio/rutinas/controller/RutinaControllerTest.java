package com.gimnasio.rutinas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimnasio.rutinas.dto.EjercicioDTO;
import com.gimnasio.rutinas.dto.RutinaDTO;
import com.gimnasio.rutinas.exception.RecursoNoEncontradoException;
import com.gimnasio.rutinas.exception.ReglaNegocioException;
import com.gimnasio.rutinas.model.Rutina;
import com.gimnasio.rutinas.service.RutinaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RutinaController.class)
class RutinaControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean RutinaService service;

    Rutina rutina() {
        Rutina r = new Rutina(); r.setId(1L); r.setNombre("Rutina Fuerza"); r.setSocioId(10L);
        r.setInstructorId(2L); r.setFechaCreacion(LocalDate.now()); r.setDuracionSemanas(8);
        return r;
    }

    RutinaDTO dto() {
        EjercicioDTO e = new EjercicioDTO(); e.setNombre("Sentadilla"); e.setSeries(3); e.setRepeticiones(10);
        RutinaDTO d = new RutinaDTO(); d.setNombre("Rutina Fuerza"); d.setSocioId(10L);
        d.setInstructorId(2L); d.setDuracionSemanas(8); d.setEjercicios(List.of(e));
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(rutina()));
        mvc.perform(get("/api/rutinas")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(rutina());
        mvc.perform(get("/api/rutinas/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/rutinas/99")).andExpect(status().isNotFound());
    }

    @Test void porSocio() throws Exception {
        when(service.listarPorSocio(10L)).thenReturn(List.of(rutina()));
        mvc.perform(get("/api/rutinas/socio/10")).andExpect(status().isOk());
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(rutina());
        mvc.perform(post("/api/rutinas").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void crearConflicto() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("socio inactivo"));
        mvc.perform(post("/api/rutinas").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(rutina());
        mvc.perform(put("/api/rutinas/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/rutinas/1")).andExpect(status().isNoContent());
    }
}
