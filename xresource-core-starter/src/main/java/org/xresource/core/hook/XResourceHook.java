package org.xresource.core.hook;

/**
 * Functional interface representing a hook to be executed on a resource
 * lifecycle event.
 * <p>
 * Implementations perform custom logic triggered by events like create, update,
 * or delete
 * of resource entities.
 * <p>
 * The {@code execute} method receives an {@link XResourceEventContext}
 * providing
 * all necessary information about the event.
 *
 * @author soumya
 * @since xresource-core 0.1
 */
@FunctionalInterface
public interface XResourceHook {

    /**
     * Executes custom hook logic for a resource event.
     *
     * @param context event context containing resource instance, metadata, user
     *                roles, etc.
     */
    void execute(XResourceEventContext context);
}
