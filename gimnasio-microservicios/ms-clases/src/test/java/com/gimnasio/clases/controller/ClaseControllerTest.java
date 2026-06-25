package com.gimnasio.clases.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gimnasio.clases.dto.ClaseDTO;
import com.gimnasio.clases.exception.RecursoNoEncontradoException;
import com.gimnasio.clases.exception.ReglaNegocioException;
import com.gimnasio.clases.model.Clase;
import com.gimnasio.clases.service.ClaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaseController.class)
class ClaseControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ClaseService service;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    Clase clase() {
        Clase c = new Clase(); c.setId(1L); c.setNombre("Yoga"); c.setInstructorId(1L);
        c.setSucursalId(1L); c.setFechaHora(LocalDateTime.now().plusDays(1));
        c.setDuracionMinutos(60); c.setCupoMaximo(20); c.setCuposDisponibles(18);
        return c;
    }

    ClaseDTO dto() {
        ClaseDTO d = new ClaseDTO(); d.setNombre("Yoga"); d.setInstructorId(1L); d.setSucursalId(1L);
        d.setFechaHora(LocalDateTime.now().plusDays(1)); d.setDuracionMinutos(60); d.setCupoMaximo(20);
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(clase()));
        mvc.perform(get("/api/clases")).andExpect(status().isOk());
    }

    @Test void listarFuturas() throws Exception {
        when(service.listarFuturas()).thenReturn(List.of(clase()));
        mvc.perform(get("/api/clases/futuras")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(clase());
        mvc.perform(get("/api/clases/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/clases/99")).andExpect(status().isNotFound());
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(clase());
        mvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void crearConflicto() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("instructor inactivo"));
        mvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(clase());
        mvc.perform(put("/api/clases/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test void decrementarCupo() throws Exception {
        when(service.decrementarCupo(1L)).thenReturn(clase());
        mvc.perform(patch("/api/clases/1/decrementar-cupo")).andExpect(status().isOk());
    }

    @Test void incrementarCupo() throws Exception {
        when(service.incrementarCupo(1L)).thenReturn(clase());
        mvc.perform(patch("/api/clases/1/incrementar-cupo")).andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/clases/1")).andExpect(status().isNoContent());
    }
}
