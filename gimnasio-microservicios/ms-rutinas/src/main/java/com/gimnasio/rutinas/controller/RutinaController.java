package com.gimnasio.rutinas.controller;

import com.gimnasio.rutinas.dto.RutinaDTO;
import com.gimnasio.rutinas.model.Rutina;
import com.gimnasio.rutinas.service.RutinaService;
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
@RequestMapping("/api/rutinas")
@Tag(name = "Rutinas", description = "Rutinas de entrenamiento con ejercicios anidados; valida socio e instructor remotos via Feign")
public class RutinaController {

    private static final Logger log = LoggerFactory.getLogger(RutinaController.class);

    private final RutinaService service;

    public RutinaController(RutinaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las rutinas")
    public ResponseEntity<List<Rutina>> listar() {
        log.info("GET /api/rutinas");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rutina por id (incluye sus ejercicios)")
    @ApiResponse(responseCode = "404", description = "Rutina no encontrada", content = @Content)
    public ResponseEntity<Rutina> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/socio/{socioId}")
    @Operation(summary = "Listar rutinas de un socio")
    public ResponseEntity<List<Rutina>> porSocio(@PathVariable Long socioId) {
        return ResponseEntity.ok(service.listarPorSocio(socioId));
    }

    @PostMapping
    @Operation(summary = "Crear una rutina con sus ejercicios",
            description = "Valida via Feign que el socio este ACTIVO en ms-socios y el instructor activo en ms-instructores. "
                    + "Los ejercicios se guardan en cascada junto con la rutina (cascade=ALL).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rutina creada con sus ejercicios"),
            @ApiResponse(responseCode = "404", description = "Socio o instructor no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "Socio o instructor inactivos", content = @Content)
    })
    public ResponseEntity<Rutina> crear(@Valid @RequestBody RutinaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una rutina", description = "Reemplaza completamente la lista de ejercicios (orphanRemoval=true).")
    public ResponseEntity<Rutina> actualizar(@PathVariable Long id, @Valid @RequestBody RutinaDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una rutina (sus ejercicios se eliminan en cascada)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
