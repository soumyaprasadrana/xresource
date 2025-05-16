
package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;

@Retention(RUNTIME)
@Target({ FIELD, TYPE })
@Repeatable(XResourceAuthGroups.class)
public @interface XResourceAuthGroup {
    String role();

    AccessLevel access();
}