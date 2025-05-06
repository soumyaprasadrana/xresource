package org.xresource.core.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationContext {

    public enum OperationType {
        CREATE, UPDATE, DELETE
    }

    private final OperationType operationType;
    private final List<String> violations = new ArrayList<>();

    public ValidationContext(OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void addViolation(String message) {
        violations.add(message);
    }

    public List<String> getViolations() {
        return violations;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}
