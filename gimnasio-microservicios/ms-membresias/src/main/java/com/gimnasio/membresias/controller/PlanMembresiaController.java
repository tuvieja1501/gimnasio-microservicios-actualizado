package com.gimnasio.membresias.controller;

import com.gimnasio.membresias.dto.PlanMembresiaDTO;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.service.PlanMembresiaService;
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
@RequestMapping("/api/planes")
@Tag(name = "Planes de Membresia", description = "Catalogo de planes que el gimnasio ofrece (mensual, trimestral, anual, etc.)")
public class PlanMembresiaController {

    private static final Logger log = LoggerFactory.getLogger(PlanMembresiaController.class);

    private final PlanMembresiaService service;

    public PlanMembresiaController(PlanMembresiaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los planes")
    public ResponseEntity<List<PlanMembresia>> listar() {
        log.info("GET /api/planes");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar solo los planes activos (disponibles para venta)")
    public ResponseEntity<List<PlanMembresia>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener plan por id")
    @ApiResponse(responseCode = "404", description = "Plan no encontrado", content = @Content)
    public ResponseEntity<PlanMembresia> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo plan de membresia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plan creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (duracion 1-36 meses, precio > 0)", content = @Content)
    })
    public ResponseEntity<PlanMembresia> crear(@Valid @RequestBody PlanMembresiaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un plan existente")
    public ResponseEntity<PlanMembresia> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody PlanMembresiaDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un plan")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
