package org.xresource.core.model;

import java.util.*;

public class ForeignKeyTree {
    public final Map<String, ForeignKeyTree> children = new HashMap<>();

    public void addPath(List<String> path) {
        if (path.isEmpty()) return;
        String head = path.get(0);
        children.computeIfAbsent(head, k -> new ForeignKeyTree())
                .addPath(path.subList(1, path.size()));
    }

    public boolean hasChild(String key) {
        return children.containsKey(key);
    }

    public ForeignKeyTree getChild(String key) {
        return children.get(key);
    }

    public Set<String> getKeys() {
        return children.keySet();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }
}
