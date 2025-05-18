package org.xresource.core.hook;

/**
 * Enumeration of resource lifecycle events that trigger hooks.
 * <p>
 * This enum defines the various events on which hooks can be invoked within the
 * XResource framework, allowing developers to plug in custom logic before or
 * after
 * core operations on entities such as create, update, and delete.
 * <p>
 * Hook implementations can listen to these events to perform additional
 * processing,
 * validations, logging, or other cross-cutting concerns related to the entity
 * lifecycle.
 *
 * @author soumya
 * @since cresource-core 0.1
 */
public enum XResourceEventType {

    /** Event fired before an entity is created in the system. */
    BEFORE_CREATE,

    /** Event fired after an entity has been successfully created. */
    AFTER_CREATE,

    /** Event fired before an existing entity is updated. */
    BEFORE_UPDATE,

    /** Event fired after an existing entity has been updated. */
    AFTER_UPDATE,

    /** Event fired before an entity is deleted from the system. */
    BEFORE_DELETE,

    /** Event fired after an entity has been deleted. */
    AFTER_DELETE
}
