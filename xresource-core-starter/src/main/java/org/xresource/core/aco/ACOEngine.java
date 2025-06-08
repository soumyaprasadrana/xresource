package org.xresource.core.aco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.xresource.core.logging.XLogger;
import org.xresource.internal.models.XFieldMetadata;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ACOEngine {

    private Map<String, Set<String>> relationshipGraph = new HashMap<String, Set<String>>();
    private XResourceMetadataRegistry registry;
    public static ACOEngine instance;
    private final XLogger log = XLogger.forClass(ACOEngine.class);
    private List<Set<String>> villageGroups;

    private ACOEngine(XResourceMetadataRegistry registry) {
        this.registry = registry;
    }

    public static ACOEngine getInstance(XResourceMetadataRegistry registry) {
        if (instance == null) {
            instance = new ACOEngine(registry);
            return instance;
        }
        return instance;
    }

    public void intialize() {
        log.enter("Initialize");
        log.info("Building the entity relationship graph...");
        buildGrapgh();
        this.villageGroups = groupVillages(3);

        System.out.println(this.villageGroups);

    }

    private void buildGrapgh() {
        for (Map.Entry<String, XResourceMetadata> entry : this.registry.getRegistry().entrySet()) {
            String sourceResource = entry.getKey();
            XResourceMetadata resMeta = entry.getValue();

            for (Map.Entry<String, XFieldMetadata> fieldEntry : resMeta.getFields().entrySet()) {
                XFieldMetadata fieldMeta = fieldEntry.getValue();

                if (fieldMeta.getForeignKeyRefTable() != null && !fieldMeta.getForeignKeyRefTable().isEmpty()) {
                    String targetResource = fieldMeta.getForeignKeyRefTable();
                    relationshipGraph.computeIfAbsent(sourceResource, k -> new HashSet<>());
                    relationshipGraph.get(sourceResource).add(targetResource);
                }
            }
        }
    }

    private List<Set<String>> groupVillages(int maxThresholdInAGroup) {
        Map<String, Set<String>> undirected = makeUndirected(relationshipGraph);
        List<Set<String>> rawGroups = findConnectedComponents(undirected);

        List<Set<String>> finalGroups = new ArrayList<>();
        List<Set<String>> pendingMergeGroups = new ArrayList<>();

        for (Set<String> group : rawGroups) {
            if (group.size() > maxThresholdInAGroup) {
                finalGroups.addAll(splitGroup(group, maxThresholdInAGroup));
            } else {
                pendingMergeGroups.add(group); // candidates to be merged
            }
        }

        // Now merge smaller groups into buckets until maxThreshold
        List<Set<String>> merged = mergeSmallGroups(pendingMergeGroups, maxThresholdInAGroup);
        finalGroups.addAll(merged);

        return finalGroups;
    }

    private List<Set<String>> mergeSmallGroups(List<Set<String>> smallGroups, int maxThreshold) {
        List<Set<String>> mergedGroups = new ArrayList<>();
        Set<String> temp = new HashSet<>();

        for (Set<String> group : smallGroups) {
            if (temp.size() + group.size() <= maxThreshold) {
                temp.addAll(group);
            } else {
                mergedGroups.add(new HashSet<>(temp));
                temp.clear();
                temp.addAll(group);
            }
        }

        if (!temp.isEmpty()) {
            mergedGroups.add(temp);
        }

        return mergedGroups;
    }

    private Map<String, Set<String>> makeUndirected(Map<String, Set<String>> graph) {
        Map<String, Set<String>> undirected = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String from = entry.getKey();
            Set<String> toSet = entry.getValue();

            undirected.putIfAbsent(from, new HashSet<>());
            for (String to : toSet) {
                undirected.get(from).add(to);

                undirected.putIfAbsent(to, new HashSet<>());
                undirected.get(to).add(from); // Add reverse link
            }
        }

        return undirected;
    }

    private List<Set<String>> findConnectedComponents(Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        List<Set<String>> components = new ArrayList<>();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                Set<String> component = new HashSet<>();
                Queue<String> queue = new LinkedList<>();
                queue.add(node);
                visited.add(node);

                while (!queue.isEmpty()) {
                    String current = queue.poll();
                    component.add(current);
                    for (String neighbor : graph.getOrDefault(current, Collections.emptySet())) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }

                components.add(component);
            }
        }

        return components;
    }

    private List<Set<String>> splitGroup(Set<String> group, int maxThreshold) {
        List<Set<String>> splitGroups = new ArrayList<>();
        Set<String> temp = new HashSet<>();

        for (String node : group) {
            temp.add(node);
            if (temp.size() == maxThreshold) {
                splitGroups.add(new HashSet<>(temp));
                temp.clear();
            }
        }

        if (!temp.isEmpty()) {
            splitGroups.add(new HashSet<>(temp));
        }

        return splitGroups;
    }

}
