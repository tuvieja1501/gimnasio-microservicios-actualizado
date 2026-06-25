package com.gimnasio.membresias.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gimnasio.membresias.dto.MembresiaDTO;
import com.gimnasio.membresias.exception.RecursoNoEncontradoException;
import com.gimnasio.membresias.exception.ReglaNegocioException;
import com.gimnasio.membresias.model.EstadoMembresia;
import com.gimnasio.membresias.model.Membresia;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.service.MembresiaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembresiaController.class)
class MembresiaControllerTest {
    @Autowired MockMvc mvc;
    @MockBean MembresiaService service;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    PlanMembresia plan() {
        PlanMembresia p = new PlanMembresia(); p.setId(1L); p.setNombre("Mensual");
        p.setDuracionMeses(1); p.setPrecio(new BigDecimal("30000")); p.setActivo(true);
        return p;
    }

    Membresia membresia() {
        Membresia m = new Membresia(); m.setId(1L); m.setSocioId(10L); m.setPlan(plan());
        m.setFechaInicio(LocalDate.now()); m.setFechaFin(LocalDate.now().plusMonths(1));
        m.setEstado(EstadoMembresia.VIGENTE);
        return m;
    }

    MembresiaDTO dto() {
        MembresiaDTO d = new MembresiaDTO(); d.setSocioId(10L); d.setPlanId(1L); d.setFechaInicio(LocalDate.now());
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(membresia()));
        mvc.perform(get("/api/membresias")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(membresia());
        mvc.perform(get("/api/membresias/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/membresias/99")).andExpect(status().isNotFound());
    }

    @Test void porSocio() throws Exception {
        when(service.listarPorSocio(10L)).thenReturn(List.of(membresia()));
        mvc.perform(get("/api/membresias/socio/10")).andExpect(status().isOk());
    }

    @Test void vigente() throws Exception {
        when(service.tieneVigente(10L)).thenReturn(true);
        mvc.perform(get("/api/membresias/socio/10/vigente"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.vigente").value(true));
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(membresia());
        mvc.perform(post("/api/membresias").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void crearConflicto() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("ya tiene vigente"));
        mvc.perform(post("/api/membresias").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test void cancelar() throws Exception {
        Membresia cancelada = membresia(); cancelada.setEstado(EstadoMembresia.CANCELADA);
        when(service.cancelar(1L)).thenReturn(cancelada);
        mvc.perform(patch("/api/membresias/1/cancelar")).andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/membresias/1")).andExpect(status().isNoContent());
    }
}
