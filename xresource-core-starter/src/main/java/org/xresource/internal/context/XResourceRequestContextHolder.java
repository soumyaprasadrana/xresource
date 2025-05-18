package org.xresource.internal.context;

public class XResourceRequestContextHolder {

    private static final ThreadLocal<XResourceRequestContext> CONTEXT = new ThreadLocal<>();

    private XResourceRequestContextHolder() {
        // Prevent instantiation
    }

    public static void set(XResourceRequestContext context) {
        CONTEXT.set(context);
    }

    public static XResourceRequestContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
