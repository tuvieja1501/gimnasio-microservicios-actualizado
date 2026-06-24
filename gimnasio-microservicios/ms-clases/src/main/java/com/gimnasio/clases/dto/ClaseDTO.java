package com.gimnasio.clases.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class ClaseDTO {

    @NotBlank @Size(max = 80)
    private String nombre;

    @Size(max = 250)
    private String descripcion;

    @NotNull(message = "El instructorId es obligatorio")
    private Long instructorId;

    @NotNull(message = "El sucursalId es obligatorio")
    private Long sucursalId;

    @NotNull
    @Future(message = "La clase debe agendarse a futuro")
    private LocalDateTime fechaHora;

    @NotNull
    @Min(value = 15, message = "Duracion minima 15 minutos")
    @Max(value = 180, message = "Duracion maxima 180 minutos")
    private Integer duracionMinutos;

    @NotNull
    @Min(value = 1, message = "El cupo minimo es 1")
    @Max(value = 100, message = "El cupo maximo permitido es 100")
    private Integer cupoMaximo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public Integer getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(Integer cupoMaximo) { this.cupoMaximo = cupoMaximo; }
}
