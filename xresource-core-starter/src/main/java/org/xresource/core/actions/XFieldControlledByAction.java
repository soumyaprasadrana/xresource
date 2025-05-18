package org.xresource.core.actions;

import java.util.List;

import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Functional interface to define custom logic for controlling field-level
 * behavior
 * during the execution of an {@code XAction}.
 * <p>
 * This interface is intended to be used with the {@code @XFieldAction}
 * annotation,
 * allowing developers to implement dynamic field-handling logic beyond the
 * default
 * value assignment.
 *
 * <p>
 * <strong>Default Behavior:</strong>
 * </p>
 * By default, {@link XFieldControlledByActionAbsractImpl} is used, which sets
 * the
 * annotated field to the value resolved from the {@code value()} expression in
 * the
 * {@code @XFieldAction} annotation.
 *
 * <p>
 * <strong>Custom Behavior:</strong>
 * </p>
 * Implementations of this interface can define advanced workflows, validations,
 * or trigger auxiliary processes when a field value is changed via an action.
 * This is useful in scenarios such as state transitions, audit trails, or
 * reactive hooks.
 *
 * <pre>{@code
 * @XFieldAction(value = "'ACTIVE'", actionClass = StatusChangeAction.class)
 * private String status;
 *
 * public class StatusChangeAction implements XFieldControlledByAction {
 *     public Object apply(Object fieldValue, Object resourceEntity,
 *             HttpServletRequest request, HttpServletResponse response) {
 *         if ("ACTIVE".equals(fieldValue)) {
 *             // Example: trigger workflow when status becomes ACTIVE
 *             WorkflowEngine.trigger("onActivation", resourceEntity);
 *         }
 *         return fieldValue; // ensure the resolved value is returned
 *     }
 * }
 * }</pre>
 *
 * @apiNote Use this interface when field-level updates need to perform side
 *          effects
 *          such as triggering workflows, updating related entities, sending
 *          notifications,
 *          or performing custom validation. The `fieldValue` parameter is a
 *          primitive or
 *          resolved literal (e.g., String, Number, Boolean) from the
 *          `@XFieldAction#value()`.
 *
 * @see org.xresource.core.annotation.XFieldAction
 * @see XFieldControlledByActionAbsractImpl
 * @author soumya
 * @since xresource-core 0.1
 */

@FunctionalInterface
public interface XFieldControlledByAction {
    ResponseEntity<?> action(HttpServletRequest request, HttpServletResponse response, String resourceName,
            String fieldName,
            Object resourceEntity, Object inputEntity, List<String> roles, String actionSucessMessage);
}
