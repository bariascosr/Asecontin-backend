package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad que mapea la tabla imb.imagen (referencia a URL en almacenamiento externo).
 */
@Table("imagen")
public class Imagen {

	@Id
	@Column("id_imagen")
	private Long idImagen;

	@Column("inmueble_id")
	private Long inmuebleId;

	@Column("url_imagen")
	private String urlImagen;

	/** Indica si es la imagen principal del inmueble (para listados/cards). Una por inmueble. */
	@Column("es_principal")
	private Boolean esPrincipal;

	public Imagen() {}

	public Long getIdImagen() {
		return idImagen;
	}

	public void setIdImagen(Long idImagen) {
		this.idImagen = idImagen;
	}

	public Long getInmuebleId() {
		return inmuebleId;
	}

	public void setInmuebleId(Long inmuebleId) {
		this.inmuebleId = inmuebleId;
	}

	public String getUrlImagen() {
		return urlImagen;
	}

	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}

	public Boolean getEsPrincipal() {
		return esPrincipal != null && esPrincipal;
	}

	public void setEsPrincipal(Boolean esPrincipal) {
		this.esPrincipal = Boolean.TRUE.equals(esPrincipal);
	}
}
