package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a resource for inclusion as an **embedded resource** in metadata,
 * without exposing it via direct CRUD endpoints.
 *
 * <p>
 * This annotation is typically used for entities that are referenced by other
 * XResources
 * (e.g., as `@ManyToOne` or nested relationships) but are not meant to be
 * independently
 * exposed as standalone REST endpoints.
 * </p>
 *
 * <p>
 * When applied, this ensures that the metadata of the associated entity is
 * scanned and
 * included in the system registry, enabling it to participate in:
 * </p>
 * <ul>
 * <li>OpenAPI schema generation (e.g., for nested request/response bodies)</li>
 * <li>Form generation or validation tooling</li>
 * <li>Dynamic DTO generation for relations</li>
 * </ul>
 *
 * <p>
 * Unlike {@link XResource}, this annotation does **not** expose CRUD endpoints.
 * </p>
 *
 * @apiNote Use this annotation when a repository should not be directly exposed
 *          via APIs
 *          but is still required as a dependent or embedded resource within
 *          other resources.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XResourceExposeAsEmbeddedResource {
}
