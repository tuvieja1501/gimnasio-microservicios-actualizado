package com.gimnasio.asistencias.controller;

import com.gimnasio.asistencias.dto.AsistenciaDTO;
import com.gimnasio.asistencias.model.Asistencia;
import com.gimnasio.asistencias.service.AsistenciaService;
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
@RequestMapping("/api/asistencias")
@Tag(name = "Asistencias", description = "Control de ingreso/salida de socios; valida socio y sucursal remotos via Feign")
public class AsistenciaController {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaController.class);

    private final AsistenciaService service;

    public AsistenciaController(AsistenciaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las asistencias")
    public ResponseEntity<List<Asistencia>> listar() {
        log.info("GET /api/asistencias");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener asistencia por id")
    @ApiResponse(responseCode = "404", description = "Asistencia no encontrada", content = @Content)
    public ResponseEntity<Asistencia> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/socio/{socioId}")
    @Operation(summary = "Listar historial de asistencias de un socio")
    public ResponseEntity<List<Asistencia>> porSocio(@PathVariable Long socioId) {
        return ResponseEntity.ok(service.listarPorSocio(socioId));
    }

    @PostMapping("/ingreso")
    @Operation(summary = "Registrar el ingreso de un socio a una sucursal",
            description = "Valida via Feign que el socio este ACTIVO y la sucursal activa, y que el socio no tenga ya un ingreso abierto (sin salida).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ingreso registrado"),
            @ApiResponse(responseCode = "409", description = "Socio inactivo o ya tiene un ingreso sin salida registrada", content = @Content)
    })
    public ResponseEntity<Asistencia> registrarIngreso(@Valid @RequestBody AsistenciaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarIngreso(dto));
    }

    @PatchMapping("/salida/socio/{socioId}")
    @Operation(summary = "Registrar la salida del socio", description = "Cierra el ingreso abierto mas reciente del socio.")
    @ApiResponse(responseCode = "409", description = "El socio no tiene ningun ingreso abierto", content = @Content)
    public ResponseEntity<Asistencia> registrarSalida(@PathVariable Long socioId) {
        return ResponseEntity.ok(service.registrarSalida(socioId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de asistencia")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
