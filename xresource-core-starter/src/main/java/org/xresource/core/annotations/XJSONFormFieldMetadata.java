package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to provide additional metadata for fields in dynamic JSON
 * form generation.
 * <p>
 * This is part of the metadata-driven form generation mechanism in XResource.
 * Fields annotated with this metadata can influence how they are labeled,
 * described, ordered, and conditionally included in the generated JSON form.
 *
 * @apiNote
 *          <strong>Form Inclusion Logic:</strong><br>
 *          By default, not all fields are included in a JSON form. A field is
 *          included
 *          only if:
 *          <ul>
 *          <li>It is not marked as <code>readonly</code>, where read-only
 *          means:</li>
 *          <ul>
 *          <li>Annotated with {@link XReadonly}</li>
 *          <li>Controlled by {@link XControlledByAction} with both
 *          <code>allowInsert=false</code> and
 *          <code>allowUpdate=false</code></li>
 *          <li>Declared with JPA's
 *          <code>@Column(insertable=false, updatable=false)</code></li>
 *          </ul>
 *          <li>AND it is either:</li>
 *          <ul>
 *          <li>Marked as required (e.g., via <code>@NotNull</code>)</li>
 *          <li>NOT annotated with {@link XHidden}</li>
 *          <li>Annotated with {@link XHidden} but this annotation's
 *          <code>includeInJSONForm=true</code> is set</li>
 *          </ul>
 *          </ul>
 *          This allows developers to override exclusion caused by
 *          <code>@XHidden</code>
 *          and force a field into the form using
 *          <code>includeInJSONForm = true</code>.
 *          <p>
 *          Additionally, clients can force inclusion of all fields during
 *          generation
 *          by specifying a wildcard field filter such as
 *          <code>fieldsCsv=*</code>.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface XJSONFormFieldMetadata {

    /**
     * Human-readable label used for rendering the field in a UI form.
     *
     * @return the display label for the field
     */
    String label() default "";

    /**
     * A short description or help text for the field.
     * Useful for tooltips or documentation in generated forms.
     *
     * @return the field description
     */
    String description() default "";

    /**
     * Defines the order of appearance in the generated form.
     * Lower values appear earlier. Defaults to -1 (unspecified).
     *
     * @return the display sequence number
     */
    int displaySeq() default -1;

    /**
     * Whether to force the inclusion of this field in the dynamic JSON form
     * even if it is marked hidden via {@link XHidden}.
     * <p>
     * This flag is only honored if the field is not {@code readonly}
     * due to annotation-based or column-based restrictions.
     *
     * @return true if the field should be explicitly included in the form
     */
    boolean includeInJSONForm() default true;
}
