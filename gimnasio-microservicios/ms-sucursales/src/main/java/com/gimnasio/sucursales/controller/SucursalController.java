package com.gimnasio.sucursales.controller;

import com.gimnasio.sucursales.dto.SucursalDTO;
import com.gimnasio.sucursales.model.Sucursal;
import com.gimnasio.sucursales.service.SucursalService;
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
@RequestMapping("/api/sucursales")
@Tag(name = "Sucursales", description = "Sedes fisicas del gimnasio; consumido por ms-clases y ms-equipos via Feign")
public class SucursalController {

    private static final Logger log = LoggerFactory.getLogger(SucursalController.class);

    private final SucursalService service;

    public SucursalController(SucursalService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las sucursales")
    public ResponseEntity<List<Sucursal>> listar() {
        log.info("GET /api/sucursales");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar solo sucursales activas")
    public ResponseEntity<List<Sucursal>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    @GetMapping("/comuna/{comuna}")
    @Operation(summary = "Buscar sucursales por comuna", description = "Busqueda case-insensitive")
    public ResponseEntity<List<Sucursal>> porComuna(@PathVariable String comuna) {
        return ResponseEntity.ok(service.listarPorComuna(comuna));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sucursal por id",
            description = "Tambien es consumido remotamente por ms-clases y ms-equipos via Feign para validar estado/capacidad.")
    @ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    public ResponseEntity<Sucursal> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva sucursal",
            description = "Valida nombre unico y que la hora de apertura sea anterior a la de cierre.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sucursal creada"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado u horario invalido", content = @Content)
    })
    public ResponseEntity<Sucursal> crear(@Valid @RequestBody SucursalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una sucursal existente")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Long id, @Valid @RequestBody SucursalDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar una sucursal")
    public ResponseEntity<Sucursal> cambiarEstado(@PathVariable Long id, @RequestParam Boolean activa) {
        return ResponseEntity.ok(service.cambiarEstado(id, activa));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una sucursal")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
