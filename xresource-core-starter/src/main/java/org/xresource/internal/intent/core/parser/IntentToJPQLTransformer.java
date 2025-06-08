package org.xresource.internal.intent.core.parser;

import org.xresource.internal.intent.core.parser.model.*;
import org.xresource.internal.exception.XResourceException;
import org.xresource.internal.models.XRelationshipMetadata;
import org.xresource.internal.util.XResourceGraphBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Transformer utility to convert IntentMeta metadata into JPQL query string.
 * 
 * Supports building SELECT, JOIN, WHERE, ORDER BY clauses with parameter
 * binding hints.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
public class IntentToJPQLTransformer {

    /**
     * Converts IntentMeta into JPQL query string.
     * 
     * @param intentMeta the parsed Intent metadata
     * @return JPQL query string ready for execution with parameter bindings
     */
    public static String toJPQL(IntentMeta intentMeta, Map<String, Map<String, XRelationshipMetadata>> graph) {
        StringBuilder jpql = new StringBuilder();
        String rootAlias = intentMeta.getRootAlias().isEmpty()
                ? intentMeta.getRootResource().charAt(0) + String.valueOf(UUID.randomUUID())
                : intentMeta.getRootAlias();
        jpql.append(buildSelectClause(intentMeta.getSelectAttributes(), rootAlias));

        jpql.append(buildFromClause(intentMeta.getEntityClass().getSimpleName(), rootAlias));

        jpql.append(buildJoinClauses(intentMeta.getRootResource(),
                rootAlias, intentMeta.getJoins(),
                graph));

        String whereClause = buildWhereClause(intentMeta);
        if (!whereClause.isEmpty()) {
            jpql.append(" WHERE ").append(whereClause);
        }

        if (intentMeta.getSortBy() != null && !intentMeta.getSortBy().isEmpty()) {
            jpql.append(" ORDER BY ");
            jpql.append(String.join(", ", intentMeta.getSortBy()));
        }

        // TODO: Add GROUP BY, pagination, limit support as needed

        return jpql.toString();
    }

    /**
     * Converts IntentMeta into JPQL count query string.
     * 
     * @param intentMeta the parsed Intent metadata
     * @return JPQL count query string ready for execution with parameter bindings
     */
    public static String toJPQLCountQuery(IntentMeta intentMeta,
            Map<String, Map<String, XRelationshipMetadata>> graph) {
        StringBuilder jpql = new StringBuilder();
        String rootAlias = intentMeta.getRootAlias().isEmpty()
                ? intentMeta.getRootResource().charAt(0) + String.valueOf(UUID.randomUUID())
                : intentMeta.getRootAlias();
        jpql.append("SELECT count(" + rootAlias + ")");

        jpql.append(buildFromClause(intentMeta.getEntityClass().getSimpleName(), rootAlias));

        jpql.append(buildJoinClauses(intentMeta.getRootResource(),
                rootAlias, intentMeta.getJoins(),
                graph));

        String whereClause = buildWhereClause(intentMeta);
        if (!whereClause.isEmpty()) {
            jpql.append(" WHERE ").append(whereClause);
        }

        return jpql.toString();
    }

    private static String buildSelectClause(List<SelectAttributeMeta> selectAttributes, String rootAlias) {
        if (selectAttributes == null || selectAttributes.isEmpty()) {
            // Default select all from root alias
            return "SELECT " + rootAlias;
        }
        // Build list of alias.field or alias.field AS aliasAs
        String selectFields = selectAttributes.stream()
                .map(attr -> {
                    String base = attr.getAlias() + "." + attr.getField();
                    return attr.getAliasAs() != null && !attr.getAliasAs().isEmpty()
                            ? base + " AS " + attr.getAliasAs()
                            : base;
                })
                .collect(Collectors.joining(", "));
        return "SELECT " + selectFields + " ";
    }

    private static String buildFromClause(String entityName, String rootAlias) {
        // Entity name assumed to be JPQL entity name (class name or mapped entity)
        // rootAlias default if empty
        String alias = (rootAlias == null || rootAlias.isEmpty())
                ? entityName.substring(0, 1).toLowerCase()
                : rootAlias;
        return "FROM " + capitalize(entityName) + " " + alias + " ";
    }

    private static String buildJoinClauses(String rootResource, String rootResourceAlias, List<JoinMeta> joins,
            Map<String, Map<String, XRelationshipMetadata>> graph) {
        if (joins == null || joins.isEmpty()) {
            return "";
        }

        StringBuilder joinBuilder = new StringBuilder();
        JoinMeta prevJoin = null;
        for (JoinMeta join : joins) {
            joinBuilder.append(" JOIN ")
                    .append(join.getResource())
                    .append(" ")
                    .append(join.getAlias())
                    .append(" ON ");

            // Add logic to process join on
            if ((join.getOn() == null || join.getOn().isEmpty()) && join.isAutoChain() && prevJoin != null) {
                // If autochain and prev join is not null , use the prev join entity as the
                // parent resource

                XRelationshipMetadata relationshipMetadata = findForeignkeyBiDirection(graph, join.getResource(),
                        prevJoin.getResource());
                if (relationshipMetadata == null) {
                    throw new XResourceException(
                            "Unable to find a foreign key between resource :"
                                    + join.getResource() + " and resource :" + prevJoin.getResource());
                } else {
                    if (join.getResource().equals(relationshipMetadata.getSourceTable())) {
                        joinBuilder.append(join.getAlias() + "." + relationshipMetadata.getForeignKeyField() + "="
                                + prevJoin.getAlias());
                    } else {
                        joinBuilder.append(prevJoin.getAlias() + "." + relationshipMetadata.getForeignKeyField() + "="
                                + join.getAlias());

                    }
                }
            } else if ((join.getOn() == null || join.getOn().isEmpty())
                    && (join.isAutoChain() && prevJoin == null || !join.isAutoChain())) {
                // If autochain and prev join is null , use the root resource as parent
                XRelationshipMetadata relationshipMetadata = findForeignkeyBiDirection(graph, rootResource,
                        join.getResource());
                if (relationshipMetadata == null) {
                    throw new XResourceException(
                            "Unable to find a foreign key between resource :"
                                    + rootResource + " and resource :" + join.getResource());
                } else {
                    joinBuilder.append(join.getAlias() + "." + relationshipMetadata.getForeignKeyField() + "="
                            + rootResourceAlias);
                }

            } else if ((join.getOn() == null || join.getOn().isEmpty()) && !join.isAutoChain()) {
                // If not autochain and getOn is emty try to relate with the root resource

            } else {
                if (join.getOn() != null && !join.getOn().isEmpty())
                    joinBuilder.append(join.getOn());
                else
                    throw new XResourceException(
                            "Unable to process intent, invalid join configuration between root resource :"
                                    + rootResource + " to child resource :" + join.getResource());
            }

            // Join filters appended as AND conditions in ON clause
            if (join.getFilters() != null && !join.getFilters().isEmpty()) {
                for (JoinFilterMeta filter : join.getFilters()) {
                    joinBuilder.append(" AND ")
                            .append(buildFilterPredicate(filter));
                }
            }
            joinBuilder.append(" ");
            prevJoin = join;
        }
        return joinBuilder.toString();
    }

    private static String buildWhereClause(IntentMeta intentMeta) {
        List<String> predicates = new ArrayList<>();

        // Collect join filters not included in join ON (optional, depends on design)
        // For now join filters handled in join ON

        // Explicit where clause from Intent (may include :param references)
        if (intentMeta.getWhere() != null && !intentMeta.getWhere().isEmpty()) {
            predicates.add(intentMeta.getWhere());
        }

        // Add filters from parameters if needed (example for future)

        return String.join(" AND ", predicates);
    }

    private static String buildFilterPredicate(JoinFilterMeta filter) {
        // Compose predicate string depending on binding type
        String field = filter.getField();
        String param = ":" + filter.getParam();
        switch (filter.getBinding()) {
            case EXACT:
                return field + " = " + param;
            case LIKE:
                return field + " LIKE " + param;
            case IN:
                return field + " IN " + param;
            case GREATER_THAN:
                return field + " > " + param;
            case LESS_THAN:
                return field + " < " + param;
            default:
                return field + " = " + param;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static XRelationshipMetadata findForeignkeyBiDirection(
            Map<String, Map<String, XRelationshipMetadata>> graph,
            String source, String target) {
        // Let's check from source to target
        if (graph.get(source) != null) {
            Map<String, XRelationshipMetadata> relationMap = graph.get(source);
            if (relationMap.get(target) != null) {
                // Found a foreign key from source to target
                return relationMap.get(target);
            }
        }
        if (graph.get(target) != null) {
            Map<String, XRelationshipMetadata> relationMap = graph.get(target);
            if (relationMap.get(source) != null) {
                // Found a foreign key from target to source
                return relationMap.get(source);
            }
        }
        return null;
    }
}
