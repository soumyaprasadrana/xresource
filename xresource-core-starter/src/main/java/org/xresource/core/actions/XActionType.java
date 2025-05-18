package org.xresource.core.actions;

/**
 * Enum representing the supported HTTP method types for a custom
 * {@code @XAction}
 * within the XResource framework.
 * <p>
 * These action types correspond to standard HTTP operations that can be
 * selectively
 * handled by an {@link XResourceAction} implementation.
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * Used to indicate which HTTP method a custom action should respond to.
 * For example, if an action is only applicable to POST, its type can be set
 * accordingly.
 *
 * @apiNote This enum is primarily used internally by the framework to route
 *          incoming requests to the appropriate handler method in an
 *          {@link XActionAbstractImpl} subclass.
 *
 * @author soumya
 * @since xresource-core 0.1
 */
public enum XActionType {
    GET,
    POST,
    PUT,
    DELETE;
}
