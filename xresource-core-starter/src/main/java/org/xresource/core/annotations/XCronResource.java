package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a resource class (typically a repository or entity) as eligible for
 * scheduled or cron-based operations.
 *
 * <p>
 * This annotation allows metadata to be processed for resources that are not
 * exposed as standard CRUD APIs
 * but are intended to be accessed internally via scheduled tasks, batch jobs,
 * or background processes.
 * </p>
 *
 * <p>
 * It is especially useful in the following cases:
 * </p>
 * <ul>
 * <li>When {@code xresource.metadata.autoScanEnabled=false} — this enables
 * selective inclusion of specific
 * repositories for internal use without exposing them as REST endpoints.</li>
 * <li>When {@code xresource.metadata.autoScanEnabled=true} and a resource is
 * marked with {@link XResourceIgnore},
 * but still needs to participate in scheduled operations or background
 * processing.</li>
 * </ul>
 *
 * @apiNote Use {@code @XCronResource} to include a repository or resource in
 *          internal scheduling logic
 *          (e.g., cleanup jobs, recurring syncs, health checks) while skipping
 *          endpoint exposure.
 *          This ensures that metadata is available for job registration,
 *          tracking, and validation.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface XCronResource {
}
