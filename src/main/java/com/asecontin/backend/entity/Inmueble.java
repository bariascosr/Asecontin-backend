package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla imb.inmueble.
 */
@Table("inmueble")
public class Inmueble {

	@Id
	@Column("id_inmueble")
	private Long idInmueble;

	private String titulo;
	private String descripcion;

	@Column("precio_venta")
	private BigDecimal precioVenta;

	private String direccion;

	@Column("localidad_id")
	private Long localidadId;

	@Column("tipo_id")
	private Long tipoId;

	@Column("estado_id")
	private Long estadoId;

	@Column("valor_arriendo")
	private BigDecimal valorArriendo;

	@Column("propietario_id")
	private Long propietarioId;

	private String etiquetas;

	private Integer parqueaderos;

	@Column("sector_id")
	private Long sectorId;

	@Column("area_m2")
	private BigDecimal areaM2;

	private Integer habitaciones;
	private Integer banos;
	private Integer estrato;

	@Column("valor_administracion")
	private BigDecimal valorAdministracion;

	@Column("ano_construccion")
	private Integer anoConstruccion;

	private Boolean amoblado;
	private Integer piso;

	@Column("fecha_creacion")
	private LocalDateTime fechaCreacion;

	@Column("fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	public Inmueble() {}

	public Long getIdInmueble() {
		return idInmueble;
	}

	public void setIdInmueble(Long idInmueble) {
		this.idInmueble = idInmueble;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecioVenta() {
		return precioVenta;
	}

	public void setPrecioVenta(BigDecimal precioVenta) {
		this.precioVenta = precioVenta;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Long getLocalidadId() {
		return localidadId;
	}

	public void setLocalidadId(Long localidadId) {
		this.localidadId = localidadId;
	}

	public Long getTipoId() {
		return tipoId;
	}

	public void setTipoId(Long tipoId) {
		this.tipoId = tipoId;
	}

	public Long getEstadoId() {
		return estadoId;
	}

	public void setEstadoId(Long estadoId) {
		this.estadoId = estadoId;
	}

	public BigDecimal getValorArriendo() {
		return valorArriendo;
	}

	public void setValorArriendo(BigDecimal valorArriendo) {
		this.valorArriendo = valorArriendo;
	}

	public Long getPropietarioId() {
		return propietarioId;
	}

	public void setPropietarioId(Long propietarioId) {
		this.propietarioId = propietarioId;
	}

	public String getEtiquetas() {
		return etiquetas;
	}

	public void setEtiquetas(String etiquetas) {
		this.etiquetas = etiquetas;
	}

	public Integer getParqueaderos() {
		return parqueaderos;
	}

	public void setParqueaderos(Integer parqueaderos) {
		this.parqueaderos = parqueaderos;
	}

	public Long getSectorId() {
		return sectorId;
	}

	public void setSectorId(Long sectorId) {
		this.sectorId = sectorId;
	}

	public BigDecimal getAreaM2() {
		return areaM2;
	}

	public void setAreaM2(BigDecimal areaM2) {
		this.areaM2 = areaM2;
	}

	public Integer getHabitaciones() {
		return habitaciones;
	}

	public void setHabitaciones(Integer habitaciones) {
		this.habitaciones = habitaciones;
	}

	public Integer getBanos() {
		return banos;
	}

	public void setBanos(Integer banos) {
		this.banos = banos;
	}

	public Integer getEstrato() {
		return estrato;
	}

	public void setEstrato(Integer estrato) {
		this.estrato = estrato;
	}

	public BigDecimal getValorAdministracion() {
		return valorAdministracion;
	}

	public void setValorAdministracion(BigDecimal valorAdministracion) {
		this.valorAdministracion = valorAdministracion;
	}

	public Integer getAnoConstruccion() {
		return anoConstruccion;
	}

	public void setAnoConstruccion(Integer anoConstruccion) {
		this.anoConstruccion = anoConstruccion;
	}

	public Boolean getAmoblado() {
		return amoblado != null && amoblado;
	}

	public void setAmoblado(Boolean amoblado) {
		this.amoblado = Boolean.TRUE.equals(amoblado);
	}

	public Integer getPiso() {
		return piso;
	}

	public void setPiso(Integer piso) {
		this.piso = piso;
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
