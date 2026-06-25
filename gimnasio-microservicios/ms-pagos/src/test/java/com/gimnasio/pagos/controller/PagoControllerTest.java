package com.gimnasio.pagos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimnasio.pagos.dto.PagoDTO;
import com.gimnasio.pagos.exception.RecursoNoEncontradoException;
import com.gimnasio.pagos.exception.ReglaNegocioException;
import com.gimnasio.pagos.model.EstadoPago;
import com.gimnasio.pagos.model.MetodoPago;
import com.gimnasio.pagos.model.Pago;
import com.gimnasio.pagos.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
class PagoControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean PagoService service;

    Pago pago() {
        Pago p = new Pago(); p.setId(1L); p.setSocioId(10L); p.setMembresiaId(5L);
        p.setMonto(new BigDecimal("50000")); p.setMetodoPago(MetodoPago.EFECTIVO);
        p.setFechaPago(LocalDateTime.now()); p.setEstado(EstadoPago.PAGADO);
        return p;
    }

    PagoDTO dto() {
        PagoDTO d = new PagoDTO(); d.setSocioId(10L); d.setMembresiaId(5L);
        d.setMonto(new BigDecimal("50000")); d.setMetodoPago(MetodoPago.EFECTIVO);
        return d;
    }

    @Test void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(pago()));
        mvc.perform(get("/api/pagos")).andExpect(status().isOk());
    }

    @Test void obtenerExistente() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(pago());
        mvc.perform(get("/api/pagos/1")).andExpect(status().isOk());
    }

    @Test void obtenerInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("no existe"));
        mvc.perform(get("/api/pagos/99")).andExpect(status().isNotFound());
    }

    @Test void porSocio() throws Exception {
        when(service.listarPorSocio(10L)).thenReturn(List.of(pago()));
        mvc.perform(get("/api/pagos/socio/10")).andExpect(status().isOk());
    }

    @Test void porMembresia() throws Exception {
        when(service.listarPorMembresia(5L)).thenReturn(List.of(pago()));
        mvc.perform(get("/api/pagos/membresia/5")).andExpect(status().isOk());
    }

    @Test void registrar() throws Exception {
        when(service.registrar(any())).thenReturn(pago());
        mvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated());
    }

    @Test void registrarSocioInexistente() throws Exception {
        when(service.registrar(any())).thenThrow(new RecursoNoEncontradoException("socio no existe"));
        mvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(dto())))
                .andExpect(status().isNotFound());
    }

    @Test void anular() throws Exception {
        Pago anulado = pago(); anulado.setEstado(EstadoPago.ANULADO);
        when(service.anular(1L)).thenReturn(anulado);
        mvc.perform(patch("/api/pagos/1/anular")).andExpect(status().isOk());
    }

    @Test void anularYaAnulado() throws Exception {
        when(service.anular(1L)).thenThrow(new ReglaNegocioException("ya anulado"));
        mvc.perform(patch("/api/pagos/1/anular")).andExpect(status().isConflict());
    }

    @Test void eliminar() throws Exception {
        doNothing().when(service).eliminar(1L);
        mvc.perform(delete("/api/pagos/1")).andExpect(status().isNoContent());
    }
}
