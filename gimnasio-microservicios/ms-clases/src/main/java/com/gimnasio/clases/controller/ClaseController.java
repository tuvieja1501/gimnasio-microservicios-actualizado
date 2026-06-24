package com.gimnasio.clases.controller;

import com.gimnasio.clases.dto.ClaseDTO;
import com.gimnasio.clases.model.Clase;
import com.gimnasio.clases.service.ClaseService;
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
@RequestMapping("/api/clases")
@Tag(name = "Clases", description = "Clases agendadas; valida instructor y sucursal remotos via Feign, gestiona cupos")
public class ClaseController {

    private static final Logger log = LoggerFactory.getLogger(ClaseController.class);

    private final ClaseService service;

    public ClaseController(ClaseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las clases")
    public ResponseEntity<List<Clase>> listar() {
        log.info("GET /api/clases");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/futuras")
    @Operation(summary = "Listar solo clases con fecha futura")
    public ResponseEntity<List<Clase>> listarFuturas() {
        return ResponseEntity.ok(service.listarFuturas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener clase por id")
    @ApiResponse(responseCode = "404", description = "Clase no encontrada", content = @Content)
    public ResponseEntity<Clase> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva clase",
            description = "Valida via Feign que el instructor este activo y la sucursal activa, y que el cupo no supere la capacidad de la sucursal.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Clase creada"),
            @ApiResponse(responseCode = "404", description = "Instructor o sucursal no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "Instructor/sucursal inactivos o cupo excede capacidad", content = @Content)
    })
    public ResponseEntity<Clase> crear(@Valid @RequestBody ClaseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una clase existente")
    public ResponseEntity<Clase> actualizar(@PathVariable Long id, @Valid @RequestBody ClaseDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/decrementar-cupo")
    @Operation(summary = "Decrementar 1 cupo disponible",
            description = "Endpoint interno consumido por ms-reservas via Feign al confirmar una reserva.")
    @ApiResponse(responseCode = "409", description = "No quedan cupos disponibles", content = @Content)
    public ResponseEntity<Clase> decrementarCupo(@PathVariable Long id) {
        return ResponseEntity.ok(service.decrementarCupo(id));
    }

    @PatchMapping("/{id}/incrementar-cupo")
    @Operation(summary = "Restaurar 1 cupo disponible",
            description = "Endpoint interno consumido por ms-reservas via Feign al cancelar una reserva.")
    public ResponseEntity<Clase> incrementarCupo(@PathVariable Long id) {
        return ResponseEntity.ok(service.incrementarCupo(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una clase")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
