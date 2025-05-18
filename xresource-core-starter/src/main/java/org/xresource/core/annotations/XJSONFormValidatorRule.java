package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to define a single client-side validation rule for dynamic JSON
 * form generation.
 * <p>
 * This annotation can be applied to either:
 * <ul>
 * <li>Entity classes – for attaching validation metadata directly to JPA
 * entities</li>
 * <li>Repository classes – to associate validation logic externally via
 * repository-level metadata</li>
 * </ul>
 *
 * <p>
 * These validation rules serve as UI-level constraints that help the frontend
 * validate form fields before submitting them to the backend. They are not
 * automatically enforced on the backend unless implemented separately.
 *
 * <p>
 * Multiple rules can be associated with a form through a container annotation
 * (e.g., {@code @XJSONFormValidatorRules}).
 *
 * @apiNote
 *          The {@code type()} indicates the type of validation, and
 *          {@code value()} holds any required
 *          constraint metadata:
 *          <ul>
 *          <li><b>REQUIRED</b> – No value needed</li>
 *          <li><b>MAX_LENGTH</b>, <b>MIN_LENGTH</b> – {@code value} specifies
 *          the length limit</li>
 *          <li><b>REGEX</b> – {@code value} defines a regular expression to
 *          match</li>
 *          <li><b>MIN</b>, <b>MAX</b> – {@code value} defines numeric
 *          boundaries</li>
 *          <li><b>ENUM</b> – {@code value} is a comma-separated list of allowed
 *          values</li>
 *          <li><b>DATE_BEFORE</b>, <b>DATE_AFTER</b> – {@code value} is an
 *          ISO-8601 date string</li>
 *          <li><b>READONLY</b> – Declares the field as non-editable</li>
 *          </ul>
 *          These rules help auto-generate validations for dynamic forms based
 *          on metadata.
 *
 * @see XJSONFormValidatorType
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XJSONFormValidatorRule {

    /**
     * The type of validation to apply.
     *
     * @return the validator type
     */
    XJSONFormValidatorType type();

    /**
     * The constraint value used by some validator types.
     * Ignored if not required for the given type.
     *
     * @return the validator parameter (e.g., max length, regex)
     */
    String value() default "";
}
