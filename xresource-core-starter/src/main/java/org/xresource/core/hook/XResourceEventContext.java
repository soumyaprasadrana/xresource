package org.xresource.core.hook;

import org.xresource.core.model.XResourceMetadata;

import java.util.List;
import java.util.Map;

public class XResourceEventContext {

    private final Object resourceObject;
    private final XResourceMetadata metadata;
    private final List<String> userRoles;
    private final Map<String, Object> extra; 

    public XResourceEventContext(Object resourceObject,
                                 XResourceMetadata metadata,
                                 List<String> userRoles,
                                 Map<String, Object> extra) {
        this.resourceObject = resourceObject;
        this.metadata = metadata;
        this.userRoles = userRoles;
        this.extra = extra;
    }

    public Object getResourceObject() {
        return resourceObject;
    }

    public XResourceMetadata getMetadata() {
        return metadata;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public String getResourceName() {
        return metadata.getResourceName();
    }
}