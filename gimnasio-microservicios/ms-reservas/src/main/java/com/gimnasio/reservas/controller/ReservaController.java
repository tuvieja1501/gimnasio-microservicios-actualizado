package com.gimnasio.reservas.controller;

import com.gimnasio.reservas.dto.ReservaDTO;
import com.gimnasio.reservas.model.Reserva;
import com.gimnasio.reservas.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * El microservicio mas "orquestador" del ecosistema: para confirmar una
 * reserva consulta a ms-socios, ms-membresias y ms-clases via Feign, y si
 * el decremento de cupo remoto falla, hace rollback (borra la reserva ya
 * guardada) para no dejar datos inconsistentes entre servicios.
 */
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Reserva de cupo en una clase; orquesta llamadas a ms-socios, ms-membresias y ms-clases")
public class ReservaController {

    private static final Logger log = LoggerFactory.getLogger(ReservaController.class);

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las reservas")
    public ResponseEntity<List<Reserva>> listar() {
        log.info("GET /api/reservas");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por id")
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content)
    public ResponseEntity<Reserva> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/socio/{socioId}")
    @Operation(summary = "Listar reservas de un socio")
    public ResponseEntity<List<Reserva>> porSocio(@PathVariable Long socioId) {
        return ResponseEntity.ok(service.listarPorSocio(socioId));
    }

    @GetMapping("/clase/{claseId}")
    @Operation(summary = "Listar reservas de una clase")
    public ResponseEntity<List<Reserva>> porClase(@PathVariable Long claseId) {
        return ResponseEntity.ok(service.listarPorClase(claseId));
    }

    @PostMapping
    @Operation(summary = "Reservar cupo en una clase",
            description = "Valida (via Feign): socio ACTIVO en ms-socios, membresia vigente en ms-membresias, "
                    + "clase futura con cupo en ms-clases. Si el decremento remoto de cupo falla tras guardar la "
                    + "reserva, se hace rollback automatico borrandola.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva confirmada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"id\":1,\"socioId\":10,\"claseId\":20,\"estado\":\"CONFIRMADA\"}"))),
            @ApiResponse(responseCode = "404", description = "Socio o clase no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "Socio inactivo, sin membresia vigente, clase pasada, sin cupo, o ya reservada", content = @Content)
    })
    public ResponseEntity<Reserva> reservar(@Valid @RequestBody ReservaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reservar(dto));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una reserva CONFIRMADA", description = "Restaura el cupo en ms-clases via Feign.")
    @ApiResponse(responseCode = "409", description = "La reserva no estaba CONFIRMADA", content = @Content)
    public ResponseEntity<Reserva> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PatchMapping("/{id}/asistida")
    @Operation(summary = "Marcar una reserva como asistida (el socio se presento a la clase)")
    public ResponseEntity<Reserva> marcarAsistida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarAsistida(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una reserva")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
