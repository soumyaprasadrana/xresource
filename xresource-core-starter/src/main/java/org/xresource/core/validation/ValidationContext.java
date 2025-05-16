package org.xresource.core.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationContext {

    public enum OperationType {
        CREATE, UPDATE, DELETE
    }

    private final OperationType operationType;
    private final List<Violation> violations = new ArrayList<>();

    public ValidationContext(OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void addViolation(String field, String message) {
        violations.add(new Violation(field, message));
    }

    public void addViolation(String field, String message, Throwable exception, Object rejectedValue,
            String validationType) {
        violations.add(new Violation(field, message, exception, rejectedValue, validationType));
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}
