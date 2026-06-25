package com.gimnasio.sucursales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gimnasio.sucursales.dto.SucursalDTO;
import com.gimnasio.sucursales.exception.RecursoNoEncontradoException;
import com.gimnasio.sucursales.exception.ReglaNegocioException;
import com.gimnasio.sucursales.model.Sucursal;
import com.gimnasio.sucursales.service.SucursalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SucursalController.class)
@DisplayName("SucursalController - pruebas web")
class SucursalControllerTest {

    @Autowired MockMvc mvc;
    @MockBean SucursalService service;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    Sucursal sucursal() {
        Sucursal s = new Sucursal(); s.setId(1L); s.setNombre("Sede Centro");
        s.setDireccion("Av. Main 100"); s.setComuna("Santiago"); s.setCapacidad(50);
        s.setHoraApertura(LocalTime.of(8,0)); s.setHoraCierre(LocalTime.of(22,0)); s.setActiva(true);
        return s;
    }

    SucursalDTO dto() {
        SucursalDTO d = new SucursalDTO(); d.setNombre("Sede Centro"); d.setDireccion("Av. Main 100");
        d.setComuna("Santiago"); d.setCapacidad(50); d.setHoraApertura(LocalTime.of(8,0));
        d.setHoraCierre(LocalTime.of(22,0)); d.setActiva(true);
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(sucursal()));
        mvc.perform(get("/api/sucursales")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Sede Centro"));
    }

    @Test void listarActivas() throws Exception {
        when(service.listarActivas()).thenReturn(List.of(sucursal()));
        mvc.perform(get("/api/sucursales/activas")).andExpect(status().isOk());
    }

    @Test void porComuna() throws Exception {
        when(service.listarPorComuna("Santiago")).thenReturn(List.of(sucursal()));
        mvc.perform(get("/api/sucursales/comuna/Santiago")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(sucursal());
        mvc.perform(get("/api/sucursales/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/sucursales/99")).andExpect(status().isNotFound());
    }

    @Test void crear() throws Exception {
        when(service.crear(any())).thenReturn(sucursal());
        mvc.perform(post("/api/sucursales").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void crearConflicto() throws Exception {
        when(service.crear(any())).thenThrow(new ReglaNegocioException("nombre duplicado"));
        mvc.perform(post("/api/sucursales").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isConflict());
    }

    @Test void actualizar() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(sucursal());
        mvc.perform(put("/api/sucursales/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk());
    }

    @Test void cambiarEstado() throws Exception {
        when(service.cambiarEstado(1L, false)).thenReturn(sucursal());
        mvc.perform(patch("/api/sucursales/1/estado").param("activa", "false")).andExpect(status().isOk());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/sucursales/1")).andExpect(status().isNoContent());
    }

    @Test void eliminarInexistente() throws Exception {
        doThrow(new RecursoNoEncontradoException("no existe")).when(service).eliminar(99L);
        mvc.perform(delete("/api/sucursales/99")).andExpect(status().isNotFound());
    }
}
