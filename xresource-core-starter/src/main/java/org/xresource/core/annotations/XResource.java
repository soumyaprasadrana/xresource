package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a repository as an exposable XResource entity with associated CRUD
 * endpoints.
 *
 * <p>
 * This annotation allows fine-grained control over whether a given
 * {@code @Repository}-backed
 * entity should be exposed as a REST resource. When the global configuration
 * property
 * {@code xresource.metadata.autoScanEnabled} is set to {@code true} (default),
 * all repository
 * classes are automatically scanned and exposed as XResource endpoints unless
 * explicitly skipped
 * using {@link XResourceIgnore}.
 * </p>
 *
 * <p>
 * Even if a repository is not exposed for CRUD operations, its metadata can
 * still be registered
 * for use in:
 * </p>
 * <ul>
 * <li>{@link XResourceExposeAsEmbeddedResource} – for embedding as part of
 * another resource.</li>
 * <li>{@link XCronResource} – for scheduled or background processing use
 * cases.</li>
 * </ul>
 *
 * <p>
 * When {@code xresource.metadata.autoScanEnabled} is {@code false}, no
 * repositories are exposed
 * by default. Only those explicitly annotated with {@code @XResource},
 * {@code @XResourceExposeAsEmbeddedResource},
 * or {@code @XCronResource} will be processed.
 * </p>
 *
 *
 * @apiNote Apply this annotation on repository interfaces to explicitly enable
 *          CRUD API exposure.
 *          When used together with {@code XResourceIgnore}, metadata is
 *          preserved without generating endpoints.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XResource {

    /**
     * Name of the database table this resource is mapped to.
     * Used for metadata mapping and documentation purposes.
     */
    String table();
}
