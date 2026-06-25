package com.gimnasio.sucursales.model;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;

class SucursalModelTest {
    @Test void gettersSetters() {
        Sucursal s = new Sucursal();
        s.setId(1L); s.setNombre("Sede Test"); s.setDireccion("Dir 1"); s.setComuna("Providencia");
        s.setTelefono("+562111"); s.setCapacidad(80);
        s.setHoraApertura(LocalTime.of(8,0)); s.setHoraCierre(LocalTime.of(21,0)); s.setActiva(true);
        assertThat(s.getId()).isEqualTo(1L); assertThat(s.getNombre()).isEqualTo("Sede Test");
        assertThat(s.getComuna()).isEqualTo("Providencia"); assertThat(s.getCapacidad()).isEqualTo(80);
        assertThat(s.getActiva()).isTrue(); assertThat(s.getTelefono()).isEqualTo("+562111");
        assertThat(s.getDireccion()).isEqualTo("Dir 1");
        assertThat(s.getHoraApertura()).isEqualTo(LocalTime.of(8,0));
    }
}
