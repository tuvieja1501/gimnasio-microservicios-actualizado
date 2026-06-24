package com.gimnasio.reservas.dto;

import jakarta.validation.constraints.NotNull;

public class ReservaDTO {

    @NotNull(message = "El socioId es obligatorio")
    private Long socioId;

    @NotNull(message = "El claseId es obligatorio")
    private Long claseId;

    public Long getSocioId() { return socioId; }
    public void setSocioId(Long socioId) { this.socioId = socioId; }
    public Long getClaseId() { return claseId; }
    public void setClaseId(Long claseId) { this.claseId = claseId; }
}
