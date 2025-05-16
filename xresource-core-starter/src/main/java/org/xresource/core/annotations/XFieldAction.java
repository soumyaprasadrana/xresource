package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.xresource.core.actions.XFieldControlledByAction;
import org.xresource.core.actions.XFieldControlledByActionAbsractImpl;

/**
 * Annotation used to define a field-level action that controls how a specific
 * field
 * in an entity is updated dynamically via a named action.
 * <p>
 * This is typically used in dynamic metadata-driven frameworks where field
 * updates
 * are triggered by declarative actions rather than direct setter calls.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XFieldAction {

    /**
     * The name of the action that triggers the update of this field.
     * This should be a unique and identifiable action key used in your system
     * to invoke the field update behavior.
     *
     * @return the name of the controlling action
     */
    String name();

    /**
     * A message template used to generate success or log messages when the action
     * is applied.
     * You may use placeholders like <code>{field}</code> and <code>{value}</code>
     * to inject dynamic content.
     *
     * Default: "Field '{field}' was updated to '{value}' successfully."
     *
     * @return the message template string
     */
    String message() default "Field '{}' was updated to '{}' successfully.";

    /**
     * The value to be set to the annotated field when the action is executed.
     * This can be a primitive value, enum name, or a JSON string representing an
     * object (to be parsed at runtime).
     *
     * @return the value to apply to the field
     */
    String value();

    /**
     * Optional arguments that define how to replace placeholders in the
     * {@link #message()}.
     * Typically this includes values like <code>action.field</code> and
     * <code>action.value</code>,
     * but can be extended as per the framework needs.
     *
     * @return array of placeholder argument keys
     */
    String[] messageArgs() default { "action.field", "action.value" };

    Class<? extends XFieldControlledByAction> actionClass() default XFieldControlledByActionAbsractImpl.class;

}
