package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface XJSONFormFieldMetadata {

    String label() default "";

    String description() default "";

    int displaySeq() default -1;

    boolean includeInJSONForm() default true;

}
