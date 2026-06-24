package com.gimnasio.equipos.controller;

import com.gimnasio.equipos.dto.EquipoDTO;
import com.gimnasio.equipos.model.Equipo;
import com.gimnasio.equipos.model.EstadoEquipo;
import com.gimnasio.equipos.service.EquipoService;
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
@RequestMapping("/api/equipos")
@Tag(name = "Equipos", description = "Inventario de equipos de gimnasio por sucursal; valida sucursal remota via Feign")
public class EquipoController {

    private static final Logger log = LoggerFactory.getLogger(EquipoController.class);

    private final EquipoService service;

    public EquipoController(EquipoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los equipos")
    public ResponseEntity<List<Equipo>> listar() {
        log.info("GET /api/equipos");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener equipo por id")
    @ApiResponse(responseCode = "404", description = "Equipo no encontrado", content = @Content)
    public ResponseEntity<Equipo> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/sucursal/{sucursalId}")
    @Operation(summary = "Listar equipos de una sucursal")
    public ResponseEntity<List<Equipo>> porSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(service.listarPorSucursal(sucursalId));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo equipo",
            description = "Valida via Feign que la sucursal este activa y que el codigo interno sea unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo creado"),
            @ApiResponse(responseCode = "409", description = "Codigo interno duplicado o sucursal inactiva", content = @Content)
    })
    public ResponseEntity<Equipo> crear(@Valid @RequestBody EquipoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un equipo existente")
    public ResponseEntity<Equipo> actualizar(@PathVariable Long id, @Valid @RequestBody EquipoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del equipo", description = "OPERATIVO, EN_MANTENIMIENTO o DADO_DE_BAJA")
    public ResponseEntity<Equipo> cambiarEstado(@PathVariable Long id, @RequestParam EstadoEquipo estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un equipo")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
