package org.xresource.internal.intent.core.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JPQLExecutorUtility {

    private final EntityManager entityManager;

    /**
     * Execute a JPQL query with optional named parameters and return a list of
     * Object[] results.
     *
     * @param jpql   the JPQL query string
     * @param params map of parameter name to value, can be null or empty if no
     *               params
     * @return list of Object arrays representing each row of the result
     */
    public List<Object[]> executeQuery(String jpql, Map<String, Object> params) {
        if (jpql == null || jpql.isEmpty()) {
            return Collections.emptyList();
        }

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);

        if (params != null && !params.isEmpty()) {
            params.forEach(query::setParameter);
        }

        return query.getResultList();
    }

    /**
     * Overload: Execute JPQL without parameters.
     */
    public List<Object[]> executeQuery(String jpql) {
        return executeQuery(jpql, null);
    }
}
