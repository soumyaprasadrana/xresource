package org.xresource.core.registry;

import org.springframework.stereotype.Component;
import org.xresource.core.model.XResourceMetadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component  // Marks the class as a Spring bean for DI
public class XResourceMetadataRegistry {
    private final Map<String, XResourceMetadata> registry = new ConcurrentHashMap<>();

    public void register(String tableName, XResourceMetadata metadata) {
        registry.put(tableName, metadata);
    }

    public XResourceMetadata get(String tableName) {
        return registry.get(tableName);
    }

    public boolean contains(String tableName) {
        return registry.containsKey(tableName);
    }
    public Map<String, XResourceMetadata> getRegistry(){
        return this.registry;
    }
}
