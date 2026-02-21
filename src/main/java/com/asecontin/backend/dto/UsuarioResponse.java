package com.asecontin.backend.dto;

/**
 * Respuesta de usuario sin exponer la contraseña.
 */
public record UsuarioResponse(Long id, String nombre, String email, String rol) {}
