package com.gimnasio.asistencias.dto;

public class SucursalRespuesta {
    private Long id;
    private String nombre;
    private Boolean activa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
