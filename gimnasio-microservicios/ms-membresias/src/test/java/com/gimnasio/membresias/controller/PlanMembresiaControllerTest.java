package com.gimnasio.membresias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimnasio.membresias.dto.PlanMembresiaDTO;
import com.gimnasio.membresias.exception.RecursoNoEncontradoException;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.service.PlanMembresiaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanMembresiaController.class)
class PlanMembresiaControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean PlanMembresiaService service;

    PlanMembresia plan() {
        PlanMembresia p = new PlanMembresia(); p.setId(1L); p.setNombre("Mensual");
        p.setDuracionMeses(1); p.setPrecio(new BigDecimal("30000")); p.setActivo(true);
        return p;
    }

    PlanMembresiaDTO dto() {
        PlanMembresiaDTO d = new PlanMembresiaDTO(); d.setNombre("Mensual");
        d.setDuracionMeses(1); d.setPrecio(new BigDecimal("30000")); d.setActivo(true);
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(plan()));
        mvc.perform(get("/api/planes")).andExpect(status().isOk());
    }

    @Test void listarActivos() throws Exception {
        when(service.listarActivos()).thenReturn(List.of(plan()));
        mvc.perform(get("/api/planes/activos")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(plan());
        mvc.perform(get("/api/planes/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/planes/99")).andExpect(status().isNotFound());
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(plan());
        mvc.perform(post("/api/planes").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(plan());
        mvc.perform(put("/api/planes/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/planes/1")).andExpect(status().isNoContent());
    }
}
