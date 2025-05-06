package org.xresource.core.cron;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class XJobRegistry {

    private final Map<String, XRegisteredJob> jobs = new HashMap<>();

    public void register(String name, XRegisteredJob job) {
        if (jobs.containsKey(name)) {
            throw new IllegalStateException("XJob with name '" + name + "' is already registered.");
        }
        jobs.put(name, job);
    }

    public Collection<XRegisteredJob> getAllJobs() {
        return jobs.values();
    }

    public Optional<XRegisteredJob> get(String name) {
        return Optional.ofNullable(jobs.get(name));
    }
}
