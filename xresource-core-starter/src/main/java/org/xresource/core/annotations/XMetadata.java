package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to specify external JSON metadata for an entity's fields.
 *
 * <p>
 * This annotation allows defining extended metadata about entity fields
 * by referencing an external JSON file following a strict schema.
 * It can be applied on top of an entity class or its corresponding repository
 * class.
 *
 * <p>
 * During metadata scanning, the JSON file specified by {@code path} is loaded
 * and used to override or enrich field metadata defined by
 * {@link XJSONFormFieldMetadata} annotations.
 *
 * <p>
 * <strong>JSON Schema:</strong>
 * The referenced JSON file must conform to the following JSON Schema draft-07
 * format:
 * 
 * <pre>{@code
 * {
 *   "$schema": "http://json-schema.org/draft-07/schema#",
 *   "title": "XResource Entity JSON Schema",
 *   "description": "Schema format to define metadata for an entity/table used by XMetadata annotation",
 *   "type": "object",
 *   "required": ["table", "fields"],
 *   "properties": {
 *     "table": {
 *       "type": "string",
 *       "description": "Name of the table/entity"
 *     },
 *     "description": {
 *       "type": "string",
 *       "description": "Optional description of the entity"
 *     },
 *     "fields": {
 *       "type": "object",
 *       "description": "Map of field names to their metadata definitions",
 *       "patternProperties": {
 *         "^[a-zA-Z_][a-zA-Z0-9_]*$": {
 *           "type": "object",
 *           "required": ["type"],
 *           "properties": {
 *             "label": {
 *               "type": "string",
 *               "description": "Human-readable label for the field (used in UI)"
 *             },
 *             "description": {
 *               "type": "string",
 *               "description": "Detailed description of the field"
 *             },
 *             "type": {
 *               "type": "string",
 *               "enum": ["string", "integer", "boolean", "number", "date"],
 *               "description": "Data type of the field"
 *             },
 *             "required": {
 *               "type": "boolean",
 *               "default": false,
 *               "description": "Whether this field is mandatory"
 *             },
 *             "format": {
 *               "type": "string",
 *               "enum": ["date", "date-time", "email", "uuid"],
 *               "description": "Special format constraints if any"
 *             },
 *             "includeinjsonform": {
 *               "type": "boolean",
 *               "default": false,
 *               "description": "Whether to include this field in JSON form"
 *             },
 *             "displayseq": {
 *               "type": "number",
 *               "description": "Field display sequence for JSON form"
 *             },
 *             "default": {
 *               "type": ["string", "number", "boolean", "null"],
 *               "description": "Default value for the field"
 *             },
 *             "enum": {
 *               "type": "array",
 *               "description": "Allowed values for this field",
 *               "items": {
 *                 "type": "string"
 *               }
 *             },
 *             "primaryKey": {
 *               "type": "boolean",
 *               "default": false,
 *               "description": "Marks this field as a primary key"
 *             },
 *             "autoIncrement": {
 *               "type": "boolean",
 *               "default": false,
 *               "description": "Marks if this field auto-increments"
 *             },
 *             "foreignKey": {
 *               "type": "object",
 *               "description": "Foreign key reference details",
 *               "required": ["table", "column"],
 *               "properties": {
 *                 "table": { "type": "string" },
 *                 "column": { "type": "string" },
 *                 "onDelete": {
 *                   "type": "string",
 *                   "enum": ["CASCADE", "SET NULL", "RESTRICT", "NO ACTION"]
 *                 },
 *                 "onUpdate": {
 *                   "type": "string",
 *                   "enum": ["CASCADE", "SET NULL", "RESTRICT", "NO ACTION"]
 *                 }
 *               },
 *               "additionalProperties": false
 *             }
 *           },
 *           "additionalProperties": false
 *         }
 *       },
 *       "additionalProperties": false
 *     }
 *   },
 *   "additionalProperties": false
 * }
 * }</pre>
 *
 * @apiNote
 *          Use this annotation when you want to externally define or override
 *          field metadata instead of or in addition to Java annotations.
 *          This enables flexible metadata-driven form generation, validation,
 *          and UI rendering based on external JSON definitions.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XMetadata {

    /**
     * Path to the JSON file containing field metadata for the entity.
     * This should be a resource path accessible on the classpath or filesystem.
     *
     * @return the JSON metadata file path
     */
    String path();
}
