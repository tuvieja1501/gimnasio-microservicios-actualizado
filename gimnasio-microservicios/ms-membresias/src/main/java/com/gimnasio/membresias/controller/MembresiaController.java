package com.gimnasio.membresias.controller;

import com.gimnasio.membresias.dto.MembresiaDTO;
import com.gimnasio.membresias.model.Membresia;
import com.gimnasio.membresias.service.MembresiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Map;

/**
 * Logica de membresias. Consume ms-socios via Feign para validar
 * la existencia y estado del socio antes de crear una membresia.
 */
@RestController
@RequestMapping("/api/membresias")
@Tag(name = "Membresias", description = "Asociacion entre un socio y un plan; valida estado del socio remoto via Feign")
public class MembresiaController {

    private static final Logger log = LoggerFactory.getLogger(MembresiaController.class);

    private final MembresiaService service;

    public MembresiaController(MembresiaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las membresias")
    public ResponseEntity<List<Membresia>> listar() {
        log.info("GET /api/membresias");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener membresia por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membresia encontrada"),
            @ApiResponse(responseCode = "404", description = "Membresia no encontrada", content = @Content)
    })
    public ResponseEntity<Membresia> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/socio/{socioId}")
    @Operation(summary = "Listar membresias de un socio")
    public ResponseEntity<List<Membresia>> porSocio(@PathVariable Long socioId) {
        return ResponseEntity.ok(service.listarPorSocio(socioId));
    }

    /**
     * Endpoint interno consumido por ms-reservas para saber si el socio
     * tiene membresia vigente antes de reservar una clase.
     */
    @GetMapping("/socio/{socioId}/vigente")
    @Operation(summary = "Consulta interna: ¿el socio tiene membresia vigente?",
            description = "Consumido por ms-reservas via Feign antes de confirmar una reserva.")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(value = "{\"vigente\": true}")))
    public ResponseEntity<Map<String, Boolean>> vigente(@PathVariable Long socioId) {
        log.info("Consulta de membresia vigente para socio={}", socioId);
        return ResponseEntity.ok(Map.of("vigente", service.tieneVigente(socioId)));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva membresia",
            description = "Valida via Feign que el socio este ACTIVO en ms-socios, que no tenga otra membresia VIGENTE y que el plan este activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membresia creada"),
            @ApiResponse(responseCode = "404", description = "Socio o plan no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "Socio no ACTIVO, ya tiene membresia VIGENTE, o plan inactivo", content = @Content)
    })
    public ResponseEntity<Membresia> crear(@Valid @RequestBody MembresiaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una membresia VIGENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membresia cancelada"),
            @ApiResponse(responseCode = "409", description = "La membresia no estaba VIGENTE", content = @Content)
    })
    public ResponseEntity<Membresia> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una membresia")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
