package com.gimnasio.asistencias.dto;

public class SocioRespuesta {
    private Long id;
    private String nombre;
    private String apellido;
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
