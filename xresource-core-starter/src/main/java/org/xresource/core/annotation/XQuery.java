package org.xresource.core.annotation;

import java.lang.annotation.*;

@Repeatable(XQueries.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XQuery {
    String name();
    String where();
    String[] contextParams() default {};
    boolean autoApply() default false;
    String[] appliesToRoles() default {"*"};
}
