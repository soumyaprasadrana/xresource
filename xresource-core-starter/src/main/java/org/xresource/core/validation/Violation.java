package org.xresource.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents a single validation failure on a specific field of an entity.
 * <p>
 * A {@code Violation} is created by an {@link XValidator} implementation when
 * it detects an issue with a field or value during a validation pass.
 * <p>
 * Each violation captures the field name, a human-readable message,
 * the rejected value (if applicable), the validator type that triggered it,
 * and optionally, the exception (e.g., {@code ConstraintViolationException})
 * that caused the failure.
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * context.addViolation("email", "Email must not be empty.", null, user.getEmail(), "NotNull");
 * }</pre>
 *
 * @author soumya
 * @see ValidationContext
 * @see XValidator
 * @since xresource-core 0.1
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Violation {

    /**
     * The name of the field where the violation occurred.
     */
    private final String field;

    /**
     * A user-friendly error message describing the violation.
     */
    private final String message;

    /**
     * (Optional) The exception that triggered this violation.
     */
    private Throwable exception;

    /**
     * (Optional) The actual value that failed validation.
     */
    private Object rejectedValue;

    /**
     * (Optional) The type or name of the validator that detected the violation
     * (e.g., "NotNull", "Email", or a custom validator name).
     */
    private String validatorType;
}
