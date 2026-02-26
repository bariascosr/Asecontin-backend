package com.asecontin.backend.repository;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación con SQL nativo para la tabla imb.inmueble_arrendatario (PK compuesta).
 * Usa Connection + Statement directamente para evitar el wrapper InParameter de DatabaseClient
 * que el driver PostgreSQL no codifica correctamente.
 */
@Repository
public class InmuebleArrendatarioRepositoryImpl implements InmuebleArrendatarioRepository {

	private final ConnectionFactory connectionFactory;

	public InmuebleArrendatarioRepositoryImpl(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@Override
	public Flux<Long> findInmuebleIdsByArrendatarioId(Long arrendatarioId) {
		String sql = "SELECT inmueble_id FROM imb.inmueble_arrendatario WHERE arrendatario_id = $1";
		int id = arrendatarioId.intValue();
		return Flux.usingWhen(
				Mono.from(connectionFactory.create()),
				conn -> Flux.from(conn.createStatement(sql).bind(0, id).execute())
						.flatMap(r -> Flux.from(r.map((row, meta) -> row.get("inmueble_id", Long.class)))),
				Connection::close);
	}

	@Override
	public Mono<Void> asociar(Long inmuebleId, Long arrendatarioId) {
		String sql = "INSERT INTO imb.inmueble_arrendatario (inmueble_id, arrendatario_id) VALUES ($1, $2) ON CONFLICT DO NOTHING";
		int inmId = inmuebleId.intValue();
		int arrId = arrendatarioId.intValue();
		return Mono.usingWhen(
				Mono.from(connectionFactory.create()),
				conn -> Flux.from(conn.createStatement(sql).bind(0, inmId).bind(1, arrId).execute())
						.flatMap(Result::getRowsUpdated)
						.then(),
				Connection::close);
	}

	@Override
	public Mono<Void> desasociar(Long inmuebleId, Long arrendatarioId) {
		String sql = "DELETE FROM imb.inmueble_arrendatario WHERE inmueble_id = $1 AND arrendatario_id = $2";
		int inmId = inmuebleId.intValue();
		int arrId = arrendatarioId.intValue();
		return Mono.usingWhen(
				Mono.from(connectionFactory.create()),
				conn -> Flux.from(conn.createStatement(sql).bind(0, inmId).bind(1, arrId).execute())
						.flatMap(Result::getRowsUpdated)
						.then(),
				Connection::close);
	}

	@Override
	public Mono<Boolean> existsByInmuebleIdAndArrendatarioId(Long inmuebleId, Long arrendatarioId) {
		String sql = "SELECT 1 FROM imb.inmueble_arrendatario WHERE inmueble_id = $1 AND arrendatario_id = $2 LIMIT 1";
		int inmId = inmuebleId.intValue();
		int arrId = arrendatarioId.intValue();
		return Mono.usingWhen(
				Mono.from(connectionFactory.create()),
				conn -> Flux.from(conn.createStatement(sql).bind(0, inmId).bind(1, arrId).execute())
						.flatMap(r -> Flux.from(r.map((row, meta) -> 1)))
						.hasElements()
						.defaultIfEmpty(false),
				Connection::close);
	}
}
