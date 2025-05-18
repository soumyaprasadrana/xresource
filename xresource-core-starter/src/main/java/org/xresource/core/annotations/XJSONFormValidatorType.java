package org.xresource.core.annotations;

/**
 * Enum representing different types of JSON form field validators
 * used by the XResource JSON form generator and runtime validation system.
 *
 * <p>
 * This enum is typically used in form metadata to specify
 * client-side and server-side validation rules for dynamically generated forms.
 *
 * @apiNote These validation types can be applied to fields using
 *          metadata-driven form
 *          generation. They help enforce input constraints such as required
 *          values,
 *          length limits, value bounds, regex patterns, and more.
 *
 * @author soumya
 * @since xresource-core 0.1
 */
public enum XJSONFormValidatorType {

    /**
     * Field must not be null or empty.
     */
    REQUIRED,

    /**
     * Field must contain a valid email address format.
     */
    EMAIL,

    /**
     * Field must not exceed the specified maximum character length.
     */
    MAX_LENGTH,

    /**
     * Field must contain at least the specified minimum number of characters.
     */
    MIN_LENGTH,

    /**
     * Field must match the specified regular expression.
     */
    REGEX,

    /**
     * Field's numeric value must not be less than the specified minimum.
     */
    MIN,

    /**
     * Field's numeric value must not be greater than the specified maximum.
     */
    MAX,

    /**
     * Field must match one of the predefined enum values.
     */
    ENUM,

    /**
     * Field value must be a date before the specified threshold.
     */
    DATE_BEFORE,

    /**
     * Field value must be a date after the specified threshold.
     */
    DATE_AFTER,

    /**
     * Field is read-only and should not be edited in the form.
     */
    READONLY
}
