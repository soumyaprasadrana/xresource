package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Prevents a repository or entity from being exposed as a dynamic REST endpoint
 * by the XResource framework.
 *
 * <p>
 * This annotation is useful when
 * {@code xresource.metadata.autoScanEnabled=true} (default behavior),
 * and a developer wants to explicitly opt out certain repository-backed
 * entities from being exposed via
 * CRUD endpoints.
 * </p>
 *
 * <p>
 * Even though the metadata for the annotated class may still be processed
 * (e.g., for embedded use),
 * no REST API will be generated for this resource.
 * </p>
 *
 * @apiNote Apply {@code @XResourceIgnore} to a repository or entity class when
 *          you want to
 *          exclude it from auto-exposed CRUD APIs, while still allowing
 *          metadata processing if required
 *          by other features like {@code @XResourceExposeAsEmbeddedResource} or
 *          {@code @XCronResource}.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XResourceIgnore {
}
