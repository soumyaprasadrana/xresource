package org.xresource.internal.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class XResourceRequestContext {

    private final HttpServletRequest request;
    private final String baseApiPath;

    private String httpMethod;
    private String fullUrl;
    private String resourceName;
    private String id;
    private String field;
    private String queryName;
    private String actionName;
    private String operationType;

    public XResourceRequestContext(HttpServletRequest request, String baseApiPath) {
        this.request = request;
        this.baseApiPath = baseApiPath != null ? baseApiPath : "/api/resources";
        this.httpMethod = request.getMethod();
        this.fullUrl = request.getRequestURL().toString();
        extractInfoFromPath();
    }

    private void extractInfoFromPath() {
        String fullPath = request.getRequestURI();
        String relativePath = fullPath.replaceFirst(baseApiPath, "");
        String[] parts = relativePath.split("/");
        List<String> segments = Arrays.stream(parts)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.toList());

        int len = segments.size();

        if (len >= 1) {
            if ("jsonform".equalsIgnoreCase(segments.get(0))) {
                this.operationType = "GENERATE_JSON_FORM";
                if (len >= 2)
                    this.resourceName = segments.get(1);
                return;
            }
            this.resourceName = segments.get(0);
        }

        switch (len) {
            case 1 -> {
                if ("GET".equalsIgnoreCase(httpMethod))
                    this.operationType = "FIND_ALL";
                if ("POST".equalsIgnoreCase(httpMethod))
                    this.operationType = "CREATE";
            }
            case 2 -> {
                this.id = segments.get(1);
                if ("GET".equalsIgnoreCase(httpMethod))
                    this.operationType = "FIND_BY_ID";
                if ("PUT".equalsIgnoreCase(httpMethod))
                    this.operationType = "UPDATE";
                if ("DELETE".equalsIgnoreCase(httpMethod))
                    this.operationType = "DELETE";
            }
            case 3 -> {
                if ("query".equalsIgnoreCase(segments.get(1))) {
                    this.queryName = segments.get(2);
                    this.operationType = "QUERY";
                } else if ("intents".equalsIgnoreCase(segments.get(1))) {
                    this.queryName = segments.get(2);
                    this.operationType = "QUERY";
                } else {
                    this.id = segments.get(1);
                    this.field = segments.get(2);
                    this.operationType = "FIELD_VALUE";
                }
            }
            case 4 -> {
                this.id = segments.get(1);
                if ("actions".equalsIgnoreCase(segments.get(2))) {
                    this.actionName = segments.get(3);
                    this.operationType = "ACTION";
                }
            }
        }
    }

}
