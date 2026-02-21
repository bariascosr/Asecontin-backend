package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad que mapea la tabla imb.estado (disponible, vendido, rentado, etc.).
 */
@Table("estado")
public class Estado {

	@Id
	@Column("id_estado")
	private Long idEstado;

	@Column("nombre_estado")
	private String nombreEstado;

	public Estado() {}

	public Estado(Long idEstado, String nombreEstado) {
		this.idEstado = idEstado;
		this.nombreEstado = nombreEstado;
	}

	public Long getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(Long idEstado) {
		this.idEstado = idEstado;
	}

	public String getNombreEstado() {
		return nombreEstado;
	}

	public void setNombreEstado(String nombreEstado) {
		this.nombreEstado = nombreEstado;
	}
}
