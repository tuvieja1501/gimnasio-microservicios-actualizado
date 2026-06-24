package com.gimnasio.sucursales.dto;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

public class SucursalDTO {

    @NotBlank @Size(max = 80)
    private String nombre;

    @NotBlank @Size(max = 200)
    private String direccion;

    @NotBlank @Size(max = 80)
    private String comuna;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Telefono invalido")
    private String telefono;

    @NotNull
    @Min(value = 1, message = "La capacidad minima es 1")
    @Max(value = 500, message = "La capacidad maxima permitida es 500")
    private Integer capacidad;

    @NotNull
    private LocalTime horaApertura;

    @NotNull
    private LocalTime horaCierre;

    @NotNull
    private Boolean activa;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalTime horaApertura) { this.horaApertura = horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalTime horaCierre) { this.horaCierre = horaCierre; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
