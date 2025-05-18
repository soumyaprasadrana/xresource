package org.xresource.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

/**
 * Declares a scheduled cron task in the XResource Cron API framework.
 * <p>
 * This annotation is designed to be applied on classes that implement the
 * {@link org.xresource.core.cron.XJobTask} interface.
 * Annotated classes will be automatically discovered and scheduled by the
 * XResource Cron engine based on the provided cron expression.
 *
 * <h3>Usage Example:</h3>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Component
 *     @XCronJob(name = "cleanupInactiveSessions", cron = "0 0/30 * * * *", description = "Cleans up sessions inactive for more than 30 minutes", resource = "session", query = "inactiveSessions")
 *     public class SessionCleanupTask implements XJobTask {
 *         public void run(List<?> data) {
 *             // task logic here
 *         }
 *     }
 * }
 * </pre>
 *
 * <h3>Behavior:</h3>
 * <ul>
 * <li>The task class <strong>must</strong> implement
 * {@link org.xresource.core.cron.XJobTask}.</li>
 * <li>The task class <strong>must</strong> be a Spring-managed bean to support
 * dependency injection.</li>
 * <li>If the annotation is not present on a bean implementing {@code XJobTask},
 * the cron engine will skip it.</li>
 * <li>The {@code resource} and {@code query} fields enable data injection using
 * the {@code XQuery} API before execution.</li>
 * <li>To temporarily disable a cron job, simply remove or comment out the
 * {@code @XCronJob} annotation.</li>
 * </ul>
 *
 * @apiNote
 *          This annotation serves both as a scheduler and a metadata provider
 *          for cron tasks.
 *          It enables dynamic task registration, query-driven data injection,
 *          and clean separation of cron logic from runtime scheduling logic.
 *          The design favors annotation-based discovery over hard-coded
 *          registry configurations.
 *
 * @see org.xresource.core.cron.XJobTask
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XCronJob {

    /**
     * Unique name for the cron job.
     */
    String name();

    /**
     * Cron expression defining when the job should run.
     */
    String cron();

    /**
     * Optional human-readable description of the job.
     */
    String description() default "";

    /**
     * Optional resource name (typically an entity or table name).
     * Used for resolving data injection using {@code XQuery}.
     */
    String resource() default "";

    /**
     * Optional named query to execute before task execution.
     * Result of the query is passed as input to
     * {@link org.xresource.core.cron.XJobTask#run(List)}.
     */
    String query() default "";
}
