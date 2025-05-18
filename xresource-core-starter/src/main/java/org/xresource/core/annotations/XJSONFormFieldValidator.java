package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to define validation rules for a specific field in a dynamic
 * JSON form.
 *
 * <p>
 * <strong>Important:</strong> This annotation <em>cannot</em> be used
 * standalone.
 * It must be declared inside {@link XJSONFormValidators} to be detected by the
 * metadata scanner.
 *
 * <p>
 * Each instance associates a field name with one or more validation rules.
 *
 * @apiNote
 *          Example usage inside {@link XJSONFormValidators}:
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
 *     )
 * })
 * }
 *          </pre>
 *
 * @see XJSONFormValidatorRule
 * @see XJSONFormValidators
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XJSONFormFieldValidator {

    /**
     * The exact name of the entity field to which these validation rules apply.
     *
     * @return the target field name
     */
    String name();

    /**
     * Array of validation rules applied to the specified field.
     *
     * @return array of validation rules
     */
    XJSONFormValidatorRule[] rules();
}
