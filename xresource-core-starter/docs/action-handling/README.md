# Feature: Action Handling

This feature group enables the definition, execution, and configuration of dynamic "actions" within the `xresource-core-starter` framework. Actions represent custom or business-specific operations on resources, beyond standard CRUD.

---

## Main Concepts

### 1. Entity/Repository-level Actions

- **Annotation:** `@XAction` (or `@XActions` for multiples)
- **Purpose:** Declare custom actions that can be triggered as REST endpoints for an entity or repository.
- **Key Attributes:**
  - `name`: Unique action identifier (used in endpoint path)
  - `type`: HTTP method (from `XActionType`: GET, POST, PUT, DELETE)
  - `actionBeanClass`: Class containing business logic (must extend `XActionAbstractImpl`)

#### Example

```java
@XAction(
    name = "approveUser",
    type = XActionType.POST,
    actionBeanClass = ApproveUserAction.class
)
public class UserEntity { ... }

// Implementation
public class ApproveUserAction extends XActionAbstractImpl {
    @Override
    public ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        // custom approval logic
        return ResponseEntity.ok("User approved");
    }
}
```

#### Multiple Actions

```java
@XActions({
    @XAction(name = "approve", type = XActionType.POST, actionBeanClass = ApproveAction.class),
    @XAction(name = "reject", type = XActionType.POST, actionBeanClass = RejectAction.class)
})
public class MyEntity { ... }
```

### 2. Field-level Controlled Actions

- **Annotation:** `@XControlledByAction` (on fields)
- **Purpose:** Mark fields that are updated exclusively via actions, not by ordinary create/update (POST/PUT).
  - Useful for business states (e.g., status transitions)
- **Attributes:**
  - `allowInsert`, `allowUpdate`: If `false`, field can't be set on POST/PUT—only via action.
  - `actions`: Array of `@XFieldAction` annotations

**Field Action Annotation:**
- `@XFieldAction` specifies:
  - `name`: Action name
  - `value`: Value to set field to when action is invoked
  - `message`: Success message template (optional)
  - `actionClass`: Custom logic class (optional, default is simple assignment)

#### Example

```java
@XControlledByAction(
    allowInsert = false,
    allowUpdate = false,
    actions = {
        @XFieldAction(name = "approve", value = "APPROVED", message = "Status set to {action.value} by {action.name}."),
        @XFieldAction(name = "reject", value = "REJECTED")
    }
)
private String status;
```

### 3. Custom Field Action Logic

For advanced scenarios, implement `XFieldControlledByAction` to define side effects or validations.

```java
public class StatusChangeAction implements XFieldControlledByAction {
    public Object apply(Object fieldValue, Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        if ("ACTIVE".equals(fieldValue)) {
            // trigger workflow or notification
        }
        return fieldValue;
    }
}
```

---

## Key Classes and Enums

- **XActionAbstractImpl**: Base class for action logic, provides default 405 responses for all HTTP methods; override only those you need.
- **XResourceAction**: Interface with method hooks for GET, POST, PUT, DELETE.
- **XActionType**: Enum for HTTP method types.
- **XFieldControlledByAction**: Functional interface for custom field update logic.
- **XControlledByActionType**: Enum for control logic (e.g., UPDATE, DELETE).

## REST Endpoints

Actions are exposed at:
```
${xresource.api.base-path:/api/resources}/${resourceName}/{id}/actions/{actionName}
```
Where `{actionName}` matches the declared action, and HTTP method is as specified.

---

## Relationships to Other Features

- **Resource & Metadata Management:** Actions operate on resources/entities.
- **Authentication & Access Control:** Permissions may restrict who can invoke actions.
- **Validation Framework:** Actions may leverage resource validation before execution.
- **Event Hooks & Extensibility:** Hooks can be invoked before/after actions.

---

## File Locations

- `src/main/java/org/xresource/core/actions/`
- `src/main/java/org/xresource/core/annotations/`

---

## See Also

- [Resource & Metadata Management](../resource-metadata/README.md)
- [Authentication & Access Control](../auth-access/README.md)
- [Validation Framework](../validation/README.md)
- [Event Hooks & Extensibility](../hooks/README.md)

---

_This README is part of the feature-based documentation for xresource-core-starter. For more features, see the [features directory](../)._