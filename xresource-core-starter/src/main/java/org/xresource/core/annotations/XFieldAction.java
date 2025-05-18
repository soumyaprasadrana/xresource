package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.xresource.core.actions.XFieldControlledByAction;
import org.xresource.internal.actions.XFieldControlledByActionAbsractImpl;

/**
 * Declares a field-level action that governs how a specific entity field should
 * be updated through dynamic action execution.
 *
 * <p>
 * This annotation is used in conjunction with {@link XControlledByAction} on an
 * entity field to define one of potentially many actions that may apply to the
 * field.
 * It enables declarative behavior for status/state transitions or any business
 * logic
 * that needs to control a field dynamically.
 *
 * <p>
 * Each {@code @XFieldAction} defines:
 * <ul>
 * <li>An {@code action name} used to trigger the action via the API</li>
 * <li>A {@code value} to assign to the field when the action is executed</li>
 * <li>An optional {@code message} template to log or return on success</li>
 * <li>Optional {@code messageArgs} for placeholder substitution</li>
 * <li>An {@code actionClass} that defines how the action is executed</li>
 * </ul>
 *
 * <p>
 * By default, the associated action class is
 * {@link XFieldControlledByActionAbsractImpl},
 * which simply sets the defined {@link #value()} to the field at runtime using
 * reflection.
 * Advanced behavior (like conditional updates, dynamic value resolution, or
 * async triggers)
 * can be achieved by specifying a custom implementation of
 * {@link XFieldControlledByAction}.
 *
 * <p>
 * <b>Example Usage:</b>
 * 
 * <pre>
 * &#64;XControlledByAction(allowInsert = false, allowUpdate = false, actions = {
 *         &#64;XFieldAction(name = "approve", value = "APPROVED", message = "Status set to {action.value} by {action.name}."),
 *         &#64;XFieldAction(name = "reject", value = "REJECTED")
 * })
 * private String status;
 * </pre>
 *
 * @apiNote This annotation is part of the public API and is intended for
 *          developers
 *          using XResource to declaratively define action-driven field updates
 *          on entities.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XFieldAction {

    /**
     * The unique name of the action. This name is used to route
     * incoming dynamic action requests to this logic.
     *
     * @return the name/key of the controlling action
     */
    String name();

    /**
     * Message template shown/logged upon successful execution of this action.
     * Supports placeholders like <code>{field}</code> and <code>{value}</code>.
     * Defaults to: "Field '{field}' was updated to '{value}' successfully."
     *
     * @return the success message template
     */
    String message() default "Field '{}' was updated to '{}' successfully.";

    /**
     * The static value to be assigned to the field when this action is invoked.
     * Can represent primitive types, enum values, or serialized JSON.
     *
     * @return the value to apply
     */
    String value();

    /**
     * Placeholder argument keys to be injected into the message template.
     * These may be resolved from the context or runtime execution data.
     *
     * @return array of dynamic message argument keys
     */
    String[] messageArgs() default { "action.field", "action.value" };

    /**
     * The implementation class that defines the action execution logic.
     * By default, {@link XFieldControlledByActionAbsractImpl} is used,
     * which simply assigns the given {@link #value()} to the annotated field.
     *
     * @return the class implementing the field control logic
     */
    Class<? extends XFieldControlledByAction> actionClass() default XFieldControlledByActionAbsractImpl.class;
}
