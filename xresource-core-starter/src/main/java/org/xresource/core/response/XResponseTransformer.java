package org.xresource.core.response;

@FunctionalInterface
public interface XResponseTransformer<T> {
    T tranform(T response, String resourceName);
}
