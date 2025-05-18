package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a field in an entity is exclusively or partially controlled
 * via dynamic actions
 * rather than standard create (POST) or update (PUT) operations.
 *
 * <p>
 * This annotation is applicable only to fields within an entity class. It is
 * used by the
 * XResource framework to enforce field-level access control based on actions
 * defined through
 * {@link XFieldAction}.
 *
 * <p>
 * The {@code allowInsert} and {@code allowUpdate} flags control whether this
 * field can be
 * set during resource creation or update via POST or PUT calls. If both flags
 * are set to {@code false},
 * the field is considered fully controlled by actions only. In such cases, the
 * framework checks
 * if the field is nullable or has a default value. If not, it may throw a
 * configuration error
 * at application startup, indicating an invalid setup.
 *
 * <p>
 * This is particularly useful for scenarios where a field (e.g.,
 * {@code status}) evolves through
 * a defined set of business actions (e.g., APPROVE, REJECT, UNDER_REVIEW). Each
 * such transition
 * should be described using an {@link XFieldAction}, allowing multiple actions
 * to be associated
 * with the same field.
 *
 * <p>
 * <b>Example usage:</b>
 * 
 * <pre>
 * &#64;XControlledByAction(allowInsert = false, allowUpdate = false, actions = {
 *         &#64;XFieldAction(name = "approve", value = "APPROVED"),
 *         &#64;XFieldAction(name = "reject", value = "REJECTED")
 * })
 * private String status;
 * </pre>
 *
 * @apiNote This annotation is part of the public API of the XResource framework
 *          and is intended
 *          for use in domain entity definitions where fields need to be
 *          governed through business actions
 *          rather than conventional CRUD input.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface XControlledByAction {

    /**
     * Indicates whether this field can be set during a POST (create) operation.
     * 
     * @return true if allowed during insert, false otherwise
     */
    boolean allowInsert() default false;

    /**
     * Indicates whether this field can be updated during a PUT (update) operation.
     * 
     * @return true if allowed during update, false otherwise
     */
    boolean allowUpdate() default false;

    /**
     * Defines the actions that can modify this field.
     * Each {@link XFieldAction} represents a valid transition or update to the
     * field
     * through a specific action endpoint.
     * 
     * @return array of applicable field actions
     */
    XFieldAction[] actions();
}
