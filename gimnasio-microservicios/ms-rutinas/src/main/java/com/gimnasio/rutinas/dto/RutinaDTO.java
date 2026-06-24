package com.gimnasio.rutinas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class RutinaDTO {

    @NotBlank @Size(max = 100)
    private String nombre;

    @Size(max = 250)
    private String objetivo;

    @NotNull
    private Long socioId;

    @NotNull
    private Long instructorId;

    @NotNull
    @Min(value = 1, message = "La duracion minima es 1 semana")
    @Max(value = 52, message = "La duracion maxima es 52 semanas")
    private Integer duracionSemanas;

    @NotNull(message = "La rutina debe tener al menos un ejercicio")
    @Size(min = 1, message = "La rutina debe tener al menos un ejercicio")
    @Valid
    private List<EjercicioDTO> ejercicios;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public Long getSocioId() { return socioId; }
    public void setSocioId(Long socioId) { this.socioId = socioId; }
    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }
    public Integer getDuracionSemanas() { return duracionSemanas; }
    public void setDuracionSemanas(Integer duracionSemanas) { this.duracionSemanas = duracionSemanas; }
    public List<EjercicioDTO> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<EjercicioDTO> ejercicios) { this.ejercicios = ejercicios; }
}
