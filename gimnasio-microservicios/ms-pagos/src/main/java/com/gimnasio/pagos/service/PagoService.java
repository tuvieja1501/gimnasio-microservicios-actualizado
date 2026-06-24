package com.gimnasio.pagos.service;

import com.gimnasio.pagos.client.SocioClient;
import com.gimnasio.pagos.dto.PagoDTO;
import com.gimnasio.pagos.dto.SocioRespuesta;
import com.gimnasio.pagos.exception.RecursoNoEncontradoException;
import com.gimnasio.pagos.exception.ReglaNegocioException;
import com.gimnasio.pagos.model.EstadoPago;
import com.gimnasio.pagos.model.Pago;
import com.gimnasio.pagos.repository.PagoRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository repo;
    private final SocioClient socioClient;

    public PagoService(PagoRepository repo, SocioClient socioClient) {
        this.repo = repo;
        this.socioClient = socioClient;
    }

    public List<Pago> listar() { return repo.findAll(); }

    public List<Pago> listarPorSocio(Long socioId) { return repo.findBySocioId(socioId); }

    public List<Pago> listarPorMembresia(Long membresiaId) { return repo.findByMembresiaId(membresiaId); }

    public Pago buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pago con id " + id + " no existe"));
    }

    public Pago registrar(PagoDTO dto) {
        log.info("Registrando pago socio={} monto={}", dto.getSocioId(), dto.getMonto());

        // Validar socio remoto
        SocioRespuesta socio;
        try {
            socio = socioClient.obtenerSocio(dto.getSocioId());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Socio con id " + dto.getSocioId() + " no existe");
        } catch (FeignException e) {
            log.error("Error consultando ms-socios: {}", e.getMessage());
            throw new ReglaNegocioException("No se pudo comunicar con ms-socios");
        }

        log.info("Socio validado: {} {}", socio.getNombre(), socio.getApellido());

        Pago p = new Pago();
        p.setSocioId(dto.getSocioId());
        p.setMembresiaId(dto.getMembresiaId());
        p.setMonto(dto.getMonto());
        p.setMetodoPago(dto.getMetodoPago());
        p.setReferencia(dto.getReferencia());
        p.setFechaPago(LocalDateTime.now());
        p.setEstado(EstadoPago.PAGADO);

        Pago guardado = repo.save(p);
        log.info("Pago registrado id={}", guardado.getId());
        return guardado;
    }

    public Pago anular(Long id) {
        log.info("Anulando pago id={}", id);
        Pago p = buscarPorId(id);
        if (p.getEstado() == EstadoPago.ANULADO) {
            throw new ReglaNegocioException("El pago ya esta anulado");
        }
        p.setEstado(EstadoPago.ANULADO);
        return repo.save(p);
    }

    public void eliminar(Long id) {
        Pago p = buscarPorId(id);
        repo.delete(p);
    }
}
