package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Ciudad permitida para inmuebles (capitales de departamentos de Colombia).
 */
@Table("ciudad")
public class Ciudad {

	@Id
	@Column("id_ciudad")
	private Long idCiudad;

	private String nombre;

	public Ciudad() {}

	public Long getIdCiudad() {
		return idCiudad;
	}

	public void setIdCiudad(Long idCiudad) {
		this.idCiudad = idCiudad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
