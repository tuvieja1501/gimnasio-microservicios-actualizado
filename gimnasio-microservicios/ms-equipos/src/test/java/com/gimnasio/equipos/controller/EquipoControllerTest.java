package com.gimnasio.equipos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gimnasio.equipos.dto.EquipoDTO;
import com.gimnasio.equipos.exception.RecursoNoEncontradoException;
import com.gimnasio.equipos.exception.ReglaNegocioException;
import com.gimnasio.equipos.model.Equipo;
import com.gimnasio.equipos.model.EstadoEquipo;
import com.gimnasio.equipos.service.EquipoService;
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

@WebMvcTest(EquipoController.class)
class EquipoControllerTest {
    @Autowired MockMvc mvc;
    @MockBean EquipoService service;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    Equipo equipo() {
        Equipo e = new Equipo(); e.setId(1L); e.setNombre("Cinta"); e.setTipo("Cardio");
        e.setCodigoInterno("CINTA-001"); e.setSucursalId(1L); e.setEstado(EstadoEquipo.OPERATIVO);
        return e;
    }

    EquipoDTO dto() {
        EquipoDTO d = new EquipoDTO(); d.setNombre("Cinta"); d.setTipo("Cardio");
        d.setCodigoInterno("CINTA-001"); d.setSucursalId(1L); d.setEstado(EstadoEquipo.OPERATIVO);
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(equipo()));
        mvc.perform(get("/api/equipos")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(equipo());
        mvc.perform(get("/api/equipos/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/equipos/99")).andExpect(status().isNotFound());
    }

    @Test void porSucursal() throws Exception {
        when(service.listarPorSucursal(1L)).thenReturn(List.of(equipo()));
        mvc.perform(get("/api/equipos/sucursal/1")).andExpect(status().isOk());
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(equipo());
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void crearConflicto() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("codigo duplicado"));
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(equipo());
        mvc.perform(put("/api/equipos/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test void cambiarEstado() throws Exception {
        when(service.cambiarEstado(1L, EstadoEquipo.EN_MANTENIMIENTO)).thenReturn(equipo());
        mvc.perform(patch("/api/equipos/1/estado").param("estado", "EN_MANTENIMIENTO")).andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/equipos/1")).andExpect(status().isNoContent());
    }
}
