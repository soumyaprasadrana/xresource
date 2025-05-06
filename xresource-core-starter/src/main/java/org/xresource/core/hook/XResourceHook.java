package org.xresource.core.hook;

@FunctionalInterface
public interface XResourceHook {
    void execute(XResourceEventContext context);
}