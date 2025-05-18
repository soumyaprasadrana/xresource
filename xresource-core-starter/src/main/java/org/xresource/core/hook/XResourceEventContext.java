package org.xresource.core.hook;

import java.util.List;
import java.util.Map;

/**
 * Context object passed to XResource hooks during lifecycle event processing.
 * <p>
 * Provides access to the resource instance being acted upon, its metadata,
 * current user roles, and additional contextual data for the hook execution.
 *
 * <p>
 * Hooks can utilize this context to make decisions or modify behavior based
 * on entity state, permissions, or extra runtime data.
 *
 * @author soumya
 * @since xresource-core 0.1
 */
public class XResourceEventContext {

    private final Object resourceObject;
    private final String resourceName;
    private final List<String> userRoles;
    private final Map<String, Object> extra;

    /**
     * Constructs a new event context for a resource lifecycle hook.
     *
     * @param resourceObject the resource entity instance involved in the event
     * @param metadata       metadata information about the resource type
     * @param userRoles      roles associated with the current user triggering the
     *                       event
     * @param extra          additional key-value pairs providing custom contextual
     *                       info
     */
    public XResourceEventContext(Object resourceObject,
            String resourceName,
            List<String> userRoles,
            Map<String, Object> extra) {
        this.resourceObject = resourceObject;
        this.resourceName = resourceName;
        this.userRoles = userRoles;
        this.extra = extra;
    }

    /**
     * Returns the resource object involved in the lifecycle event.
     *
     * @return the resource entity instance
     */
    public Object getResourceObject() {
        return resourceObject;
    }

    /**
     * Returns the list of user roles relevant for this event.
     *
     * @return list of user role names
     */
    public List<String> getUserRoles() {
        return userRoles;
    }

    /**
     * Returns any extra context data associated with the event.
     *
     * @return map of additional context key-value pairs
     */
    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * Returns the canonical resource name for this event context.
     *
     * @return resource name string
     */
    public String getResourceName() {
        return resourceName;
    }
}
