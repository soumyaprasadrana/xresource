package org.xresource.core.cron;

import java.util.List;

/**
 * Defines the contract for a cron-based job task in the XResource framework.
 * <p>
 * Implementations of this interface represent scheduled tasks that can be
 * automatically executed by the XResource Cron API engine based on cron
 * expressions provided via the {@link org.xresource.core.annotations.XCronJob}
 * annotation.
 *
 * <p>
 * <strong>Integration with XResource Cron:</strong><br>
 * Any Spring-managed bean that implements this interface and is annotated with
 * {@code @XCronJob} becomes a candidate for scheduled execution by the
 * framework.
 * The {@code @XCronJob} annotation provides the cron expression, job name,
 * and optional resource/query metadata for dynamic input injection.
 *
 * <p>
 * <strong>Lifecycle:</strong>
 * <ul>
 * <li>All Spring beans implementing {@code XJobTask} are scanned.</li>
 * <li>Only those annotated with {@code @XCronJob} are scheduled.</li>
 * <li>The {@code resource} and {@code query} attributes of {@code @XCronJob}
 * allow data to be dynamically resolved using the XQuery engine.</li>
 * <li>The resolved data is passed into the {@code run()} method as input.</li>
 * </ul>
 *
 * <p>
 * This separation of metadata (via {@code @XCronJob}) and execution logic
 * (via {@code XJobTask}) allows jobs to be toggled or reconfigured easily
 * without
 * modifying their core implementation.
 *
 * <p>
 * <strong>Example:</strong>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Component
 *     &#64;XCronJob(name = "cleanupInactiveUsers", cron = "0 0 2 * * ?", description = "Remove users inactive for over 90 days", resource = "user", query = "findInactiveUsers")
 *     public class CleanupJob implements XJobTask {
 *         @Override
 *         public void run(List<?> data) {
 *             List<User> inactiveUsers = (List<User>) data;
 *             inactiveUsers.forEach(userService::delete);
 *         }
 *     }
 * }
 * </pre>
 *
 * @see org.xresource.core.annotations.XCronJob
 * @author soumya
 * @since xresource 0.1
 */
public interface XJobTask {

    /**
     * Executes the job logic using data resolved from the configured resource and
     * query.
     *
     * @param data a list of input objects (usually entities or DTOs) fetched
     *             dynamically
     *             based on {@code resource} and {@code query} defined in the
     *             {@code @XCronJob}
     */
    void run(List<?> data);
}
