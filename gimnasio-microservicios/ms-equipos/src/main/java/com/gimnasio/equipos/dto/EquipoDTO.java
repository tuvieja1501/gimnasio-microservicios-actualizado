package com.gimnasio.equipos.dto;

import com.gimnasio.equipos.model.EstadoEquipo;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class EquipoDTO {

    @NotBlank @Size(max = 80)
    private String nombre;

    @NotBlank @Size(max = 50)
    private String tipo;

    @NotBlank
    @Size(min = 3, max = 30, message = "El codigo interno debe tener entre 3 y 30 caracteres")
    private String codigoInterno;

    @NotNull(message = "La sucursalId es obligatoria")
    private Long sucursalId;

    @PastOrPresent(message = "La fecha de adquisicion no puede ser futura")
    private LocalDate fechaAdquisicion;

    @NotNull
    private EstadoEquipo estado;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getCodigoInterno() { return codigoInterno; }
    public void setCodigoInterno(String codigoInterno) { this.codigoInterno = codigoInterno; }
    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
    public LocalDate getFechaAdquisicion() { return fechaAdquisicion; }
    public void setFechaAdquisicion(LocalDate fechaAdquisicion) { this.fechaAdquisicion = fechaAdquisicion; }
    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }
}
