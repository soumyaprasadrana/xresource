package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a field as hidden from GET/read-time API responses, effectively
 * excluding
 * it from the serialized JSON output during response rendering.
 *
 * <p>
 * This annotation is typically used in dynamic resource-based frameworks where
 * metadata drives the structure of JSON responses. Fields marked with
 * {@code @XHidden}
 * will be excluded from output during GET calls but can still:
 * <ul>
 * <li>Be included in POST and PUT payloads for creation or updates</li>
 * <li>Appear in auto-generated dynamic forms (e.g., JSON forms for resource
 * creation)</li>
 * </ul>
 *
 * <p>
 * If you want to completely exclude the field from both serialization and
 * deserialization
 * (i.e., from all API interactions), consider using {@code @JsonIgnore} or
 * {@code @JsonBackReference}
 * from the Jackson library instead.
 *
 * <p>
 * <b>Use Cases:</b>
 * <ul>
 * <li>Internal audit fields like <code>internalNote</code>,
 * <code>tokenHash</code> etc. that
 * should not be exposed in read APIs but are still required for internal
 * input</li>
 * <li>Fields that are dynamically calculated or sensitive and only intended for
 * backend logic</li>
 * </ul>
 *
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * &#64;XHidden
 * private String internalToken;
 * </pre>
 *
 * @apiNote This is a public API annotation in the XResource framework, used to
 *          mark
 *          fields as output-hidden while still allowing them to participate in
 *          input forms
 *          and entity persistence.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface XHidden {
}
