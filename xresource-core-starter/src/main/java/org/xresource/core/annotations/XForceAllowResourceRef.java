package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a field representing a reference to another entity should be
 * forcefully included during GET/read-time serialization,
 * even if it is marked with {@code @JsonIgnore} or {@code @JsonBackReference}.
 *
 * <p>
 * This annotation is specifically designed to support scenarios where:
 * <ul>
 * <li>The field refers to a lazy-loaded Hibernate proxy or another entity</li>
 * <li>You want to <b>ignore</b> the field during POST/PUT (write) operations to
 * prevent recursion or malformed payloads</li>
 * <li>But you <b>still want it available</b> during GET/read operations for
 * completeness in serialized API responses</li>
 * </ul>
 *
 * <p>
 * In such cases, use {@code @JsonIgnore} or {@code @JsonBackReference} to
 * suppress the field during deserialization (POST/PUT),
 * and combine that with {@code @XForceAllowResourceRef} to force-inject it into
 * the response serialization during GET.
 *
 * <p>
 * This is particularly useful in metadata-driven APIs like XResource where
 * object graph serialization and deserialization
 * behaviors must be explicitly controlled for dynamic resource rendering and
 * lifecycle compliance.
 *
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * &#64;ManyToOne(fetch = FetchType.LAZY)
 * &#64;JoinColumn(name = "team_id")
 * &#64;JsonIgnore
 * &#64;XForceAllowResourceRef
 * private Team team;
 * </pre>
 *
 * @apiNote This is a public API annotation in the XResource framework designed
 *          to
 *          bridge the serialization gap between write-time safety and read-time
 *          completeness.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface XForceAllowResourceRef {
}
