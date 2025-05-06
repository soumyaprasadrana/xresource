package org.xresource.core.exception;

import java.util.List;

import org.xresource.core.validation.ValidationContext;

public class XValidationException extends RuntimeException {

    private final List<String> violations;

    public XValidationException(ValidationContext context) {
        super("Validation failed: " + String.join("; ", context.getViolations()));
        this.violations = context.getViolations();
    }

    public List<String> getViolations() {
        return violations;
    }
}

