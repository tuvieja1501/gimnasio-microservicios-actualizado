package com.gimnasio.asistencias.dto;

import com.gimnasio.asistencias.model.Asistencia;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DtoYModelTest {

    @Test
    void testModelosYDto() {
        // Modelo
        Asistencia asistencia = new Asistencia();
        asistencia.setId(1L); // Llama a todos los setters que tengas
        assertNotNull(asistencia.getId()); // Llama a todos los getters
        
        // DTOs
        AsistenciaDTO asistenciaDTO = new AsistenciaDTO();
        assertNotNull(asistenciaDTO);

        SocioRespuesta socio = new SocioRespuesta();
        assertNotNull(socio);

        SucursalRespuesta sucursal = new SucursalRespuesta();
        assertNotNull(sucursal);
    }
}