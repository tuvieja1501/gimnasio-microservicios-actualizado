package com.gimnasio.socios.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Socio model y EstadoSocio enum")
class SocioModelTest {

    @Test @DisplayName("Socio constructor vacío y getters/setters")
    void gettersSetters() {
        Socio s = new Socio();
        s.setId(1L); s.setNombre("Juan"); s.setApellido("Perez");
        s.setRut("12345678-9"); s.setEmail("juan@mail.com");
        s.setTelefono("+56911111111");
        LocalDate hoy = LocalDate.now();
        s.setFechaNacimiento(hoy.minusYears(30));
        s.setFechaRegistro(hoy);
        s.setEstado(EstadoSocio.ACTIVO);

        assertThat(s.getId()).isEqualTo(1L);
        assertThat(s.getNombre()).isEqualTo("Juan");
        assertThat(s.getApellido()).isEqualTo("Perez");
        assertThat(s.getRut()).isEqualTo("12345678-9");
        assertThat(s.getEmail()).isEqualTo("juan@mail.com");
        assertThat(s.getTelefono()).isEqualTo("+56911111111");
        assertThat(s.getFechaNacimiento()).isEqualTo(hoy.minusYears(30));
        assertThat(s.getFechaRegistro()).isEqualTo(hoy);
        assertThat(s.getEstado()).isEqualTo(EstadoSocio.ACTIVO);
    }

    @Test @DisplayName("EstadoSocio tiene todos los valores esperados")
    void estadoSocioEnum() {
        assertThat(EstadoSocio.values()).containsExactlyInAnyOrder(
                EstadoSocio.ACTIVO, EstadoSocio.INACTIVO, EstadoSocio.MOROSO);
    }

    @Test @DisplayName("EstadoSocio valueOf funciona")
    void enumValueOf() {
        assertThat(EstadoSocio.valueOf("ACTIVO")).isEqualTo(EstadoSocio.ACTIVO);
        assertThat(EstadoSocio.valueOf("MOROSO")).isEqualTo(EstadoSocio.MOROSO);
    }
}
