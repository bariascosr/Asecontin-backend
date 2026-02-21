package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Localidad de una ciudad (por ahora principalmente Bogotá: Usaquén, Chapinero, etc.).
 */
@Table("localidad")
public class Localidad {

	@Id
	@Column("id_localidad")
	private Long idLocalidad;

	private String nombre;

	@Column("ciudad_id")
	private Long ciudadId;

	public Localidad() {}

	public Long getIdLocalidad() {
		return idLocalidad;
	}

	public void setIdLocalidad(Long idLocalidad) {
		this.idLocalidad = idLocalidad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Long getCiudadId() {
		return ciudadId;
	}

	public void setCiudadId(Long ciudadId) {
		this.ciudadId = ciudadId;
	}
}
