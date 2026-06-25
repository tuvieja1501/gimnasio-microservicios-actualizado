package com.gimnasio.instructores.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimnasio.instructores.dto.InstructorDTO;
import com.gimnasio.instructores.exception.RecursoNoEncontradoException;
import com.gimnasio.instructores.exception.ReglaNegocioException;
import com.gimnasio.instructores.model.Instructor;
import com.gimnasio.instructores.service.InstructorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstructorController.class)
class InstructorControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean InstructorService service;

    Instructor instructor() {
        Instructor i = new Instructor(); i.setId(1L); i.setNombre("Carlos"); i.setApellido("Lopez");
        i.setRut("12345678-9"); i.setEmail("carlos@mail.com"); i.setEspecialidad("Yoga");
        i.setAniosExperiencia(5); i.setActivo(true);
        return i;
    }

    InstructorDTO dto() {
        InstructorDTO d = new InstructorDTO(); d.setNombre("Carlos"); d.setApellido("Lopez");
        d.setRut("12345678-9"); d.setEmail("carlos@mail.com"); d.setEspecialidad("Yoga");
        d.setAniosExperiencia(5); d.setActivo(true);
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(instructor()));
        mvc.perform(get("/api/instructores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Carlos"));
    }

    @Test void listarActivos() throws Exception {
        when(service.listarActivos()).thenReturn(List.of(instructor()));
        mvc.perform(get("/api/instructores/activos")).andExpect(status().isOk());
    }

    @Test void porEspecialidad() throws Exception {
        when(service.buscarPorEspecialidad("yoga")).thenReturn(List.of(instructor()));
        mvc.perform(get("/api/instructores/especialidad/yoga")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(instructor());
        mvc.perform(get("/api/instructores/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/instructores/99")).andExpect(status().isNotFound());
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(instructor());
        mvc.perform(post("/api/instructores").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void crearConflicto() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("RUT duplicado"));
        mvc.perform(post("/api/instructores").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(instructor());
        mvc.perform(put("/api/instructores/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/instructores/1")).andExpect(status().isNoContent());
    }

    @Test void eliminarInexistente() throws Exception {
        doThrow(new RecursoNoEncontradoException("no existe")).when(service).eliminar(99L);
        mvc.perform(delete("/api/instructores/99")).andExpect(status().isNotFound());
    }
}
