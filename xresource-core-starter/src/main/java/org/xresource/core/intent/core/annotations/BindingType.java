package org.xresource.core.intent.core.annotations;

/**
 * Enum defining the binding style between IntentParameter values and query
 * fields.
 * 
 * - EXACT: exact match (e.g., field = :param)
 * - LIKE: partial match with SQL LIKE syntax (e.g., field LIKE :param)
 * - IN: field IN (:param) where param is a collection
 * - GREATER_THAN: field > :param
 * - LESS_THAN: field < :param
 * 
 * Additional binding types can be added as needed.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
public enum BindingType {
    EXACT,
    LIKE,
    IN,
    GREATER_THAN,
    LESS_THAN
}
