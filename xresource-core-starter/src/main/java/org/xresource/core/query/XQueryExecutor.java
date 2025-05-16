package org.xresource.core.query;

import org.xresource.core.annotations.XQuery;
import org.xresource.core.model.XResourceMetadata;

import jakarta.persistence.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.*;
import java.util.Map.Entry;

public class XQueryExecutor {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        if (!emf.isOpen()) {
            throw new IllegalStateException("EntityManagerFactory is closed");
        }
        return emf.createEntityManager();
    }

    public <T> List<T> executeQuery(Class<T> entityClass, XQuery query, Map<String, Object> context) {
        EntityManager entityManager = getEntityManager();
        if (query == null)
            throw new RuntimeException("No XQuery provided for entity: " + entityClass.getSimpleName());

        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE " + query.where();
        TypedQuery<T> jpaQuery = entityManager.createQuery(jpql, entityClass);

        // Inject dynamic context variables into query
        for (String ctxKey : query.contextParams()) {
            String paramName = extractParamName(ctxKey); // e.g. user.id -> id

            if (paramName.startsWith("context_loggeduser")) {
                String loggedUserContextParam = paramName.substring("context_loggeduser_".length());
                Object resolvedValue = resolveLoggedUserContextValue(loggedUserContextParam, context);
                jpaQuery.setParameter(paramName, resolvedValue);
            } else {
                Object resolvedValue = resolveContextValue(ctxKey, context);
                jpaQuery.setParameter(paramName, resolvedValue);
            }

        }

        return jpaQuery.getResultList();
    }

    public <T> Page<T> executePagedQuery(
            Class<T> entityClass,
            XQuery query,
            Map<String, Object> context,
            int page,
            int size,
            String sortBy,
            String direction) {
        if (query == null)
            throw new RuntimeException("No XQuery provided for entity: " + entityClass.getSimpleName());

        EntityManager entityManager = getEntityManager();
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE " + query.where();

        if (sortBy != null && !sortBy.isBlank()) {
            jpql += " ORDER BY e." + sortBy + " " + direction;
        }

        TypedQuery<T> jpaQuery = entityManager.createQuery(jpql, entityClass);

        for (String ctxKey : query.contextParams()) {
            String paramName = extractParamName(ctxKey);
            Object resolvedValue = resolveContextValue(ctxKey, context);
            jpaQuery.setParameter(paramName, resolvedValue);
        }

        jpaQuery.setFirstResult(page * size);
        jpaQuery.setMaxResults(size);

        // Total count query
        String countJpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE " + query.where();
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        for (String ctxKey : query.contextParams()) {
            String paramName = extractParamName(ctxKey);
            Object resolvedValue = resolveContextValue(ctxKey, context);
            countQuery.setParameter(paramName, resolvedValue);
        }

        long total = countQuery.getSingleResult();
        return new PageImpl<>(jpaQuery.getResultList(), PageRequest.of(page, size), total);
    }

    public <T> Page<T> executePagedQueries(
            Class<T> entityClass,
            Map<String, XQuery> xQueries,
            Map<String, Object> context,
            int page,
            int size,
            String sortBy,
            String direction) {
        if (xQueries == null || xQueries.isEmpty()) {
            throw new RuntimeException("No XQueries provided for entity: " + entityClass.getSimpleName());
        }

        StringBuilder whereBuilder = new StringBuilder();
        Map<String, Object> paramMap = new HashMap<>();
        boolean first = true;

        EntityManager entityManager = getEntityManager();

        for (XQuery query : xQueries.values()) {
            String where = query.where().trim();
            if (where.isEmpty())
                continue;

            if (!first) {
                whereBuilder.append(" AND ");
            } else {
                whereBuilder.append(" WHERE ");
                first = false;
            }

            whereBuilder.append("(").append(where).append(")");

            for (String ctxKey : query.contextParams()) {
                String paramName = extractParamName(ctxKey);
                Object resolvedValue;

                if (paramName.startsWith("context_loggeduser_")) {
                    String loggedParam = paramName.substring("context_loggeduser_".length());
                    resolvedValue = resolveLoggedUserContextValue(loggedParam, context);
                } else {
                    resolvedValue = resolveContextValue(ctxKey, context);
                }

                if (resolvedValue != null) {
                    paramMap.put(paramName, resolvedValue);
                }
            }
        }

        String whereClause = whereBuilder.toString();
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e " + whereClause;

        if (sortBy != null && !sortBy.isBlank()) {
            jpql += " ORDER BY e." + sortBy + " " + direction;
        }

        TypedQuery<T> jpaQuery = entityManager.createQuery(jpql, entityClass);
        paramMap.forEach(jpaQuery::setParameter);
        jpaQuery.setFirstResult(page * size);
        jpaQuery.setMaxResults(size);

        String countJpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e " + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        paramMap.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(jpaQuery.getResultList(), PageRequest.of(page, size), total);
    }

    public Optional<Object> findById(XResourceMetadata metadata, Map<String, XQuery> xQueries,
            Map<String, Object> context, Object id) {

        EntityManager entityManager = getEntityManager();
        // Build where clause combining ID and auto-apply queries
        String whereClause = "e.id = :id";
        for (Entry<String, XQuery> entry : xQueries.entrySet()) {
            XQuery query = entry.getValue();
            whereClause += " AND (" + query.where() + ")";
        }

        String jpql = "SELECT e FROM " + metadata.getEntityClass().getSimpleName() + " e WHERE " + whereClause;
        TypedQuery<Object> jpaQuery = (TypedQuery<Object>) entityManager.createQuery(jpql, metadata.getEntityClass());
        jpaQuery.setParameter("id", id);

        // Set dynamic context params
        Map<String, Object> paramMap = new HashMap<>();
        for (Entry<String, XQuery> entry : xQueries.entrySet()) {
            XQuery query = entry.getValue();
            for (String ctxKey : query.contextParams()) {
                String paramName = extractParamName(ctxKey);
                Object resolvedValue;

                if (paramName.startsWith("context_loggeduser_")) {
                    String loggedParam = paramName.substring("context_loggeduser_".length());
                    resolvedValue = resolveLoggedUserContextValue(loggedParam, context);
                } else {
                    resolvedValue = resolveContextValue(ctxKey, context);
                }

                if (resolvedValue != null) {
                    paramMap.put(paramName, resolvedValue);
                }
            }
        }

        paramMap.forEach(jpaQuery::setParameter);

        List<Object> results = jpaQuery.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public <T> List<T> executeXQueries(Class<T> entityClass, Map<String, XQuery> xQueries,
            Map<String, Object> context) {
        if (xQueries == null || xQueries.isEmpty()) {
            throw new RuntimeException("No XQueries provided for entity: " + entityClass.getSimpleName());
        }
        EntityManager entityManager = getEntityManager();
        StringBuilder whereBuilder = new StringBuilder();
        Map<String, Object> paramMap = new HashMap<>();
        boolean first = true;

        for (XQuery query : xQueries.values()) {
            String where = query.where().trim();
            if (where.isEmpty())
                continue;

            if (!first) {
                whereBuilder.append(" AND ");
            } else {
                first = false;
                whereBuilder.append(" WHERE ");
            }

            whereBuilder.append("(").append(where).append(")");

            // Resolve each context param
            for (String ctxKey : query.contextParams()) {
                String paramName = extractParamName(ctxKey); // e.g. user.id -> id

                Object resolvedValue;
                if (paramName.startsWith("context_loggeduser_")) {
                    String loggedParam = paramName.substring("context_loggeduser_".length());
                    resolvedValue = resolveLoggedUserContextValue(loggedParam, context);
                } else {
                    resolvedValue = resolveContextValue(ctxKey, context);
                }

                if (resolvedValue != null) {
                    paramMap.put(paramName, resolvedValue);
                }
            }
        }

        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e " + whereBuilder.toString();
        TypedQuery<T> jpaQuery = entityManager.createQuery(jpql, entityClass);

        // Set parameters
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            jpaQuery.setParameter(entry.getKey(), entry.getValue());
        }

        return jpaQuery.getResultList();
    }

    public String extractParamName(String ctxKey) {
        // Extract final segment of dot notation (e.g. user.team.id -> id)
        return ctxKey.contains(".") ? ctxKey.substring(ctxKey.lastIndexOf('.') + 1) : ctxKey;
    }

    public Object resolveLoggedUserContextValue(String ctxKey, Map<String, Object> context) {

        Map<String, Object> userContext = (Map<String, Object>) context.get("user");
        if (userContext == null) {
            throw new RuntimeException("Missing context value for: " + ctxKey);
        }
        Object value = userContext.get(ctxKey);
        if (value == null) {
            throw new RuntimeException("Missing context value for: " + ctxKey);
        }
        return value;
    }

    public Object resolveContextValue(String ctxKey, Map<String, Object> context) {
        Object value = context.get(ctxKey);
        if (value == null) {
            throw new RuntimeException("Missing context value for: " + ctxKey);
        }
        return value;
    }
}
