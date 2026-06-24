package com.gimnasio.pagos.controller;

import com.gimnasio.pagos.dto.PagoDTO;
import com.gimnasio.pagos.model.Pago;
import com.gimnasio.pagos.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Registro de pagos de socios; valida existencia del socio en ms-socios via Feign")
public class PagoController {

    private static final Logger log = LoggerFactory.getLogger(PagoController.class);

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los pagos")
    public ResponseEntity<List<Pago>> listar() {
        log.info("GET /api/pagos");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por id")
    @ApiResponse(responseCode = "404", description = "Pago no encontrado", content = @Content)
    public ResponseEntity<Pago> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/socio/{socioId}")
    @Operation(summary = "Listar pagos de un socio")
    public ResponseEntity<List<Pago>> porSocio(@PathVariable Long socioId) {
        return ResponseEntity.ok(service.listarPorSocio(socioId));
    }

    @GetMapping("/membresia/{membresiaId}")
    @Operation(summary = "Listar pagos asociados a una membresia")
    public ResponseEntity<List<Pago>> porMembresia(@PathVariable Long membresiaId) {
        return ResponseEntity.ok(service.listarPorMembresia(membresiaId));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo pago",
            description = "Valida via Feign que el socio exista en ms-socios antes de registrar el pago como PAGADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado"),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado en ms-socios", content = @Content)
    })
    public ResponseEntity<Pago> registrar(@Valid @RequestBody PagoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular un pago")
    @ApiResponse(responseCode = "409", description = "El pago ya estaba anulado", content = @Content)
    public ResponseEntity<Pago> anular(@PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pago")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
