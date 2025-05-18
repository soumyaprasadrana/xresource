package org.xresource.core.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds contextual validation state during an entity lifecycle operation
 * (such as create, update, or delete).
 * <p>
 * The {@code ValidationContext} acts as a container to collect all
 * {@link Violation} entries that are detected by custom
 * {@link XValidator} implementations for a given entity during
 * the execution of the validation phase.
 * <p>
 * At the end of validation, if any violations have been added,
 * an {@link org.xresource.core.exception.XValidationException} is
 * raised automatically by the framework or manually by the validator.
 *
 * <p>
 * <strong>Typical Flow:</strong>
 * <ul>
 * <li>Instantiate context with the current {@link OperationType}</li>
 * <li>Validators call {@link #addViolation} to record issues</li>
 * <li>Caller inspects {@link #hasViolations()} to decide whether to throw</li>
 * </ul>
 *
 * <p>
 * <strong>Example:</strong>
 * 
 * <pre>{@code
 * ValidationContext context = new ValidationContext(OperationType.CREATE);
 * if (entity.getEmail() == null) {
 *     context.addViolation("email", "Email cannot be null.");
 * }
 * if (context.hasViolations()) {
 *     throw new XValidationException(context);
 * }
 * }</pre>
 *
 * @author soumya
 * @see Violation
 * @see XValidator
 * @see org.xresource.core.exception.XValidationException
 * @since xresource-core 0.1
 */
public class ValidationContext {

    /**
     * Enum representing the type of operation being performed on the entity.
     */
    public enum OperationType {
        CREATE, UPDATE, DELETE
    }

    private final OperationType operationType;
    private final List<Violation> violations = new ArrayList<>();

    /**
     * Constructs a new {@code ValidationContext} for the specified operation type.
     *
     * @param operationType the type of operation being validated (e.g., create,
     *                      update)
     */
    public ValidationContext(OperationType operationType) {
        this.operationType = operationType;
    }

    /**
     * Returns the type of operation for which this validation is being performed.
     *
     * @return the {@link OperationType} (CREATE, UPDATE, DELETE)
     */
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * Adds a simple field-level violation with a message.
     *
     * @param field   the field name where the violation occurred
     * @param message the message describing the validation issue
     */
    public void addViolation(String field, String message) {
        violations.add(new Violation(field, message));
    }

    /**
     * Adds a detailed violation including exception, rejected value, and validation
     * rule type.
     *
     * @param field          the field name where the violation occurred
     * @param message        the message describing the validation issue
     * @param exception      the exception that triggered the violation (optional)
     * @param rejectedValue  the actual value that failed validation
     * @param validationType the type of validation (e.g., NotNull, Email,
     *                       CustomRule)
     */
    public void addViolation(String field, String message, Throwable exception, Object rejectedValue,
            String validationType) {
        violations.add(new Violation(field, message, exception, rejectedValue, validationType));
    }

    /**
     * Returns the list of all violations collected in this context.
     *
     * @return the list of {@link Violation} objects
     */
    public List<Violation> getViolations() {
        return violations;
    }

    /**
     * Returns {@code true} if one or more validation violations were recorded.
     *
     * @return {@code true} if validation failed; {@code false} otherwise
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}
