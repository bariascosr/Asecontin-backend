package com.asecontin.backend.dto;

import java.time.LocalDateTime;

public record ArticuloResponse(Long id, String titulo, String contenido, LocalDateTime fechaPublicacion) {}
