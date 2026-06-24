package com.gimnasio.pagos.service;

import com.gimnasio.pagos.client.SocioClient;
import com.gimnasio.pagos.dto.PagoDTO;
import com.gimnasio.pagos.dto.SocioRespuesta;
import com.gimnasio.pagos.exception.RecursoNoEncontradoException;
import com.gimnasio.pagos.exception.ReglaNegocioException;
import com.gimnasio.pagos.model.EstadoPago;
import com.gimnasio.pagos.model.MetodoPago;
import com.gimnasio.pagos.model.Pago;
import com.gimnasio.pagos.repository.PagoRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - pruebas unitarias")
class PagoServiceTest {

    @Mock
    private PagoRepository repo;

    @Mock
    private SocioClient socioClient;

    @InjectMocks
    private PagoService service;

    private PagoDTO dto;
    private SocioRespuesta socio;

    @BeforeEach
    void setUp() {
        dto = new PagoDTO();
        dto.setSocioId(10L);
        dto.setMembresiaId(5L);
        dto.setMonto(new BigDecimal("19990"));
        dto.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        dto.setReferencia("REF-001");

        socio = new SocioRespuesta();
        socio.setId(10L);
        socio.setNombre("Juan");
        socio.setApellido("Perez");
    }

    @Test
    @DisplayName("registra el pago como PAGADO cuando el socio existe")
    void registraPagoConSocioValido() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socio);
        when(repo.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        Pago resultado = service.registrar(dto);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.PAGADO);
        assertThat(resultado.getMonto()).isEqualTo(new BigDecimal("19990"));
    }

    @Test
    @DisplayName("traduce 404 de ms-socios a RecursoNoEncontradoException")
    void rechazaSocioInexistente() {
        when(socioClient.obtenerSocio(10L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.registrar(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("traduce error generico de Feign a ReglaNegocioException")
    void traduceErrorGenericoDeFeign() {
        when(socioClient.obtenerSocio(10L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> service.registrar(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("anular() cambia el estado a ANULADO")
    void anulaPagoPagado() {
        Pago pagado = pagoGuardado(1L, EstadoPago.PAGADO);
        when(repo.findById(1L)).thenReturn(Optional.of(pagado));
        when(repo.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago resultado = service.anular(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.ANULADO);
    }

    @Test
    @DisplayName("anular() rechaza si el pago ya estaba anulado")
    void rechazaAnularPagoYaAnulado() {
        Pago anulado = pagoGuardado(1L, EstadoPago.ANULADO);
        when(repo.findById(1L)).thenReturn(Optional.of(anulado));

        assertThatThrownBy(() -> service.anular(1L))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("listar() retorna todos los pagos")
    void listarRetornaTodos() {
        when(repo.findAll()).thenReturn(java.util.List.of(
                pagoGuardado(1L, EstadoPago.PAGADO), pagoGuardado(2L, EstadoPago.ANULADO)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarPorSocio() delega en el repositorio")
    void listarPorSocioDelegaEnRepo() {
        when(repo.findBySocioId(10L)).thenReturn(java.util.List.of(pagoGuardado(1L, EstadoPago.PAGADO)));

        assertThat(service.listarPorSocio(10L)).hasSize(1);
        verify(repo).findBySocioId(10L);
    }

    @Test
    @DisplayName("listarPorMembresia() delega en el repositorio")
    void listarPorMembresiaDelegaEnRepo() {
        when(repo.findByMembresiaId(5L)).thenReturn(java.util.List.of(pagoGuardado(1L, EstadoPago.PAGADO)));

        assertThat(service.listarPorMembresia(5L)).hasSize(1);
        verify(repo).findByMembresiaId(5L);
    }

    @Test
    @DisplayName("buscarPorId() retorna el pago cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(Optional.of(pagoGuardado(1L, EstadoPago.PAGADO)));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("anular() lanza excepcion si el pago no existe")
    void anularPagoInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.anular(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() borra el pago cuando existe")
    void eliminarExistente() {
        Pago p = pagoGuardado(1L, EstadoPago.ANULADO);
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        service.eliminar(1L);

        verify(repo).delete(p);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si el pago no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    private Pago pagoGuardado(Long id, EstadoPago estado) {
        Pago p = new Pago();
        p.setId(id);
        p.setSocioId(10L);
        p.setMembresiaId(5L);
        p.setMonto(new BigDecimal("19990"));
        p.setMetodoPago(MetodoPago.EFECTIVO);
        p.setFechaPago(LocalDateTime.now().minusDays(1));
        p.setEstado(estado);
        return p;
    }
}
