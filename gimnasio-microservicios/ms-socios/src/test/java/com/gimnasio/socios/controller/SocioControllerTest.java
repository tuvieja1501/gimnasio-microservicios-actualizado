package com.gimnasio.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimnasio.socios.dto.SocioDTO;
import com.gimnasio.socios.exception.RecursoNoEncontradoException;
import com.gimnasio.socios.exception.ReglaNegocioException;
import com.gimnasio.socios.model.EstadoSocio;
import com.gimnasio.socios.model.Socio;
import com.gimnasio.socios.service.SocioService;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(SocioController.class)
@DisplayName("SocioController - pruebas de capa web")
class SocioControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean SocioService service;

    private Socio socio() {
        Socio s = new Socio();
        s.setId(1L); s.setNombre("Ana"); s.setApellido("Soto");
        s.setRut("11111111-1"); s.setEmail("ana@mail.com");
        s.setFechaRegistro(LocalDate.now()); s.setEstado(EstadoSocio.ACTIVO);
        return s;
    }

    private SocioDTO dto() {
        SocioDTO d = new SocioDTO();
        d.setNombre("Ana"); d.setApellido("Soto"); d.setRut("11111111-1");
        d.setEmail("ana@mail.com"); d.setFechaNacimiento(LocalDate.now().minusYears(25));
        d.setEstado(EstadoSocio.ACTIVO);
        return d;
    }

    @Test @DisplayName("GET /api/socios -> 200")
    void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(socio()));
        mvc.perform(get("/api/socios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rut").value("11111111-1"));
    }

    @Test @DisplayName("GET /api/socios/{id} existente -> 200")
    void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(socio());
        mvc.perform(get("/api/socios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test @DisplayName("GET /api/socios/{id} inexistente -> 404")
    void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/socios/99")).andExpect(status().isNotFound());
    }

    @Test @DisplayName("GET /api/socios/rut/{rut} -> 200")
    void obtenerPorRut() throws Exception {
        when(service.buscarPorRut("11111111-1")).thenReturn(socio());
        mvc.perform(get("/api/socios/rut/11111111-1"))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/socios valido -> 201")
    void crear() throws Exception {
        when(service.crear(any())).thenReturn(socio());
        mvc.perform(post("/api/socios").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /api/socios campos invalidos -> 400")
    void crearInvalido() throws Exception {
        SocioDTO bad = new SocioDTO();
        mvc.perform(post("/api/socios").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("POST /api/socios RUT duplicado -> 409")
    void crearRutDuplicado() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("RUT duplicado"));
        mvc.perform(post("/api/socios").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test @DisplayName("PUT /api/socios/{id} -> 200")
    void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(socio());
        mvc.perform(put("/api/socios/1").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("PATCH /api/socios/{id}/estado -> 200")
    void cambiarEstado() throws Exception {
        Socio moroso = socio(); moroso.setEstado(EstadoSocio.MOROSO);
        when(service.cambiarEstado(1L, EstadoSocio.MOROSO)).thenReturn(moroso);
        mvc.perform(patch("/api/socios/1/estado").param("estado", "MOROSO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("MOROSO"));
    }

    @Test @DisplayName("DELETE /api/socios/{id} -> 204")
    void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/socios/1")).andExpect(status().isNoContent());
    }

    @Test @DisplayName("DELETE /api/socios/{id} inexistente -> 404")
    void eliminarInexistente() throws Exception {
        doThrow(new RecursoNoEncontradoException("no existe")).when(service).eliminar(99L);
        mvc.perform(delete("/api/socios/99")).andExpect(status().isNotFound());
    }
}
