package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container annotation for grouping multiple {@link XJSONFormFieldValidator}
 * annotations at the entity or repository class level.
 *
 * <p>
 * This enables attaching multiple field-specific validation rule sets to a
 * class.
 * Each field validator applies one or more {@link XJSONFormValidatorRule}s
 * to a named field.
 *
 * <p>
 * This annotation is not meant to be used directly on fields; it targets the
 * type level (i.e., class declarations).
 *
 * @apiNote
 *          This annotation serves as a wrapper to support multiple validators
 *          per entity.
 *          Example usage:
 * 
 *          <pre>
 * {@code
 * &#64;XJSONFormValidators({
 *     &#64;XJSONFormFieldValidator(
 *         name = "email",
 *         rules = {
 *             &#64;XJSONFormValidatorRule(type = XJSONFormValidatorType.REQUIRED),
 *             &#64;XJSONFormValidatorRule(type = XJSONFormValidatorType.EMAIL)
 *         }
 *     ),
 *     &#64;XJSONFormFieldValidator(
 *         name = "age",
 *         rules = {
 *             &#64;XJSONFormValidatorRule(type = XJSONFormValidatorType.MIN, value = "18"),
 *             &#64;XJSONFormValidatorRule(type = XJSONFormValidatorType.MAX, value = "99")
 *         }
 *     )
 * })
 * }
 *          </pre>
 *
 * @see XJSONFormFieldValidator
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XJSONFormValidators {

    /**
     * List of field validators defining validation rules for individual fields.
     *
     * @return array of field-level validators
     */
    XJSONFormFieldValidator[] value();
}
