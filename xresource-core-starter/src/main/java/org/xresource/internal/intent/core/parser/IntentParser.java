package org.xresource.internal.intent.core.parser;

import lombok.experimental.UtilityClass;
import org.xresource.core.intent.core.annotations.*;
import org.xresource.internal.intent.core.parser.model.*;
import org.xresource.internal.exception.XInvalidConfigurationException;
import org.xresource.internal.models.XResourceMetadata;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse @Intent and related annotations from a resource/entity
 * class and convert them into structured metadata model objects keyed by intent
 * name.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@UtilityClass
public class IntentParser {

    /**
     * Parses the given class for @Intent or @Intents annotations,
     * converts them into IntentMeta model objects and returns a map
     * keyed by intent name.
     * 
     * @param clazz annotated class
     * @return map of intent name to IntentMeta (empty if none found)
     */
    public static Map<String, IntentMeta> parseIntentMetadataMap(Class<?> clazz, XResourceMetadata metadata) {
        Map<String, IntentMeta> result = new LinkedHashMap<>();

        // Check if class is annotated with @Intent directly
        Intent singleIntent = clazz.getAnnotation(Intent.class);
        if (singleIntent != null) {
            IntentMeta meta = parseSingleIntent(singleIntent, clazz, metadata);
            result.put(meta.getName(), meta);
        }

        // Check for container annotation @Intents (multiple)
        Intents container = clazz.getAnnotation(Intents.class);
        if (container != null) {
            for (Intent intent : container.value()) {
                IntentMeta meta = parseSingleIntent(intent, clazz, metadata);
                result.put(meta.getName(), meta);
            }
        }

        return result;
    }

    private static IntentMeta parseSingleIntent(Intent intent, Class<?> clazz,
            XResourceMetadata metadata) {
        IntentMeta meta = new IntentMeta();
        meta.setName(intent.name());
        meta.setDescription(intent.description());
        meta.setRootAlias(resolveRootAlias(intent, clazz));
        meta.setPaginated(intent.paginated());
        meta.setLimit(intent.limit());
        meta.setEntityClass(metadata.getEntityClass());
        meta.setRootResource(metadata.getResourceName());

        Map<String, String> resourceAliasMap = new HashMap<String, String>();
        resourceAliasMap.put(metadata.getResourceName(), meta.getRootAlias());
        // Parse joins
        List<JoinMeta> joinMetas = new ArrayList<>();
        for (Join join : intent.joins()) {
            JoinMeta joinMeta = new JoinMeta();
            joinMeta.setResource(join.resource());
            String joinAlias = resolveJoinAlias(join);
            if (!resourceAliasMap.containsKey(join.resource())) {
                resourceAliasMap.put(join.resource(), joinAlias);
            } else {
                // dupliacted resource found on the chain enforce manual alias
                if (join.alias().isEmpty()) {
                    throw new XInvalidConfigurationException("Invalid configuration in intent '" + intent.name() +
                            "': Resource '" + join.resource() + "' is joined multiple times without a unique alias. " +
                            "You must specify an explicit alias for each occurrence of a resource that is joined more than once.");

                } else {
                    resourceAliasMap.put(join.resource() + "." + joinAlias, joinAlias);
                }

            }

            joinMeta.setAlias(joinAlias);
            joinMeta.setOn(join.on());
            joinMeta.setAutoChain(join.autoChain());

            // Parse join filters
            List<JoinFilterMeta> filterMetas = new ArrayList<>();
            for (JoinFilter filter : join.filters()) {
                JoinFilterMeta fMeta = new JoinFilterMeta();
                fMeta.setField(filter.field());
                fMeta.setParam(filter.param());
                fMeta.setBinding(filter.binding());
                filterMetas.add(fMeta);
            }
            joinMeta.setFilters(filterMetas);
            joinMetas.add(joinMeta);
        }
        meta.setJoins(joinMetas);

        // Parse selectAttributes
        List<SelectAttributeMeta> selectMetas = new ArrayList<>();
        for (SelectAttribute sel : intent.selectAttributes()) {
            SelectAttributeMeta selMeta = new SelectAttributeMeta();
            String alias = resolveSelectAlias(sel, resourceAliasMap, intent.name(), meta.getRootAlias(), metadata);
            selMeta.setAlias(alias);
            String field = sel.field();
            if (field.contains(".")) {
                String[] fieldNameArray = field.split("\\.");
                selMeta.setField(fieldNameArray[1]);
            } else {
                selMeta.setField(sel.field());
            }
            selMeta.setAliasAs(sel.aliasAs());
            selectMetas.add(selMeta);
        }
        meta.setSelectAttributes(selectMetas);

        // Parse parameters
        List<IntentParameterMeta> paramMetas = new ArrayList<>();
        for (IntentParameter param : intent.parameters()) {
            IntentParameterMeta pMeta = new IntentParameterMeta();
            pMeta.setName(param.name());
            pMeta.setType(param.type());
            pMeta.setDefaultValue(param.defaultValue());
            pMeta.setSource(param.source());
            pMeta.setBinding(param.binding());
            paramMetas.add(pMeta);
        }
        meta.setParameters(paramMetas);

        meta.setWhere(
                processWhereClause(intent.where(), resourceAliasMap, meta.getRootAlias(), metadata, intent.name()));
        meta.setSortBy(processSortBy(intent.sortBy(), resourceAliasMap, metadata));
        meta.setGroupBy(List.of(intent.groupBy()));

        return meta;
    }

    private static List<String> processSortBy(String[] sortBy, Map<String, String> joinedResourceMap,
            XResourceMetadata resourceMeta) {
        List<String> result = new ArrayList<String>();
        for (String field : sortBy) {
            if (field.contains(".")) {
                String[] fieldArray = field.split("\\.");
                String resourceName = fieldArray[0];
                if (joinedResourceMap.containsKey(resourceName)) {
                    result.add(joinedResourceMap.containsKey(resourceName) + "." + fieldArray[1]);
                } else {
                    throw new XInvalidConfigurationException("sortBy field " + field + "is invalid. No resource "
                            + resourceName + " found on joined resources list.");
                }
            } else {
                if (resourceMeta.isFieldPresent(field)) {
                    result.add(joinedResourceMap.get(resourceMeta.getResourceName()) + "." + field);
                } else {
                    throw new XInvalidConfigurationException("sortBy field " + field + " is invalid.");
                }
            }
        }
        return result;
    }

    private static String processWhereClause(String whereClause, Map<String, String> joinedResourceMap,
            String rootAlias, XResourceMetadata resourceMeta, String intentName) {
        if (whereClause == null || whereClause.isBlank())
            return whereClause;

        StringBuilder processed = new StringBuilder();
        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)(\\.[a-zA-Z0-9_]+)?\\b\\s*(=|>|<|>=|<=|!=|LIKE)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(whereClause);

        int lastEnd = 0;

        while (matcher.find()) {
            // Append everything before match
            processed.append(whereClause, lastEnd, matcher.start());

            String resourceOrField = matcher.group(1); // e.g. "assetId" or "City"
            String subField = matcher.group(2); // e.g. ".name" or null
            String operator = matcher.group(3); // e.g. =, LIKE, etc.

            String replacement;

            if (subField == null) {
                // No dot, so it's a root-level field
                if (resourceMeta.isFieldPresent(resourceOrField)) {
                    replacement = rootAlias + "." + resourceOrField;
                } else {
                    throw new XInvalidConfigurationException(
                            "Invalid field `" + resourceOrField + "` in where clause of intent " + intentName);
                }
            } else {
                // Has a resource and field
                String resourceName = resourceOrField;
                String fieldPart = subField;

                String alias = joinedResourceMap.get(resourceName);
                if (alias == null) {
                    throw new XInvalidConfigurationException(
                            "Invalid resource `" + resourceName + "` in where clause of intent " + intentName);
                }
                replacement = alias + fieldPart;
            }

            processed.append(replacement).append(" ").append(operator);

            lastEnd = matcher.end();
        }

        // Append the rest of the clause
        processed.append(whereClause.substring(lastEnd));

        return processed.toString();
    }

    private static String resolveSelectAlias(SelectAttribute attr, Map<String, String> joinedResourceMap,
            String intentName, String rootResourceAlias, XResourceMetadata resourceMeta) {
        if (!attr.alias().isEmpty()) {
            boolean aliasFound = false;
            for (Entry<String, String> entry : joinedResourceMap.entrySet()) {
                if (entry.getValue().equals(attr.alias())) {
                    aliasFound = true;
                    break;
                }
            }
            if (!aliasFound) {
                throw new XInvalidConfigurationException("Invalid configuration while processing intent :" + intentName
                        + ". Alias defined for select attribute " + attr.field()
                        + " is not found in the joined resources list.");
            } else {
                return attr.alias();
            }
        } else {
            String fieldName = attr.field();
            if (fieldName.contains(".")) {
                String[] fieldNameArray = fieldName.split("\\.");
                if (fieldNameArray.length == 2) {
                    String resourceName = fieldNameArray[0]; // fieldName = ${resourceName}.fieldName
                    if (joinedResourceMap.get(resourceName) != null) {
                        return joinedResourceMap.get(resourceName);
                    } else {
                        throw new XInvalidConfigurationException("Invalid configuration while processing intent :"
                                + intentName + ". Resource defined in field for select attribute " + attr.field()
                                + " is not found in the joined resources list.");

                    }
                }
            } else {
                // No alias present, No resource name is in the field, checking if the field is
                // in root resource scope
                if (resourceMeta.isFieldPresent(fieldName)) {
                    return rootResourceAlias;
                } else {
                    throw new XInvalidConfigurationException("Invalid configuration while processing intent :"
                            + intentName + "." + "No alias defined for select attribute " + attr.field()
                            + ", and the field is not present in the root resource :" + resourceMeta.getResourceName());
                }
            }
        }
        return null;
    }

    private static String resolveJoinAlias(Join join) {
        if (!join.alias().isEmpty())
            return join.alias();
        else
            return join.resource().substring(0, 1) + "_" + (int) (Math.random() * 10000);
    }

    private static String resolveRootAlias(Intent intent, Class<?> clazz) {
        if (!intent.rootAlias().isBlank()) {
            return intent.rootAlias();
        }
        String name = clazz.getSimpleName();
        if (name.isEmpty())
            return "r";
        return name.substring(0, 1).toLowerCase() + "_" + (int) (Math.random() * 10000);
    }
}
