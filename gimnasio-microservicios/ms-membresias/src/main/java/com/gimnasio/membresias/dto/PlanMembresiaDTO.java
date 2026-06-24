package com.gimnasio.membresias.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PlanMembresiaDTO {

    @NotBlank(message = "El nombre del plan es obligatorio")
    @Size(max = 80)
    private String nombre;

    @Size(max = 250)
    private String descripcion;

    @NotNull(message = "La duracion es obligatoria")
    @Min(value = 1, message = "La duracion minima es 1 mes")
    @Max(value = 36, message = "La duracion maxima es 36 meses")
    private Integer duracionMeses;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotNull(message = "Debe indicar si el plan esta activo")
    private Boolean activo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getDuracionMeses() { return duracionMeses; }
    public void setDuracionMeses(Integer duracionMeses) { this.duracionMeses = duracionMeses; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
