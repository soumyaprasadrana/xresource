package org.xresource.internal.cron;

import org.xresource.core.cron.XJobTask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XRegisteredJob {
    private final String name;
    private final String cron;
    private final String description;
    private final String resource;
    private final String query;
    private final XJobTask task;

    public XRegisteredJob(String name, String cron, String description, String resource, String query, XJobTask task) {
        this.name = name;
        this.cron = cron;
        this.description = description;
        this.resource = resource;
        this.query = query;
        this.task = task;
    }
}
