package org.xresource.core.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.xresource.core.validation.ValidationContext;
import org.xresource.core.validation.Violation;

/**
 * Exception thrown by the XResource Validation API when validation rules are
 * violated.
 * <p>
 * This exception is automatically raised when any registered
 * {@link org.xresource.core.validation.XValidator}
 * adds one or more {@link Violation} entries into a {@link ValidationContext}.
 * <p>
 * Validators are typically used to enforce domain-specific rules before create,
 * update, or delete
 * operations, and are automatically triggered as part of the XResource
 * framework's data processing pipeline.
 * <p>
 * The {@code XValidationException} wraps the complete list of validation
 * violations for the given operation,
 * and presents them both in structured form (via {@link #getViolations()}) and
 * as a readable message for
 * exception reporting or logging purposes.
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Component
 *     public class UserAuthorizationValidator implements XValidator {
 *
 *         &#64;Autowired
 *         private XValidatorRegistry xValidatorRegistry;
 *
 *         &#64;PostConstruct
 *         public void addSelf() {
 *             this.xValidatorRegistry.register(User.class, this);
 *         }
 *
 *         @Override
 *         public void validate(Object entity, ValidationContext context) throws XValidationException {
 *             User user = (User) entity;
 *             Authorization auth = user.getAuthorization();
 *             if (auth == null) {
 *                 context.addViolation("authorization", "Field 'authorization': required, cannot be null.", null, null,
 *                         "NotNull");
 *             }
 *             if (context.hasViolations()) {
 *                 throw new XValidationException(context);
 *             }
 *         }
 *     }
 * }
 * </pre>
 *
 * @author soumya
 * @see org.xresource.core.validation.XValidator
 * @see org.xresource.core.validation.ValidationContext
 * @see org.xresource.core.validation.Violation
 * @since xresource-core 0.1
 */
public class XValidationException extends RuntimeException {

    private final List<Violation> violations;

    /**
     * Constructs a new XValidationException using the given validation context.
     *
     * @param context the {@link ValidationContext} containing violations collected
     *                during validation
     */
    public XValidationException(ValidationContext context) {
        super("Violations: " +
                context.getViolations().stream()
                        .map(v -> "[" + v.getField() + "] " + v.getMessage())
                        .collect(Collectors.joining("; ")));
        this.violations = context.getViolations();
    }

    /**
     * Returns the list of {@link Violation} objects that triggered this exception.
     *
     * @return a list of validation violations
     */
    public List<Violation> getViolations() {
        return violations;
    }
}
