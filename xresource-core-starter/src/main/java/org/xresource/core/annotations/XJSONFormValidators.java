package org.xresource.core.annotations;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface XJSONFormValidators {
    XJSONFormFieldValidaor[] value();
}
