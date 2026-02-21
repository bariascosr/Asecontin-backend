package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Tipo de inmueble (Casa, Apartamento, Lote, etc.).
 */
@Table("tipo_inmueble")
public class TipoInmueble {

	@Id
	@Column("id_tipo")
	private Long idTipo;

	private String nombre;

	public TipoInmueble() {}

	public Long getIdTipo() {
		return idTipo;
	}

	public void setIdTipo(Long idTipo) {
		this.idTipo = idTipo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
