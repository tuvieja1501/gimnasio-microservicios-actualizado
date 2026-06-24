package com.gimnasio.instructores.controller;

import com.gimnasio.instructores.dto.InstructorDTO;
import com.gimnasio.instructores.model.Instructor;
import com.gimnasio.instructores.service.InstructorService;
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
@RequestMapping("/api/instructores")
@Tag(name = "Instructores", description = "Gestion de instructores del gimnasio y sus especialidades")
public class InstructorController {

    private static final Logger log = LoggerFactory.getLogger(InstructorController.class);

    private final InstructorService service;

    public InstructorController(InstructorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los instructores")
    public ResponseEntity<List<Instructor>> listar() {
        log.info("GET /api/instructores");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar solo instructores activos")
    public ResponseEntity<List<Instructor>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    @GetMapping("/especialidad/{esp}")
    @Operation(summary = "Buscar instructores por especialidad", description = "Busqueda case-insensitive, ej: 'yoga' encuentra 'Yoga'")
    public ResponseEntity<List<Instructor>> porEspecialidad(@PathVariable String esp) {
        return ResponseEntity.ok(service.buscarPorEspecialidad(esp));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener instructor por id")
    @ApiResponse(responseCode = "404", description = "Instructor no encontrado", content = @Content)
    public ResponseEntity<Instructor> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo instructor", description = "Valida que el RUT y el email sean unicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Instructor creado"),
            @ApiResponse(responseCode = "409", description = "RUT o email ya registrado", content = @Content)
    })
    public ResponseEntity<Instructor> crear(@Valid @RequestBody InstructorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un instructor")
    public ResponseEntity<Instructor> actualizar(@PathVariable Long id, @Valid @RequestBody InstructorDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un instructor")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
