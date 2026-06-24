package com.gimnasio.membresias.dto;

/**
 * DTO espejo para deserializar la respuesta del microservicio ms-socios
 * cuando se consulta via Feign. Solo incluye los campos que este
 * microservicio necesita conocer.
 */
public class SocioRespuesta {

    private Long id;
    private String nombre;
    private String apellido;
    private String rut;
    private String email;
    private String estado;

    public SocioRespuesta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
