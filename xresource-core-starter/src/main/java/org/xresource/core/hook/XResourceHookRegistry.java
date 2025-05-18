package org.xresource.core.hook;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and executor for XResource lifecycle hooks.
 * <p>
 * Maintains a thread-safe registry of hooks keyed by resource name and event
 * type.
 * Allows dynamic registration and execution of hooks on resource events.
 * <p>
 * Hooks registered here are executed automatically by the framework at
 * appropriate
 * lifecycle stages.
 * 
 * @author soumya
 * @since xresource-core 0.1
 */
public class XResourceHookRegistry {

    private final Map<String, Map<XResourceEventType, List<XResourceHook>>> registry = new ConcurrentHashMap<>();

    /**
     * Registers a hook for the given resource name and event type.
     * 
     * @param resourceName canonical name of the resource (case insensitive)
     * @param eventType    the lifecycle event type
     * @param hook         the hook instance to register
     */
    public void registerHook(String resourceName, XResourceEventType eventType, XResourceHook hook) {
        registry
                .computeIfAbsent(resourceName.toLowerCase(), k -> new EnumMap<>(XResourceEventType.class))
                .computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(hook);
    }

    /**
     * Retrieves all hooks registered for a given resource and event type.
     * 
     * @param resourceName resource name (case insensitive)
     * @param eventType    lifecycle event type
     * @return list of registered hooks or empty list if none
     */
    public List<XResourceHook> getHooks(String resourceName, XResourceEventType eventType) {
        return registry
                .getOrDefault(resourceName.toLowerCase(), Collections.emptyMap())
                .getOrDefault(eventType, Collections.emptyList());
    }

    /**
     * Executes all hooks registered for the specified resource and event,
     * passing the event context to each hook.
     * 
     * @param resourceName resource name (case insensitive)
     * @param eventType    lifecycle event type
     * @param context      event context passed to hooks
     */
    public void executeHooks(String resourceName, XResourceEventType eventType, XResourceEventContext context) {
        for (XResourceHook hook : getHooks(resourceName, eventType)) {
            hook.execute(context);
        }
    }
}
