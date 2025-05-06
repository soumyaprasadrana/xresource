package org.xresource.core.hook;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class XResourceHookRegistry {

    private final Map<String, Map<XResourceEventType, List<XResourceHook>>> registry = new ConcurrentHashMap<>();

    public void registerHook(String resourceName, XResourceEventType eventType, XResourceHook hook) {
        registry
            .computeIfAbsent(resourceName.toLowerCase(), k -> new EnumMap<>(XResourceEventType.class))
            .computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(hook);
    }

    public List<XResourceHook> getHooks(String resourceName, XResourceEventType eventType) {
        return registry
            .getOrDefault(resourceName.toLowerCase(), Collections.emptyMap())
            .getOrDefault(eventType, Collections.emptyList());
    }

    public void executeHooks(String resourceName, XResourceEventType eventType, XResourceEventContext context) {
        for (XResourceHook hook : getHooks(resourceName, eventType)) {
            hook.execute(context);
        }
    }
}
