package org.xresource.core.intent.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container annotation to allow multiple @Intent annotations on the same
 * resource/entity.
 * 
 * Usage:
 * 
 * <pre>
 *  &#64;Intents({
 *      &#64;Intent(name = "intent1", ...),
 *      @Intent(name = "intent2", ...)
 *  })
 * </pre>
 * 
 * This enables declaring multiple complex queries within the same class.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Intents {
    /**
     * Array of Intent annotations.
     * 
     * @return array of intents
     */
    Intent[] value();
}
