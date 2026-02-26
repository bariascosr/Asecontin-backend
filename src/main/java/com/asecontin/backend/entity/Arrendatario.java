package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

/**
 * Entidad que mapea la tabla imb.arrendatario (relación N:M con inmuebles vía inmueble_arrendatario).
 */
@Table("arrendatario")
public class Arrendatario {

	@Id
	@Column("id_arrendatario")
	private Long idArrendatario;

	private String nombres;
	private String apellidos;
	private String cedula;

	@Column("fecha_expedicion")
	private LocalDate fechaExpedicion;

	/** Teléfono (ej. celular) para notificaciones por WhatsApp. Opcional. */
	private String telefono;

	public Arrendatario() {}

	public Long getIdArrendatario() {
		return idArrendatario;
	}

	public void setIdArrendatario(Long idArrendatario) {
		this.idArrendatario = idArrendatario;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public LocalDate getFechaExpedicion() {
		return fechaExpedicion;
	}

	public void setFechaExpedicion(LocalDate fechaExpedicion) {
		this.fechaExpedicion = fechaExpedicion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
}
