package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla imb.configuracion_inmobiliaria (Acerca de nosotros: misión, visión, términos, política de datos, etc.).
 */
@Table("configuracion_inmobiliaria")
public class ConfiguracionInmobiliaria {

	@Id
	@Column("id_config")
	private Long idConfig;

	private String mision;
	private String vision;

	@Column("terminos_condiciones")
	private String terminosCondiciones;

	@Column("politica_tratamiento_datos")
	private String politicaTratamientoDatos;

	private String descripcion;

	@Column("fecha_creacion")
	private LocalDateTime fechaCreacion;

	@Column("fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	public ConfiguracionInmobiliaria() {}

	public Long getIdConfig() {
		return idConfig;
	}

	public void setIdConfig(Long idConfig) {
		this.idConfig = idConfig;
	}

	public String getMision() {
		return mision;
	}

	public void setMision(String mision) {
		this.mision = mision;
	}

	public String getVision() {
		return vision;
	}

	public void setVision(String vision) {
		this.vision = vision;
	}

	public String getTerminosCondiciones() {
		return terminosCondiciones;
	}

	public void setTerminosCondiciones(String terminosCondiciones) {
		this.terminosCondiciones = terminosCondiciones;
	}

	public String getPoliticaTratamientoDatos() {
		return politicaTratamientoDatos;
	}

	public void setPoliticaTratamientoDatos(String politicaTratamientoDatos) {
		this.politicaTratamientoDatos = politicaTratamientoDatos;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}
}
