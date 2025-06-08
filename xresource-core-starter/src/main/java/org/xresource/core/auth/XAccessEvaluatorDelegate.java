package org.xresource.core.auth;

import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper bean for the {@link XRoleBasedAccessFunction} used in the XResource
 * access control system.
 * <p>
 * This delegate provides a Spring-managed indirection layer that allows
 * developers to override
 * and customize access evaluation logic globally by defining their own
 * implementation of
 * {@link XRoleBasedAccessFunction}.
 * </p>
 *
 * <p>
 * The framework internally relies on this delegate to resolve effective access
 * levels
 * after completing its built-in access checks. By registering a custom Spring
 * bean
 * of type {@code XRoleBasedAccessFunction}, the access logic can be extended or
 * modified
 * without changing core framework logic.
 * </p>
 *
 * <p>
 * Example:
 * 
 * <pre>{@code
 * &#64;Component
 * public class CustomAccessFunction implements XRoleBasedAccessFunction {
 *     public AccessLevel getEffectiveAccess(...) {
 *         // custom logic
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @see XRoleBasedAccessFunction
 * @see AccessLevel
 * @see XResourceTypes
 *
 * @author soumya
 * @since xresource-core 0.1
 */

@RequiredArgsConstructor
@Getter
public class XAccessEvaluatorDelegate {

    /**
     * The pluggable role-based access function used to compute effective access
     * after framework-level evaluation.
     */
    private final XRoleBasedAccessFunction xRoleBasedAccessFunction;
}
