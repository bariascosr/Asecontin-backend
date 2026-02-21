package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Sector de la ciudad (Oriente, Occidente, Norte, Sur, etc.).
 */
@Table("sector")
public class Sector {

	@Id
	@Column("id_sector")
	private Long idSector;

	private String nombre;

	public Sector() {}

	public Long getIdSector() {
		return idSector;
	}

	public void setIdSector(Long idSector) {
		this.idSector = idSector;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
