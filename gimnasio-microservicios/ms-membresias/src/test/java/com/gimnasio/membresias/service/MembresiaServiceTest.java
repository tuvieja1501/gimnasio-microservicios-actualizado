package com.gimnasio.membresias.service;

import com.gimnasio.membresias.client.SocioClient;
import com.gimnasio.membresias.dto.MembresiaDTO;
import com.gimnasio.membresias.dto.SocioRespuesta;
import com.gimnasio.membresias.exception.RecursoNoEncontradoException;
import com.gimnasio.membresias.exception.ReglaNegocioException;
import com.gimnasio.membresias.model.EstadoMembresia;
import com.gimnasio.membresias.model.Membresia;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.repository.MembresiaRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de MembresiaService. Se mockean tanto el repositorio JPA como el
 * SocioClient (Feign) y el PlanMembresiaService, para aislar completamente
 * la logica de negocio de la comunicacion real con ms-socios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MembresiaService - pruebas unitarias")
class MembresiaServiceTest {

    @Mock
    private MembresiaRepository repo;

    @Mock
    private PlanMembresiaService planService;

    @Mock
    private SocioClient socioClient;

    @InjectMocks
    private MembresiaService service;

    private MembresiaDTO dto;
    private SocioRespuesta socioActivo;
    private PlanMembresia planActivo;

    @BeforeEach
    void setUp() {
        dto = new MembresiaDTO();
        dto.setSocioId(10L);
        dto.setPlanId(1L);
        dto.setFechaInicio(LocalDate.now());

        socioActivo = new SocioRespuesta();
        socioActivo.setId(10L);
        socioActivo.setRut("12345678-9");
        socioActivo.setEstado("ACTIVO");

        planActivo = new PlanMembresia();
        planActivo.setId(1L);
        planActivo.setNombre("Mensual Basico");
        planActivo.setDuracionMeses(1);
        planActivo.setPrecio(new BigDecimal("19990"));
        planActivo.setActivo(true);
    }

    @Test
    @DisplayName("crea membresia VIGENTE cuando socio activo, sin membresia previa y plan activo")
    void creaMembresiaConDatosValidos() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(repo.existsBySocioIdAndEstado(10L, EstadoMembresia.VIGENTE)).thenReturn(false);
        when(planService.buscarPorId(1L)).thenReturn(planActivo);
        when(repo.save(any(Membresia.class))).thenAnswer(inv -> {
            Membresia m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });

        Membresia resultado = service.crear(dto);

        assertThat(resultado.getEstado()).isEqualTo(EstadoMembresia.VIGENTE);
        assertThat(resultado.getFechaFin()).isEqualTo(dto.getFechaInicio().plusMonths(1));
    }

    @Test
    @DisplayName("rechaza si el socio remoto no esta ACTIVO")
    void rechazaSocioInactivo() {
        socioActivo.setEstado("MOROSO");
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("MOROSO");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza si el socio ya tiene una membresia VIGENTE")
    void rechazaMembresiaDuplicada() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(repo.existsBySocioIdAndEstado(10L, EstadoMembresia.VIGENTE)).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("VIGENTE");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza si el plan seleccionado no esta activo")
    void rechazaPlanInactivo() {
        planActivo.setActivo(false);
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(repo.existsBySocioIdAndEstado(10L, EstadoMembresia.VIGENTE)).thenReturn(false);
        when(planService.buscarPorId(1L)).thenReturn(planActivo);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("activo");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("traduce 404 de ms-socios a RecursoNoEncontradoException")
    void traduce404DeFeignASocioNoEncontrado() {
        when(socioClient.obtenerSocio(10L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un error generico de Feign a ReglaNegocioException")
    void traduceErrorGenericoDeFeign() {
        when(socioClient.obtenerSocio(10L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("cancelar() cambia el estado a CANCELADA si estaba VIGENTE")
    void cancelaMembresiaVigente() {
        Membresia vigente = membresiaGuardada(1L, EstadoMembresia.VIGENTE);
        when(repo.findById(1L)).thenReturn(Optional.of(vigente));
        when(repo.save(any(Membresia.class))).thenAnswer(inv -> inv.getArgument(0));

        Membresia resultado = service.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoMembresia.CANCELADA);
    }

    @Test
    @DisplayName("cancelar() rechaza si la membresia no esta VIGENTE")
    void rechazaCancelarMembresiaNoVigente() {
        Membresia cancelada = membresiaGuardada(1L, EstadoMembresia.CANCELADA);
        when(repo.findById(1L)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> service.cancelar(1L))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("tieneVigente() retorna true si existe membresia vigente y no vencida")
    void tieneVigenteRetornaTrue() {
        Membresia vigente = membresiaGuardada(1L, EstadoMembresia.VIGENTE);
        vigente.setFechaFin(LocalDate.now().plusDays(5));
        when(repo.findFirstBySocioIdAndEstadoOrderByFechaFinDesc(10L, EstadoMembresia.VIGENTE))
                .thenReturn(Optional.of(vigente));

        assertThat(service.tieneVigente(10L)).isTrue();
    }

    @Test
    @DisplayName("tieneVigente() retorna false si no hay ninguna")
    void tieneVigenteRetornaFalseSinRegistro() {
        when(repo.findFirstBySocioIdAndEstadoOrderByFechaFinDesc(10L, EstadoMembresia.VIGENTE))
                .thenReturn(Optional.empty());

        assertThat(service.tieneVigente(10L)).isFalse();
    }

    @Test
    @DisplayName("listar() retorna todas las membresias")
    void listarRetornaTodas() {
        when(repo.findAll()).thenReturn(java.util.List.of(
                membresiaGuardada(1L, EstadoMembresia.VIGENTE),
                membresiaGuardada(2L, EstadoMembresia.CANCELADA)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("buscarPorId() retorna la membresia cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(Optional.of(membresiaGuardada(1L, EstadoMembresia.VIGENTE)));

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
    @DisplayName("listarPorSocio() delega en el repositorio")
    void listarPorSocioDelegaEnRepo() {
        when(repo.findBySocioId(10L)).thenReturn(java.util.List.of(membresiaGuardada(1L, EstadoMembresia.VIGENTE)));

        assertThat(service.listarPorSocio(10L)).hasSize(1);
        verify(repo).findBySocioId(10L);
    }

    @Test
    @DisplayName("eliminar() borra la membresia cuando existe")
    void eliminarExistente() {
        Membresia m = membresiaGuardada(1L, EstadoMembresia.CANCELADA);
        when(repo.findById(1L)).thenReturn(Optional.of(m));

        service.eliminar(1L);

        verify(repo).delete(m);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si la membresia no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    private Membresia membresiaGuardada(Long id, EstadoMembresia estado) {
        Membresia m = new Membresia();
        m.setId(id);
        m.setSocioId(10L);
        m.setPlan(planActivo);
        m.setFechaInicio(LocalDate.now().minusDays(10));
        m.setFechaFin(LocalDate.now().plusMonths(1));
        m.setEstado(estado);
        return m;
    }
}
