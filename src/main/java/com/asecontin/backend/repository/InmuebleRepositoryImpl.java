package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Inmueble;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * Implementación de consultas dinámicas con filtros y rangos.
 */
@Repository
public class InmuebleRepositoryImpl implements InmuebleRepositoryCustom {

	private final R2dbcEntityTemplate template;

	public InmuebleRepositoryImpl(R2dbcEntityTemplate template) {
		this.template = template;
	}

	@Override
	public Flux<Inmueble> findByFilters(
			Optional<Long> estadoId,
			Optional<Long> localidadId,
			Optional<Long> tipoId,
			Optional<BigDecimal> precioMin,
			Optional<BigDecimal> precioMax,
			Optional<BigDecimal> areaMin,
			Optional<BigDecimal> areaMax,
			Optional<Integer> habitacionesMin,
			Optional<Integer> habitacionesMax,
			Optional<Integer> banosMin,
			Optional<Integer> banosMax,
			Optional<Integer> estratoMin,
			Optional<Integer> estratoMax,
			Optional<Integer> parqueaderosMin,
			Optional<Integer> parqueaderosMax,
			Sort sort) {
		List<Criteria> conditions = buildCriteriaList(
				estadoId, localidadId, tipoId, precioMin, precioMax,
				areaMin, areaMax, habitacionesMin, habitacionesMax,
				banosMin, banosMax, estratoMin, estratoMax,
				parqueaderosMin, parqueaderosMax);
		Query query = conditions.isEmpty()
				? Query.empty().sort(sort)
				: Query.query(combineCriteria(conditions)).sort(sort);
		return template.select(Inmueble.class).matching(query).all();
	}

	@Override
	public Mono<Long> countByFilters(
			Optional<Long> estadoId,
			Optional<Long> localidadId,
			Optional<Long> tipoId,
			Optional<BigDecimal> precioMin,
			Optional<BigDecimal> precioMax,
			Optional<BigDecimal> areaMin,
			Optional<BigDecimal> areaMax,
			Optional<Integer> habitacionesMin,
			Optional<Integer> habitacionesMax,
			Optional<Integer> banosMin,
			Optional<Integer> banosMax,
			Optional<Integer> estratoMin,
			Optional<Integer> estratoMax,
			Optional<Integer> parqueaderosMin,
			Optional<Integer> parqueaderosMax) {
		List<Criteria> conditions = buildCriteriaList(
				estadoId, localidadId, tipoId, precioMin, precioMax,
				areaMin, areaMax, habitacionesMin, habitacionesMax,
				banosMin, banosMax, estratoMin, estratoMax,
				parqueaderosMin, parqueaderosMax);
		Query query = conditions.isEmpty()
				? Query.empty()
				: Query.query(combineCriteria(conditions));
		return template.select(Inmueble.class).matching(query).count();
	}

	private List<Criteria> buildCriteriaList(
			Optional<Long> estadoId,
			Optional<Long> localidadId,
			Optional<Long> tipoId,
			Optional<BigDecimal> precioMin,
			Optional<BigDecimal> precioMax,
			Optional<BigDecimal> areaMin,
			Optional<BigDecimal> areaMax,
			Optional<Integer> habitacionesMin,
			Optional<Integer> habitacionesMax,
			Optional<Integer> banosMin,
			Optional<Integer> banosMax,
			Optional<Integer> estratoMin,
			Optional<Integer> estratoMax,
			Optional<Integer> parqueaderosMin,
			Optional<Integer> parqueaderosMax) {
		List<Criteria> list = new ArrayList<>();
		estadoId.ifPresent(id -> list.add(where("estadoId").is(id)));
		localidadId.ifPresent(id -> list.add(where("localidadId").is(id)));
		tipoId.ifPresent(id -> list.add(where("tipoId").is(id)));
		precioMin.ifPresent(v -> list.add(where("precio").greaterThanOrEquals(v)));
		precioMax.ifPresent(v -> list.add(where("precio").lessThanOrEquals(v)));
		areaMin.ifPresent(v -> list.add(where("areaM2").greaterThanOrEquals(v)));
		areaMax.ifPresent(v -> list.add(where("areaM2").lessThanOrEquals(v)));
		habitacionesMin.ifPresent(v -> list.add(where("habitaciones").greaterThanOrEquals(v)));
		habitacionesMax.ifPresent(v -> list.add(where("habitaciones").lessThanOrEquals(v)));
		banosMin.ifPresent(v -> list.add(where("banos").greaterThanOrEquals(v)));
		banosMax.ifPresent(v -> list.add(where("banos").lessThanOrEquals(v)));
		estratoMin.ifPresent(v -> list.add(where("estrato").greaterThanOrEquals(v)));
		estratoMax.ifPresent(v -> list.add(where("estrato").lessThanOrEquals(v)));
		parqueaderosMin.ifPresent(v -> list.add(where("parqueaderos").greaterThanOrEquals(v)));
		parqueaderosMax.ifPresent(v -> list.add(where("parqueaderos").lessThanOrEquals(v)));
		return list;
	}

	private Criteria combineCriteria(List<Criteria> conditions) {
		Criteria combined = conditions.get(0);
		for (int i = 1; i < conditions.size(); i++) {
			combined = combined.and(conditions.get(i));
		}
		return combined;
	}
}
