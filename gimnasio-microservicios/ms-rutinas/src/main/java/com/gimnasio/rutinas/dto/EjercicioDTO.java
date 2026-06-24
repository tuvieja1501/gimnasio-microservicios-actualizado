package com.gimnasio.rutinas.dto;

import jakarta.validation.constraints.*;

public class EjercicioDTO {

    @NotBlank @Size(max = 100)
    private String nombre;

    @NotNull
    @Min(value = 1, message = "Las series deben ser al menos 1")
    @Max(value = 20)
    private Integer series;

    @NotNull
    @Min(value = 1, message = "Las repeticiones deben ser al menos 1")
    @Max(value = 100)
    private Integer repeticiones;

    @Min(value = 0, message = "El descanso no puede ser negativo")
    @Max(value = 600, message = "Descanso maximo 600 segundos (10 minutos)")
    private Integer descansoSegundos;

    @Size(max = 250)
    private String observaciones;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }
    public Integer getRepeticiones() { return repeticiones; }
    public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }
    public Integer getDescansoSegundos() { return descansoSegundos; }
    public void setDescansoSegundos(Integer descansoSegundos) { this.descansoSegundos = descansoSegundos; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
