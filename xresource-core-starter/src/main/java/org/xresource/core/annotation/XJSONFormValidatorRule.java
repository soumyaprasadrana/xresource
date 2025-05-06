package org.xresource.core.annotation;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface XJSONFormValidatorRule {
    XJSONFormValidatorType type();
    String value() default "";
}
