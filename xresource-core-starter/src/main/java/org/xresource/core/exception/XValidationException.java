package org.xresource.core.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.xresource.core.validation.ValidationContext;
import org.xresource.core.validation.Violation;

public class XValidationException extends RuntimeException {

    private final List<Violation> violations;

    public XValidationException(ValidationContext context) {
        super("Violations: " +
                context.getViolations().stream()
                        .map(v -> "[" + v.getField() + "] " + v.getMessage())
                        .collect(Collectors.joining("; ")));
        this.violations = context.getViolations();
    }

    public List<Violation> getViolations() {
        return violations;
    }
}
