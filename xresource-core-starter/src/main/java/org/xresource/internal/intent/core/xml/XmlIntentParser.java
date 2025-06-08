package org.xresource.internal.intent.core.xml;

import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xresource.core.intent.core.annotations.BindingType;
import org.xresource.core.intent.core.annotations.ParamSource;
import org.xresource.internal.intent.core.dsl.IntentDslCompiler;
import org.xresource.internal.intent.core.parser.IntentToJPQLTransformer;
import org.xresource.internal.intent.core.parser.model.*;
import org.xresource.internal.exception.XInvalidConfigurationException;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XRelationshipMetadata;
import org.xresource.internal.models.XResourceMetadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to validate XML <Intent> definitions and compile them into
 * metadata classes.
 * This enables a more tree-structured query definition approach than annotation
 * interfaces.
 *
 * @author soumya
 * @since xresource-core 0.2
 */
@UtilityClass
public class XmlIntentParser {

    private static final String SCHEMA_FILE = "/org/xresource/internal/intent/core/xml/intentSchema.xsd";

    /**
     * Validates and compiles an XML <Intent> element into an {@link IntentMeta}
     * object.
     *
     * @param intentElement the DOM Element representing the root <Intent>
     * @return compiled IntentMeta
     */
    public static IntentMeta compile(Element intentElement, String resourceName, XResourceMetadata resourceMeta) {
        validateAgainstSchema(intentElement);

        if (resourceName == null || resourceMeta == null) {
            throw new XInvalidConfigurationException("Invalid Intent! Unable to locate root resource");
        }

        IntentMeta meta = new IntentMeta();

        meta.setEntityClass(resourceMeta.getEntityClass());

        meta.setName(intentElement.getAttribute("name"));
        meta.setRootResource(resourceName);
        if (intentElement.getAttribute("resource") != null && !intentElement.getAttribute("resource").isEmpty()) {
            if (!intentElement.getAttribute("resource").equals(resourceName)) {
                throw new XInvalidConfigurationException(
                        "Requested resource and resource defined in intent for process mismatched.");
            }
        }
        meta.setDescription(intentElement.getAttribute("description"));

        // Resolve root alias
        String rootAlias = resolveRootAlias(intentElement, resourceMeta.getEntityClass());
        meta.setRootAlias(rootAlias);

        meta.setPaginated(Boolean.parseBoolean(intentElement.getAttribute("paginated")));
        meta.setLimit(parseInt(intentElement.getAttribute("limit"), 0));

        // Recursively process joins and JoinFilters
        meta.setJoins(
                XmlJoinParser.parseJoinsFromIntentElement(intentElement, meta));
        Map<String, String> resourceAliasMap = new HashMap<String, String>();
        resourceAliasMap.put(resourceMeta.getResourceName(), meta.getRootAlias());

        for (JoinMeta jmeta : meta.getJoins()) {
            resourceAliasMap.put(jmeta.getResource(), jmeta.getAlias());
        }

        meta.mergeSelectAttributes(parseSelectAttributes(intentElement, resourceAliasMap, meta.getName(),
                meta.getRootAlias(), resourceMeta));
        meta.setWhere(processWhereClause(intentElement.getAttribute("where"), resourceAliasMap, meta.getRootAlias(),
                resourceMeta, meta.getName()));
        meta.setParameters(parseIntentParameters(intentElement));
        meta.setSortBy(processSortBy(parseTextArrayByTag(intentElement, "sortBy").toArray(
                new String[0]), resourceAliasMap, resourceMeta));
        meta.setGroupBy(parseTextArrayByTag(intentElement, "groupBy"));

        return meta;
    }

    private static void validateAgainstSchema(Element element) {
        try {
            // Convert Element back to Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element copied = (Element) document.importNode(element, true);
            document.appendChild(copied);

            // Load schema
            InputStream schemaStream = XmlIntentParser.class.getResourceAsStream(SCHEMA_FILE);
            if (schemaStream == null) {
                throw new XInvalidConfigurationException("Schema file not found at: " + SCHEMA_FILE);
            }

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new javax.xml.transform.stream.StreamSource(schemaStream));
            Validator validator = schema.newValidator();
            validator.validate(new javax.xml.transform.dom.DOMSource(document));

        } catch (SAXException e) {
            e.printStackTrace();
            throw new XInvalidConfigurationException("XML validation error: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XInvalidConfigurationException("Error validating XML Intent element", e);
        }

    }

    private static List<SelectAttributeMeta> parseSelectAttributes(Element root, Map<String, String> joinedResourceMap,
            String intentName, String rootResourceAlias, XResourceMetadata resourceMeta) {
        List<SelectAttributeMeta> list = new ArrayList<>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE &&
                    "SelectAttribute".equals(node.getNodeName())) {
                // This is a direct child SelectAttribute element
                Element el = (Element) node;

                SelectAttributeMeta sel = new SelectAttributeMeta();
                sel.setField(el.getAttribute("field"));
                String selectAlias = resolveSelectAlias(sel, el, joinedResourceMap, intentName, rootResourceAlias,
                        resourceMeta);
                sel.setAlias(selectAlias);
                sel.setAliasAs(el.getAttribute("aliasAs"));
                list.add(sel);
            }
        }
        return list;
    }

    private static String resolveSelectAlias(
            SelectAttributeMeta sel, Element attr, Map<String, String> joinedResourceMap,
            String intentName, String rootResourceAlias, XResourceMetadata resourceMeta) {
        if (attr.getAttribute("alias") != null && !attr.getAttribute("alias").isEmpty()) {
            boolean aliasFound = false;
            for (Entry<String, String> entry : joinedResourceMap.entrySet()) {
                if (entry.getValue().equals(attr.getAttribute("alias"))) {
                    aliasFound = true;
                    break;
                }
            }
            if (!aliasFound) {
                throw new XInvalidConfigurationException("Invalid configuration while processing intent :" + intentName
                        + ". Alias defined for select attribute " + attr.getAttribute("alias")
                        + " is not found in the joined resources list.");
            } else {
                return attr.getAttribute("alias");
            }
        } else {
            String fieldName = attr.getAttribute("field");
            if (fieldName.contains(".")) {
                String[] fieldNameArray = fieldName.split("\\.");
                if (fieldNameArray.length == 2) {
                    String resourceName = fieldNameArray[0]; // fieldName = ${resourceName}.fieldName
                    if (joinedResourceMap.get(resourceName) != null) {
                        sel.setField(fieldNameArray[1]);
                        return joinedResourceMap.get(resourceName);
                    } else {
                        throw new XInvalidConfigurationException("Invalid configuration while processing intent :"
                                + intentName + ". Resource defined in field for select attribute " + attr
                                        .getAttribute("field")
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
                            + intentName + "." + "No alias defined for select attribute " + attr.getAttribute("field")
                            + ", and the field is not present in the root resource :" + resourceMeta.getResourceName());
                }
            }
        }
        return null;
    }

    private static List<IntentParameterMeta> parseIntentParameters(Element root) {
        List<IntentParameterMeta> list = new ArrayList<>();
        var paramBlocks = root.getElementsByTagName("parameters");
        if (paramBlocks.getLength() > 0) {
            Element block = (Element) paramBlocks.item(0);
            var params = block.getElementsByTagName("IntentParameter");
            for (int i = 0; i < params.getLength(); i++) {
                Element el = (Element) params.item(i);
                IntentParameterMeta p = new IntentParameterMeta();
                p.setName(el.getAttribute("name"));
                try {
                    p.setType(resolveType(el.getAttribute("type")));
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    p.setType(String.class);
                    e.printStackTrace();
                }
                p.setDefaultValue(el.getAttribute("defaultValue"));
                p.setSource(el.getAttribute("source").isEmpty() ? ParamSource.STATIC
                        : ParamSource.valueOf(el.getAttribute("source")));
                p.setBinding(el.getAttribute("binding").isEmpty() ? BindingType.EXACT
                        : BindingType.valueOf(el.getAttribute("binding")));
                list.add(p);
            }
        }
        return list;
    }

    private static List<String> parseTextArrayByTag(Element root, String tagName) {
        List<String> values = new ArrayList<>();
        var blocks = root.getElementsByTagName(tagName);
        if (blocks.getLength() > 0) {
            Element block = (Element) blocks.item(0);
            var children = block.getElementsByTagName("value");
            for (int i = 0; i < children.getLength(); i++) {
                values.add(children.item(i).getTextContent().trim());
            }
        }
        return values;
    }

    private static int parseInt(String value, int defaultVal) {
        try {
            return value != null && !value.isBlank() ? Integer.parseInt(value) : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static String resolveRootAlias(Element element, Class<?> clazz) {
        if (!element.getAttribute("rootAlias").isBlank()) {
            return element.getAttribute("rootAlias");
        }
        String name = clazz.getSimpleName();
        if (name.isEmpty())
            return "r";
        return name.substring(0, 1).toLowerCase() + "_" + (int) (Math.random() * 10000);
    }

    private static Class<?> resolveType(String typeString) throws ClassNotFoundException {
        if (typeString == null || typeString.isBlank()) {
            throw new IllegalArgumentException("Type string cannot be null or blank");
        }

        typeString = typeString.trim();

        // Special case: entity reference, e.g., @entity(com.example.MyEntity)
        if (typeString.startsWith("@entity(") && typeString.endsWith(")")) {
            String entityClass = typeString.substring(8, typeString.length() - 1).trim();
            return Class.forName(entityClass);
        }

        // Short names for primitive wrapper types and common types
        switch (typeString) {
            case "String":
                return String.class;
            case "Integer":
                return Integer.class;
            case "Long":
                return Long.class;
            case "Double":
                return Double.class;
            case "Float":
                return Float.class;
            case "Boolean":
                return Boolean.class;
            case "Character":
                return Character.class;
            case "Byte":
                return Byte.class;
            case "Short":
                return Short.class;
            case "BigDecimal":
                return java.math.BigDecimal.class;
            case "BigInteger":
                return java.math.BigInteger.class;
            case "Date":
                return java.util.Date.class;
            case "LocalDate":
                return java.time.LocalDate.class;
            case "LocalDateTime":
                return java.time.LocalDateTime.class;
            case "Instant":
                return java.time.Instant.class;
            default:
                // Assume it's a fully qualified class name
                return Class.forName(typeString);
        }
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

    public static void main(String args[]) {
        String iql = """
                Create Intent for resource Asset as assetComplexQuery
                    Description "Get asset details with project, manager, city, region filters"
                    Where "tag='TAG-2' AND City.name='City 4'"
                    Select assetId, tag, description
                    Include AssetUsage
                        Include Project
                            Select title as ProjectTitle
                            Include ProjectMember
                                Include Person
                                    Select name as managerName
                                    Include Address
                                        Include City
                                            Select name as cityName
                                            Include Region
                                                Add filter for name having exact value from parameter regionName
                                                Select name as regionName
                    Include AssetMaintenance
                        Select date as lastMaintenance
                        Include ProjectMember
                            Include Person
                                Select name as maintenanceOwner
                    Parameters
                        Param regionName with datatype String having default value "Region 5" from source static
                    Sort by assetId
                """;
        String secondIntent = """
                Create Intent for resource Asset as assetComplexQuery1
                    Description "Get asset details with project, manager, city, region filters"
                    Where "tag='TAG-2' AND City.name='City 4'"
                    Select assetId, tag, description
                    Include AssetMaintenance
                        Select date as lastMaintenance
                        Include ProjectMember
                            Include Person
                                Select name as maintenanceOwner
                    Parameters
                        Param regionName with datatype String having default value "Region 5" from source static
                    Sort by assetId
                    Include AssetUsage
                        Include Project
                            Select title as ProjectTitle
                                Include ProjectMember
                                    Include Person
                                        Select name as managerName
                                            Include Address
                                                Include City
                                                    Select name as cityName
                                                    Include Region
                                                        Add filter for name having exact value from parameter regionName
                                                        Select name as regionName
                """;
        System.out.println("--- Generated IQL String ---");
        System.out.println(iql);
        System.out.println("--------------------------");

        IntentDslCompiler compiler = new IntentDslCompiler();
        Element xml;
        try {
            xml = compiler.compile(secondIntent);
            System.out.println(printXmlElement(xml));
            String resourceName = xml.getAttribute("resource");
            ObjectMapper mapper = new ObjectMapper();
            String xFields = "{\"ownerOrganization\":{\"name\":\"ownerOrganization\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"object\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"owner_org_id\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":\"Organization\",\"foreignKeyColumn\":\"owner_org_id\",\"foreignKeyRefField\":\"orgId\",\"foreignKeyRefColumn\":\"orgId\",\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":true},\"assetLocations\":{\"name\":\"assetLocations\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"array\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"assetLocations\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false},\"assetId\":{\"name\":\"assetId\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"long\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"assetId\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false},\"description\":{\"name\":\"description\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"string\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"description\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false},\"assetUsages\":{\"name\":\"assetUsages\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"array\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"assetUsages\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false},\"tag\":{\"name\":\"tag\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"string\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"tag\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false},\"type\":{\"name\":\"type\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"string\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"type\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false},\"assetMaintenances\":{\"name\":\"assetMaintenances\",\"required\":false,\"readonly\":false,\"hidden\":false,\"includeinjsonform\":false,\"displaySeq\":0,\"type\":\"array\",\"format\":\"\",\"label\":null,\"dbColumnName\":\"assetMaintenances\",\"description\":null,\"defaultValue\":null,\"enumValues\":null,\"accessMap\":{},\"foreignKeyRefTable\":null,\"foreignKeyColumn\":null,\"foreignKeyRefField\":null,\"foreignKeyRefColumn\":null,\"compositeForeignKey\":false,\"compositeForeignKeyMap\":null,\"controlledByAction\":false,\"allowInsert\":true,\"allowUpdate\":true,\"foreignKey\":false}}";
            Map<String, XFieldMetadata> fields = mapper.readValue(
                    xFields,
                    new TypeReference<Map<String, XFieldMetadata>>() {
                    });
            XResourceMetadata meta = new XResourceMetadata();
            meta.setResourceName(resourceName);

            class Asset {
                private Long assetId;

                private String tag;
                private String description;
                private String type;
            }
            meta.setEntityClass(Asset.class);
            meta.setFields(fields);
            IntentMeta imeta = XmlIntentParser.compile(xml, resourceName, meta);
            String json = "{"
                    + "\"Project\":{\"Organization\":{\"sourceTable\":\"Project\",\"targetTable\":\"Organization\",\"foreignKeyField\":\"organization\",\"foreignKeyColumn\":\"org_id\",\"targetField\":\"orgId\",\"targetColumn\":\"orgId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"AssetLocation\":{\"Address\":{\"sourceTable\":\"AssetLocation\",\"targetTable\":\"Address\",\"foreignKeyField\":\"address\",\"foreignKeyColumn\":\"address_id\",\"targetField\":\"addressId\",\"targetColumn\":\"addressId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"Asset\":{\"sourceTable\":\"AssetLocation\",\"targetTable\":\"Asset\",\"foreignKeyField\":\"asset\",\"foreignKeyColumn\":\"asset_id\",\"targetField\":\"assetId\",\"targetColumn\":\"assetId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"Organization\":{\"Address\":{\"sourceTable\":\"Organization\",\"targetTable\":\"Address\",\"foreignKeyField\":\"address\",\"foreignKeyColumn\":\"address_id\",\"targetField\":\"addressId\",\"targetColumn\":\"addressId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"AssetUsage\":{\"Project\":{\"sourceTable\":\"AssetUsage\",\"targetTable\":\"Project\",\"foreignKeyField\":\"project\",\"foreignKeyColumn\":\"project_id\",\"targetField\":\"projectId\",\"targetColumn\":\"projectId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"Asset\":{\"sourceTable\":\"AssetUsage\",\"targetTable\":\"Asset\",\"foreignKeyField\":\"asset\",\"foreignKeyColumn\":\"asset_id\",\"targetField\":\"assetId\",\"targetColumn\":\"assetId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"ProjectMember\":{\"sourceTable\":\"AssetUsage\",\"targetTable\":\"ProjectMember\",\"foreignKeyField\":\"usedByProjectMember\",\"foreignKeyColumn\":\"used_by_pm_id\",\"targetField\":\"pmId\",\"targetColumn\":\"pmId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"Address\":{\"City\":{\"sourceTable\":\"Address\",\"targetTable\":\"City\",\"foreignKeyField\":\"city\",\"foreignKeyColumn\":\"city_id\",\"targetField\":\"cityId\",\"targetColumn\":\"cityId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"AssetMaintenance\":{\"Asset\":{\"sourceTable\":\"AssetMaintenance\",\"targetTable\":\"Asset\",\"foreignKeyField\":\"asset\",\"foreignKeyColumn\":\"asset_id\",\"targetField\":\"assetId\",\"targetColumn\":\"assetId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"ProjectMember\":{\"sourceTable\":\"AssetMaintenance\",\"targetTable\":\"ProjectMember\",\"foreignKeyField\":\"performedByProjectMember\",\"foreignKeyColumn\":\"performed_by_pm_id\",\"targetField\":\"pmId\",\"targetColumn\":\"pmId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"Asset\":{\"Organization\":{\"sourceTable\":\"Asset\",\"targetTable\":\"Organization\",\"foreignKeyField\":\"ownerOrganization\",\"foreignKeyColumn\":\"owner_org_id\",\"targetField\":\"orgId\",\"targetColumn\":\"orgId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"Region\":{\"Country\":{\"sourceTable\":\"Region\",\"targetTable\":\"Country\",\"foreignKeyField\":\"country\",\"foreignKeyColumn\":\"country_id\",\"targetField\":\"countryId\",\"targetColumn\":\"countryId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"City\":{\"Region\":{\"sourceTable\":\"City\",\"targetTable\":\"Region\",\"foreignKeyField\":\"region\",\"foreignKeyColumn\":\"region_id\",\"targetField\":\"regionId\",\"targetColumn\":\"regionId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"Person\":{\"Organization\":{\"sourceTable\":\"Person\",\"targetTable\":\"Organization\",\"foreignKeyField\":\"organization\",\"foreignKeyColumn\":\"org_id\",\"targetField\":\"orgId\",\"targetColumn\":\"orgId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"Address\":{\"sourceTable\":\"Person\",\"targetTable\":\"Address\",\"foreignKeyField\":\"address\",\"foreignKeyColumn\":\"address_id\",\"targetField\":\"addressId\",\"targetColumn\":\"addressId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"Person\":{\"sourceTable\":\"Person\",\"targetTable\":\"Person\",\"foreignKeyField\":\"supervisor\",\"foreignKeyColumn\":\"supervisor_id\",\"targetField\":\"personId\",\"targetColumn\":\"personId\",\"compositeMap\":null,\"composite\":false}},"
                    + "\"ProjectMember\":{\"Project\":{\"sourceTable\":\"ProjectMember\",\"targetTable\":\"Project\",\"foreignKeyField\":\"project\",\"foreignKeyColumn\":\"project_id\",\"targetField\":\"projectId\",\"targetColumn\":\"projectId\",\"compositeMap\":null,\"composite\":false},"
                    + "\"Person\":{\"sourceTable\":\"ProjectMember\",\"targetTable\":\"Person\",\"foreignKeyField\":\"person\",\"foreignKeyColumn\":\"person_id\",\"targetField\":\"personId\",\"targetColumn\":\"personId\",\"compositeMap\":null,\"composite\":false}}}"
                    + "}";

            Map<String, Map<String, XRelationshipMetadata>> graph = mapper.readValue(
                    json,
                    new TypeReference<Map<String, Map<String, XRelationshipMetadata>>>() {
                    });
            String jpql = IntentToJPQLTransformer.toJPQL(imeta, graph);
            System.out.println("JPQL=" + jpql);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String printXmlElement(Element element) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // Optional
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();

        } catch (TransformerException e) {
            throw new RuntimeException("Error while printing XML element", e);
        }
    }
}
