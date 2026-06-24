package com.gimnasio.asistencias.dto;

import jakarta.validation.constraints.NotNull;

public class AsistenciaDTO {

    @NotNull(message = "El socioId es obligatorio")
    private Long socioId;

    @NotNull(message = "El sucursalId es obligatorio")
    private Long sucursalId;

    public Long getSocioId() { return socioId; }
    public void setSocioId(Long socioId) { this.socioId = socioId; }
    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
}
