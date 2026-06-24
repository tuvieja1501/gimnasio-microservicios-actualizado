package com.gimnasio.pagos.dto;

import com.gimnasio.pagos.model.MetodoPago;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PagoDTO {

    @NotNull(message = "El socioId es obligatorio")
    private Long socioId;

    @NotNull(message = "El membresiaId es obligatorio")
    private Long membresiaId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "El metodo de pago es obligatorio")
    private MetodoPago metodoPago;

    @Size(max = 80)
    private String referencia;

    public Long getSocioId() { return socioId; }
    public void setSocioId(Long socioId) { this.socioId = socioId; }
    public Long getMembresiaId() { return membresiaId; }
    public void setMembresiaId(Long membresiaId) { this.membresiaId = membresiaId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
