package org.xresource.internal.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xresource.internal.models.XResourceMetadata;

public class XResourceMetadataRegistry {
    private final Map<String, XResourceMetadata> registry = new ConcurrentHashMap<>();

    // Stores resources those are enbled to be exposed only as a embedded
    // resource/foreign keys, no CRUD operations directly
    private final Map<String, XResourceMetadata> embeddedResourceregistry = new ConcurrentHashMap<>();

    // Stores those are marked as XResource ignore , whjich means not exposed
    // but enabled for ebing consumed via cron jobs, for example system level tables
    // like session etc
    private final Map<String, XResourceMetadata> cronResourceregistry = new ConcurrentHashMap<>();

    public void register(String tableName, XResourceMetadata metadata) {
        registry.put(tableName, metadata);
    }

    public XResourceMetadata get(String tableName) {
        return registry.get(tableName);
    }

    public boolean contains(String tableName) {
        return registry.containsKey(tableName);
    }

    public Map<String, XResourceMetadata> getRegistry() {
        return this.registry;
    }

    public void registerEmbeddedResource(String tableName, XResourceMetadata metadata) {
        embeddedResourceregistry.put(tableName, metadata);
    }

    public XResourceMetadata getEmbeddedResource(String tableName) {
        return embeddedResourceregistry.get(tableName);
    }

    public boolean containsEmbeddedResource(String tableName) {
        return embeddedResourceregistry.containsKey(tableName);
    }

    public Map<String, XResourceMetadata> getEmbeddedResourceRegistry() {
        return this.embeddedResourceregistry;
    }

    public void registerCronResource(String tableName, XResourceMetadata metadata) {
        cronResourceregistry.put(tableName, metadata);
    }

    public XResourceMetadata getCronResource(String tableName) {
        return cronResourceregistry.get(tableName);
    }

    public boolean containsCronResource(String tableName) {
        return cronResourceregistry.containsKey(tableName);
    }

    public Map<String, XResourceMetadata> getCronResourceRegistry() {
        return this.cronResourceregistry;
    }
}
