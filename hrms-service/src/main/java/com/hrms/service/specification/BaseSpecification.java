package com.hrms.service.specification;

import com.hrms.core.model.dto.FilterDTO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import one.util.streamex.StreamEx;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.function.BiFunction;

public class BaseSpecification {

    public static <T> Specification<T> filterBy(List<FilterDTO> filters, BiFunction<String, Object, Object> valueConverter) {
        return (root, query, cb) -> {
            var predicates = StreamEx.of(filters)
                .map(f -> createPredicate(f, root, cb, valueConverter))
                .toArray(Predicate[]::new);
            return cb.and(predicates);
        };
    }

    private static <T> Predicate createPredicate(FilterDTO filter, Root<T> root, CriteriaBuilder cb, BiFunction<String, Object, Object> converter) {
        var field = filter.getField();
        var value = filter.getValue();
        return switch (filter.getOperator().toUpperCase()) {
            case "CONTAINS" -> contains(field, value, root, cb);
            case "CONTAINS_ANY" -> containsAny(field, value, root, cb);
            case "EQUALS" -> cb.equal(getPath(field, root), converter.apply(field, value));
            case "NOT_EQUALS" -> cb.notEqual(getPath(field, root), converter.apply(field, value));
            case "GREATER_THAN" -> greaterThan(field, value, root, cb);
            case "LESS_THAN" -> lessThan(field, value, root, cb);
            case "IS_NULL" -> cb.isNull(getPath(field, root));
            case "IS_NOT_NULL" -> cb.isNotNull(getPath(field, root));
            case "IN" -> createInPredicate(field, value, root);
            default -> throw new IllegalArgumentException("Unknown operator: " + filter.getOperator());
        };
    }

    private static <T> Predicate contains(String field, Object value, Root<T> root, CriteriaBuilder cb) {
        return cb.like(
            cb.lower(getPath(field, root).as(String.class)),
            "%" + value.toString().toLowerCase() + "%"
        );
    }

    private static <T> Predicate containsAny(String fields, Object value, Root<T> root, CriteriaBuilder cb) {
        var searchValue = "%" + value.toString().toLowerCase() + "%";
        var predicates = StreamEx.of(fields.split(","))
            .map(String::trim)
            .map(f -> cb.like(cb.lower(getPath(f, root).as(String.class)), searchValue))
            .toArray(Predicate[]::new);
        return cb.or(predicates);
    }

    @SuppressWarnings("unchecked")
    private static <T> Predicate greaterThan(String field, Object value, Root<T> root, CriteriaBuilder cb) {
        var numValue = Double.parseDouble(value.toString());
        return cb.gt((Path<Number>) (Path<?>) getPath(field, root), numValue);
    }

    @SuppressWarnings("unchecked")
    private static <T> Predicate lessThan(String field, Object value, Root<T> root, CriteriaBuilder cb) {
        var numValue = Double.parseDouble(value.toString());
        return cb.lt((Path<Number>) (Path<?>) getPath(field, root), numValue);
    }

    private static <T> Predicate createInPredicate(String field, Object value, Root<T> root) {
        var path = getPath(field, root);
        var values = StreamEx.of(value.toString().split(","))
            .map(String::trim)
            .map(v -> field.endsWith("id") || field.endsWith("Id") ? Long.parseLong(v) : v)
            .toList();
        return path.in(values);
    }

    public static <T> Path<?> getPath(String field, Root<T> root) {
        if (!field.contains(".")) {
            return root.get(field);
        }
        var parts = field.split("\\.");
        Path<?> path = root.get(parts[0]);
        for (var i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }
}

