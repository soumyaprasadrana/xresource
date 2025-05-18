package org.xresource.core.validation;

import org.xresource.core.exception.XValidationException;

/**
 * Functional interface representing a custom validator for entity objects.
 * <p>
 * Validators are responsible for performing custom validation logic
 * on a given entity during CREATE, UPDATE, or DELETE operations. If validation
 * fails, the validator adds violations to the provided
 * {@link ValidationContext}.
 * <p>
 * This interface can be implemented as a lambda, method reference, or concrete
 * class.
 * Validators are registered with the {@link XValidatorRegistry} to bind them to
 * specific entity classes.
 * <p>
 * <b>Typical Usage:</b>
 * <ul>
 * <li>Implement this interface in a Spring-managed component</li>
 * <li>Inject the {@code XValidatorRegistry}</li>
 * <li>Register your validator using {@code xValidatorRegistry.register(...)} in
 * a {@code @PostConstruct} method</li>
 * </ul>
 *
 * <pre>
 * {
 *     &#64;code
 *     &#64;Component
 *     public class UserPassValidator implements XValidator {
 *
 *         &#64;Autowired
 *         private XValidatorRegistry xValidatorRegistry;
 *
 *         &#64;PostConstruct
 *         public void addSelf() {
 *             xValidatorRegistry.register(User.class, this);
 *         }
 *
 *         @Override
 *         public void validate(Object entity, ValidationContext context) throws XValidationException {
 *             User user = (User) entity;
 *             if (user.getUserPass() == null || user.getUserPass().length() < 8) {
 *                 context.addViolation("userPass", "Password must be at least 8 characters.");
 *             }
 *         }
 *     }
 * }
 * </pre>
 *
 * @see ValidationContext
 * @see XValidatorRegistry
 * @see org.xresource.core.exception.XValidationException
 * @since xresource-core 0.1
 * @author soumya
 */
@FunctionalInterface
public interface XValidator {

    /**
     * Performs validation on the provided entity and collects any violations.
     *
     * @param entity  The entity to validate. Must be type-cast by implementors.
     * @param context The context object used to report validation failures.
     * @throws XValidationException optionally if the validator decides to throw
     *                              instead of adding violations to the context.
     */
    void validate(Object entity, ValidationContext context) throws XValidationException;
}
