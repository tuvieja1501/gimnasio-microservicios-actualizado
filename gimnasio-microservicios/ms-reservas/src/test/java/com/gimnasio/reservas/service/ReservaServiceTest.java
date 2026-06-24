package com.gimnasio.reservas.service;

import com.gimnasio.reservas.client.ClaseClient;
import com.gimnasio.reservas.client.MembresiaClient;
import com.gimnasio.reservas.client.SocioClient;
import com.gimnasio.reservas.dto.ClaseRespuesta;
import com.gimnasio.reservas.dto.MembresiaVigenteRespuesta;
import com.gimnasio.reservas.dto.ReservaDTO;
import com.gimnasio.reservas.dto.SocioRespuesta;
import com.gimnasio.reservas.exception.ReglaNegocioException;
import com.gimnasio.reservas.model.EstadoReserva;
import com.gimnasio.reservas.model.Reserva;
import com.gimnasio.reservas.repository.ReservaRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService - pruebas unitarias")
class ReservaServiceTest {

    @Mock
    private ReservaRepository repo;
    @Mock
    private SocioClient socioClient;
    @Mock
    private ClaseClient claseClient;
    @Mock
    private MembresiaClient membresiaClient;

    @InjectMocks
    private ReservaService service;

    private ReservaDTO dto;
    private SocioRespuesta socioActivo;
    private ClaseRespuesta claseConCupo;
    private MembresiaVigenteRespuesta vigente;

    @BeforeEach
    void setUp() {
        dto = new ReservaDTO();
        dto.setSocioId(10L);
        dto.setClaseId(20L);

        socioActivo = new SocioRespuesta();
        socioActivo.setId(10L);
        socioActivo.setEstado("ACTIVO");

        claseConCupo = new ClaseRespuesta();
        claseConCupo.setId(20L);
        claseConCupo.setFechaHora(LocalDateTime.now().plusDays(1));
        claseConCupo.setCupoMaximo(20);
        claseConCupo.setCuposDisponibles(5);

        vigente = new MembresiaVigenteRespuesta();
        vigente.setVigente(true);
    }

    @Nested
    @DisplayName("reservar()")
    class Reservar {

        @Test
        @DisplayName("camino feliz: confirma reserva y decrementa cupo remoto")
        void reservaConDatosValidos() {
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
            when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);
            when(claseClient.obtenerClase(20L)).thenReturn(claseConCupo);
            when(repo.findBySocioIdAndClaseId(10L, 20L)).thenReturn(Optional.empty());
            when(repo.save(any(Reserva.class))).thenAnswer(inv -> {
                Reserva r = inv.getArgument(0);
                r.setId(1L);
                return r;
            });

            Reserva resultado = service.reservar(dto);

            assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
            verify(claseClient).decrementarCupo(20L);
            verify(repo, never()).delete(any());
        }

        @Test
        @DisplayName("rechaza si el socio no esta ACTIVO")
        void rechazaSocioInactivo() {
            socioActivo.setEstado("MOROSO");
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);

            assertThatThrownBy(() -> service.reservar(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ACTIVO");

            verifyNoInteractions(claseClient, membresiaClient);
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("rechaza si el socio no tiene membresia vigente")
        void rechazaSinMembresiaVigente() {
            vigente.setVigente(false);
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
            when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);

            assertThatThrownBy(() -> service.reservar(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("membresia vigente");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("rechaza si la clase ya ocurrio")
        void rechazaClasePasada() {
            claseConCupo.setFechaHora(LocalDateTime.now().minusHours(1));
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
            when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);
            when(claseClient.obtenerClase(20L)).thenReturn(claseConCupo);

            assertThatThrownBy(() -> service.reservar(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ya ocurrio");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("rechaza si no quedan cupos disponibles")
        void rechazaSinCupos() {
            claseConCupo.setCuposDisponibles(0);
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
            when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);
            when(claseClient.obtenerClase(20L)).thenReturn(claseConCupo);

            assertThatThrownBy(() -> service.reservar(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("cupos");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("rechaza si el socio ya reservo esa clase")
        void rechazaReservaDuplicada() {
            Reserva existente = new Reserva();
            existente.setId(99L);
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
            when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);
            when(claseClient.obtenerClase(20L)).thenReturn(claseConCupo);
            when(repo.findBySocioIdAndClaseId(10L, 20L)).thenReturn(Optional.of(existente));

            assertThatThrownBy(() -> service.reservar(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("99");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("hace rollback (borra la reserva) si falla el decremento de cupo remoto")
        void hacenRollbackSiFallaDecremento() {
            when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
            when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);
            when(claseClient.obtenerClase(20L)).thenReturn(claseConCupo);
            when(repo.findBySocioIdAndClaseId(10L, 20L)).thenReturn(Optional.empty());

            Reserva guardada = new Reserva();
            guardada.setId(1L);
            when(repo.save(any(Reserva.class))).thenReturn(guardada);
            doThrow(mock(FeignException.class)).when(claseClient).decrementarCupo(20L);

            assertThatThrownBy(() -> service.reservar(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("cupo");

            ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
            verify(repo).delete(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(1L);
        }
    }

    @Test
    @DisplayName("cancelar() marca CANCELADA y restaura el cupo remoto")
    void cancelaReservaConfirmada() {
        Reserva confirmada = reservaGuardada(1L, EstadoReserva.CONFIRMADA);
        when(repo.findById(1L)).thenReturn(Optional.of(confirmada));
        when(repo.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva resultado = service.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(claseClient).incrementarCupo(20L);
    }

    @Test
    @DisplayName("cancelar() rechaza si la reserva no esta CONFIRMADA")
    void rechazaCancelarNoConfirmada() {
        Reserva asistida = reservaGuardada(1L, EstadoReserva.ASISTIDA);
        when(repo.findById(1L)).thenReturn(Optional.of(asistida));

        assertThatThrownBy(() -> service.cancelar(1L))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("marcarAsistida() cambia el estado a ASISTIDA")
    void marcaAsistidaCorrectamente() {
        Reserva confirmada = reservaGuardada(1L, EstadoReserva.CONFIRMADA);
        when(repo.findById(1L)).thenReturn(Optional.of(confirmada));
        when(repo.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva resultado = service.marcarAsistida(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.ASISTIDA);
    }

    @Test
    @DisplayName("traduce un 404 de ms-socios en RecursoNoEncontradoException")
    void socioNoEncontradoViaFeign() {
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET, "/api/socios/10",
                java.util.Collections.emptyMap(), null,
                java.nio.charset.Charset.defaultCharset(), null);
        feign.Response response = feign.Response.builder()
                .status(404).reason("Not Found").request(request)
                .headers(java.util.Collections.emptyMap()).build();
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "SocioClient#obtenerSocio(Long)", response);
        when(socioClient.obtenerSocio(10L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.reservar(dto))
                .isInstanceOf(com.gimnasio.reservas.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un 404 de ms-clases en RecursoNoEncontradoException")
    void claseNoEncontradaViaFeign() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(membresiaClient.tieneVigente(10L)).thenReturn(vigente);
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET, "/api/clases/20",
                java.util.Collections.emptyMap(), null,
                java.nio.charset.Charset.defaultCharset(), null);
        feign.Response response = feign.Response.builder()
                .status(404).reason("Not Found").request(request)
                .headers(java.util.Collections.emptyMap()).build();
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "ClaseClient#obtenerClase(Long)", response);
        when(claseClient.obtenerClase(20L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.reservar(dto))
                .isInstanceOf(com.gimnasio.reservas.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un error generico de ms-membresias en ReglaNegocioException")
    void errorGenericoConsultandoMembresia() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(membresiaClient.tieneVigente(10L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> service.reservar(dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("listar() retorna todas las reservas")
    void listarRetornaTodas() {
        when(repo.findAll()).thenReturn(java.util.List.of(
                reservaGuardada(1L, EstadoReserva.CONFIRMADA), reservaGuardada(2L, EstadoReserva.ASISTIDA)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarPorSocio() delega en el repositorio")
    void listarPorSocioDelegaEnRepo() {
        when(repo.findBySocioId(10L)).thenReturn(java.util.List.of(reservaGuardada(1L, EstadoReserva.CONFIRMADA)));

        assertThat(service.listarPorSocio(10L)).hasSize(1);
        verify(repo).findBySocioId(10L);
    }

    @Test
    @DisplayName("listarPorClase() delega en el repositorio")
    void listarPorClaseDelegaEnRepo() {
        when(repo.findByClaseId(20L)).thenReturn(java.util.List.of(reservaGuardada(1L, EstadoReserva.CONFIRMADA)));

        assertThat(service.listarPorClase(20L)).hasSize(1);
        verify(repo).findByClaseId(20L);
    }

    @Test
    @DisplayName("buscarPorId() retorna la reserva cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(Optional.of(reservaGuardada(1L, EstadoReserva.CONFIRMADA)));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(com.gimnasio.reservas.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("eliminar() borra la reserva cuando existe")
    void eliminarExistente() {
        Reserva r = reservaGuardada(1L, EstadoReserva.CANCELADA);
        when(repo.findById(1L)).thenReturn(Optional.of(r));

        service.eliminar(1L);

        verify(repo).delete(r);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si la reserva no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(com.gimnasio.reservas.exception.RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("marcarAsistida() rechaza si la reserva no esta CONFIRMADA")
    void marcarAsistidaRechazaSiNoConfirmada() {
        Reserva cancelada = reservaGuardada(1L, EstadoReserva.CANCELADA);
        when(repo.findById(1L)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> service.marcarAsistida(1L))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("cancelar() no falla si ms-clases no responde al restaurar el cupo")
    void cancelarToleraFalloAlRestaurarCupo() {
        Reserva confirmada = reservaGuardada(1L, EstadoReserva.CONFIRMADA);
        when(repo.findById(1L)).thenReturn(Optional.of(confirmada));
        when(repo.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(mock(FeignException.class)).when(claseClient).incrementarCupo(20L);

        Reserva resultado = service.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    private Reserva reservaGuardada(Long id, EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setSocioId(10L);
        r.setClaseId(20L);
        r.setFechaReserva(LocalDateTime.now().minusHours(2));
        r.setEstado(estado);
        return r;
    }
}
