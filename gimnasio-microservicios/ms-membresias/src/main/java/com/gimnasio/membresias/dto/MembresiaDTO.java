package com.gimnasio.membresias.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class MembresiaDTO {

    @NotNull(message = "El socioId es obligatorio")
    private Long socioId;

    @NotNull(message = "El planId es obligatorio")
    private Long planId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    public Long getSocioId() { return socioId; }
    public void setSocioId(Long socioId) { this.socioId = socioId; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
}
