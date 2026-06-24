package com.gimnasio.socios.controller;

import com.gimnasio.socios.dto.SocioDTO;
import com.gimnasio.socios.model.EstadoSocio;
import com.gimnasio.socios.model.Socio;
import com.gimnasio.socios.service.SocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * Controller REST para Socios.
 * Expone endpoints CRUD y operaciones de dominio.
 * Solo orquesta: delega la logica al SocioService.
 */
@RestController
@RequestMapping("/api/socios")
@Tag(name = "Socios", description = "Gestion de socios del gimnasio: alta, busqueda, actualizacion y cambio de estado")
public class SocioController {

    private static final Logger log = LoggerFactory.getLogger(SocioController.class);

    private final SocioService service;

    public SocioController(SocioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los socios", description = "Retorna el listado completo de socios registrados, sin filtros.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = "[{\"id\":1,\"nombre\":\"Juan\",\"apellido\":\"Perez\",\"rut\":\"12345678-9\",\"email\":\"juan.perez@mail.com\",\"estado\":\"ACTIVO\"}]")))
    public ResponseEntity<List<Socio>> listar() {
        log.info("GET /api/socios");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener socio por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Socio encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"id\":1,\"nombre\":\"Juan\",\"apellido\":\"Perez\",\"rut\":\"12345678-9\",\"estado\":\"ACTIVO\"}"))),
            @ApiResponse(responseCode = "404", description = "No existe un socio con ese id", content = @Content)
    })
    public ResponseEntity<Socio> obtener(
            @Parameter(description = "Id numerico del socio", example = "1") @PathVariable Long id) {
        log.info("GET /api/socios/{}", id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/rut/{rut}")
    @Operation(summary = "Obtener socio por RUT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Socio encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un socio con ese RUT", content = @Content)
    })
    public ResponseEntity<Socio> obtenerPorRut(
            @Parameter(description = "RUT en formato 12345678-9", example = "12345678-9") @PathVariable String rut) {
        log.info("GET /api/socios/rut/{}", rut);
        return ResponseEntity.ok(service.buscarPorRut(rut));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo socio",
            description = "Valida que el RUT y el email sean unicos (RN-S1, RN-S2) y que el socio tenga al menos 14 anios (RN-S3).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Socio creado correctamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"id\":5,\"nombre\":\"Ana\",\"apellido\":\"Soto\",\"rut\":\"11111111-1\",\"estado\":\"ACTIVO\"}"))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (validacion de campos)", content = @Content),
            @ApiResponse(responseCode = "409", description = "RUT o email ya registrado, o socio menor de 14 anios", content = @Content)
    })
    public ResponseEntity<Socio> crear(@Valid @RequestBody SocioDTO dto) {
        log.info("POST /api/socios rut={}", dto.getRut());
        Socio creado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un socio existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Socio actualizado"),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "RUT/email en conflicto con otro socio", content = @Content)
    })
    public ResponseEntity<Socio> actualizar(@PathVariable Long id,
                                            @Valid @RequestBody SocioDTO dto) {
        log.info("PUT /api/socios/{}", id);
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de un socio", description = "Ej: ACTIVO, MOROSO, INACTIVO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content)
    })
    public ResponseEntity<Socio> cambiarEstado(@PathVariable Long id,
                                               @Schema(implementation = EstadoSocio.class) @RequestParam EstadoSocio estado) {
        log.info("PATCH /api/socios/{}/estado -> {}", id, estado);
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un socio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Socio eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado", content = @Content)
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/socios/{}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

