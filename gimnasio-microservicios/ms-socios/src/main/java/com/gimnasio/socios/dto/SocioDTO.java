package com.gimnasio.socios.dto;

import com.gimnasio.socios.model.EstadoSocio;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar socios.
 * Aplica validaciones con Bean Validation (JSR 380).
 */
public class SocioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 80, message = "El apellido no puede superar 80 caracteres")
    private String apellido;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$",
             message = "RUT invalido. Formato esperado: 12345678-9")
    private String rut;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email con formato invalido")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$",
             message = "Telefono invalido (solo digitos, opcional +)")
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El estado es obligatorio")
    private EstadoSocio estado;

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public EstadoSocio getEstado() { return estado; }
    public void setEstado(EstadoSocio estado) { this.estado = estado; }
}
