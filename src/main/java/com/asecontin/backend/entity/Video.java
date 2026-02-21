package com.asecontin.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad que mapea la tabla imb.video (referencia a URL en almacenamiento externo).
 */
@Table("video")
public class Video {

	@Id
	@Column("id_video")
	private Long idVideo;

	@Column("inmueble_id")
	private Long inmuebleId;

	@Column("url_video")
	private String urlVideo;

	public Video() {}

	public Long getIdVideo() {
		return idVideo;
	}

	public void setIdVideo(Long idVideo) {
		this.idVideo = idVideo;
	}

	public Long getInmuebleId() {
		return inmuebleId;
	}

	public void setInmuebleId(Long inmuebleId) {
		this.inmuebleId = inmuebleId;
	}

	public String getUrlVideo() {
		return urlVideo;
	}

	public void setUrlVideo(String urlVideo) {
		this.urlVideo = urlVideo;
	}
}
