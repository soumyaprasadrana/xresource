package org.xresource.core.query;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class XQueryContextProvider {

    public Map<String, Object> buildContext(Map<String, Object> userContext, Map<String, String> dynamicFilters) {
        Map<String, Object> merged = new HashMap<>();

        // Add user/system context first
        if (userContext != null) {
            merged.put("user",userContext);
        }

        // Then override or add dynamic filters
        if (dynamicFilters != null) {
            for (Map.Entry<String, String> entry : dynamicFilters.entrySet()) {
                merged.put(entry.getKey(), entry.getValue()); // Let filters override
            }
        }

        return merged;
    }

    public Map<String, Object> getEmptyContext() {
        Map<String, Object> merged = new HashMap<>();
        return merged;
    }
    private Map<String, Object> flattenNestedContext(Map<String, Object> input, String prefix) {
        Map<String, Object> flat = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = prefix + "." + entry.getKey();
            flat.put(key, entry.getValue());
        }
        return flat;
    }
}
