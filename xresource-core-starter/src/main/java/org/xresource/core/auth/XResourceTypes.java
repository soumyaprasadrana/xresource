package org.xresource.core.auth;

/**
 * Defines the types of resources that can be evaluated for access control
 * within the XResource framework.
 *
 * <p>
 * This enum helps categorize the level of access being checked,
 * such as whether it's for an entire entity (resource) or a specific field.
 * </p>
 *
 * <ul>
 * <li>{@link #ENTITY} - Refers to access at the resource (entity/class)
 * level.</li>
 * <li>{@link #FIELD} - Refers to access at the individual field/property
 * level.</li>
 * </ul>
 *
 * @apiNote This enum is used by access evaluators like
 *          {@link XRoleBasedAccessFunction}
 *          to determine the granularity of access being checked and allow
 *          fine-grained control.
 *
 * @since xresource-core 0.1
 * @author soumya
 */
public enum XResourceTypes {
    /**
     * Resource-level (entity or class) access.
     */
    ENTITY,

    /**
     * Field-level access within a resource.
     */
    FIELD
}
