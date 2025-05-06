package org.xresource.core.cron;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XCronJob {
    String name();
    String cron();
    String description() default "";
    String resource() default "";
    String query() default "";
}
