package org.xresource.internal.util;

import java.util.*;

import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XRelationshipMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;

public class XResourceGraphBuilder {

    private static Map<String, Map<String, XRelationshipMetadata>> cachedGraph;

    private XResourceGraphBuilder() {
    }

    public static synchronized Map<String, Map<String, XRelationshipMetadata>> getGraph(
            XResourceMetadataRegistry registry) {
        if (cachedGraph == null) {
            cachedGraph = buildGraph(registry);
        }
        return cachedGraph;
    }

    private static Map<String, Map<String, XRelationshipMetadata>> buildGraph(XResourceMetadataRegistry registry) {
        Map<String, Map<String, XRelationshipMetadata>> graph = new HashMap<>();

        for (Map.Entry<String, XResourceMetadata> entry : registry.getRegistry().entrySet()) {
            String sourceResource = entry.getKey();
            XResourceMetadata resMeta = entry.getValue();

            for (Map.Entry<String, XFieldMetadata> fieldEntry : resMeta.getFields().entrySet()) {
                XFieldMetadata fieldMeta = fieldEntry.getValue();

                if (fieldMeta.getForeignKeyRefTable() != null && !fieldMeta.getForeignKeyRefTable().isEmpty()) {
                    String targetResource = fieldMeta.getForeignKeyRefTable();

                    XRelationshipMetadata relationship = new XRelationshipMetadata(
                            sourceResource,
                            targetResource,
                            fieldEntry.getKey(),
                            fieldMeta.getForeignKeyColumn(),
                            fieldMeta.getForeignKeyRefField(),
                            fieldMeta.getForeignKeyRefColumn(),
                            fieldMeta.isCompositeForeignKey(),
                            fieldMeta.getCompositeForeignKeyMap());

                    graph.computeIfAbsent(sourceResource, k -> new HashMap<String, XRelationshipMetadata>())
                            .put(targetResource, relationship);
                }
            }
        }

        return graph;
    }

    public static synchronized void reset() {
        cachedGraph = null;
    }
}
