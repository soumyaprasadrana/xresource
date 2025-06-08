# Feature: Validation Framework

This feature describes how `xresource-core-starter` supports robust, extensible validation for entities during lifecycle operations (create, update, delete). The validation framework is metadata-driven, integrates with standard annotations, and allows custom domain logic for business rules.

---

## Core Concepts

### 1. Validation Lifecycle

- **ValidationContext**  
  A container for validation state during an operation (CREATE, UPDATE, DELETE).  
  - Collects all detected `Violation` objects.
  - At the end of validation, if any violations are present, an `XValidationException` is thrown.
  - Enables programmatic and custom validator integration.

**Example Usage:**
```java
ValidationContext context = new ValidationContext(OperationType.CREATE);
if (entity.getEmail() == null) {
    context.addViolation("email", "Email cannot be null.");
}
if (context.hasViolations()) {
    throw new XValidationException(context);
}
```

### 2. Violation Reporting

- **Violation**  
  Represents a single validation failure:
  - Field name
  - Error message
  - Optional: exception, rejected value, validation rule type

- **XValidationException**  
  Thrown when violations are found. Wraps the list of violations for structured error reporting and logging.

**Example:**
```java
if (context.hasViolations()) {
    throw new XValidationException(context);
}
```

### 3. Declarative & Programmatic Validation

- **Standard Annotation Support**  
  Out-of-the-box support for JSR-380/Bean Validation annotations:
  - `@NotNull`, `@Size`, `@Min`, `@Max`, `@Pattern`, etc.
  - These are auto-wired into entity fields via the framework.

- **Custom Validator Interface (`XValidator`)**  
  Implement custom business logic for validation.
  - Validators receive the entity and the `ValidationContext` to record violations.
  - Register validators for entity classes.

**Example:**
```java
@Component
public class UserAuthorizationValidator implements XValidator {
    @Autowired
    private XValidatorRegistry registry;

    @PostConstruct
    public void addSelf() {
        registry.register(User.class, this);
    }

    @Override
    public void validate(Object entity, ValidationContext context) {
        User user = (User) entity;
        if (user.getAuthorization() == null) {
            context.addViolation("authorization", "Field 'authorization': required, cannot be null.", null, null, "NotNull");
        }
        if (context.hasViolations()) {
            throw new XValidationException(context);
        }
    }
}
```

### 4. Validation Utility

- **XValidationUtil**  
  Internal utility for registering field validators based on annotations.
  - Handles field accessibility, annotation inspection, and auto-registration.
  - Supports composite validation types (collections, strings, numbers).

---

## Relationships to Other Features

- **Resource & Metadata Management:**  
  Validation rules can be defined via metadata and annotations on resources.
- **Access Control:**  
  Field access restrictions can work in tandem with validation (e.g., only validate visible fields).
- **Event Hooks:**  
  Validation is invoked during entity lifecycle events (pre-insert, pre-update, etc.).

---

## File Locations

- `src/main/java/org/xresource/core/validation/`
- `src/main/java/org/xresource/internal/validation/`
- `src/main/java/org/xresource/core/exception/XValidationException.java`

---

## See Also

- [Resource & Metadata Management](../resource-metadata/README.md)
- [Action Handling](../action-handling/README.md)
- [Event Hooks & Extensibility](../hooks/README.md)

---

_This README is part of the feature-based documentation for xresource-core-starter. For more features, see the [features directory](../)._