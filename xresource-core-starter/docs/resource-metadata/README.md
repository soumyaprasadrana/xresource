# Feature: Resource & Metadata Management

This feature group describes how the `xresource-core-starter` framework exposes, configures, and manages resources (entities/repositories) and their metadata, enabling dynamic, declarative REST APIs with flexible access controls and extensibility.

---

## Core Concepts

### 1. Declaring Exposable Resources

- **@XResource**  
  Annotate a repository or entity class to expose it as a RESTful resource with CRUD endpoints.
  - **Property:** `table` — the associated database table, for documentation and mapping.
  - **Auto-scan:** When `xresource.metadata.autoScanEnabled=true` (default), all repositories are scanned and exposed unless explicitly ignored.
  - **Manual exposure:** When auto-scan is off, only classes with `@XResource`, `@XResourceExposeAsEmbeddedResource`, or `@XCronResource` are processed.

```java
@XResource(table = "users")
public interface UserRepository extends JpaRepository<User, Long> { ... }
```

- **@XResourceIgnore**  
  Explicitly prevent a repository/entity from being exposed as a REST API, even if auto-scan is enabled.
  - Metadata may still be processed for embedding or background use.

```java
@XResourceIgnore
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> { ... }
```

- **@XResourceExposeAsEmbeddedResource**  
  Mark an entity/repository for inclusion as an **embedded resource** in other resources’ metadata (e.g., for relations, OpenAPI schemas), but not directly exposed as a CRUD endpoint.

```java
@XResourceExposeAsEmbeddedResource
public class Address { ... }
```

### 2. Metadata Extension & Externalization

- **@XMetadata**  
  Attach extended or externalized metadata for entity fields via a JSON file (strict schema).
  - Can be used on entity or repository classes.
  - The referenced JSON is loaded at runtime and can override or enrich field metadata defined in Java.

**Example usage:**
```java
@XMetadata(path = "/metadata/user-fields.json")
public class User { ... }
```

**Expected JSON schema (excerpt):**
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "table": "users",
  "fields": {
    "email": {
      "label": "Email Address",
      "type": "string",
      "required": true,
      "format": "email"
    },
    "createdAt": {
      "type": "date",
      "label": "Creation Date",
      "readonly": true
    }
  }
}
```
See full schema in the JavaDoc for `@XMetadata`.

---

### 3. Access Control

- **@XResourceAccess**  
  Resource-level, role-based access control.
  - `denyRoles`: Block listed roles from all access (takes precedence).
  - `readRoles`: Allow only listed roles to perform GET.
  - `writeRoles`: Allow only listed roles to perform POST/PUT/DELETE.
  - Wildcard `"*"` is supported.

**Example:**
```java
@XResourceAccess(
    denyRoles = { "*" },                   // Deny all unless overridden
    readRoles = { "ROLE_USER", "AUDITOR" },
    writeRoles = { "ROLE_ADMIN" }
)
public class User { ... }
```

- **@XResourceCRUDAccessRestriction**  
  Deny-list CRUD operations by HTTP method and role, even if general access is allowed.

**Example:**
```java
@XResourceCRUDAccessRestriction(
    delete = { "MODERATOR", "AUDITOR" }, // Deny specific roles DELETE
    post   = { "VIEWER" }                // Deny VIEWER from creating
)
public class Document { ... }
```

- **@XFieldAccess**  
  Fine-grained, field-level role-based access control.
  - Deny or allow specific roles to read/write a field.
  - Wildcard `"*"` is supported.

**Example:**
```java
@XFieldAccess(
    denyRoles = { "*" },       // Hide from all by default
    readRoles = { "ROLE_USER" },
    writeRoles = { "ROLE_ADMIN" }
)
private String sensitiveInfo;
```

---

### 4. Field Visibility & Mutability

- **@XReadonly**  
  Mark a field as read-only (excluded from insert/update operations, included in GET responses).

- **@XHidden**  
  Hide a field from GET/read responses (but allow in POST/PUT or forms).

**Example:**
```java
@XReadonly
private LocalDateTime createdAt;

@XHidden
private String internalToken;
```

---

## Relationships to Other Features

- **Action Handling:** Actions operate on resources/entities described here.
- **Authentication & Access Control:** These annotations enforce permissions at both resource and field levels.
- **Validation Framework:** Metadata and access rules inform validation logic.
- **Event Hooks & Extensibility:** Metadata scanning enables dynamic event and hook registration.

---

## File Locations

- `src/main/java/org/xresource/core/annotations/`
- `src/main/java/org/xresource/core/model/`

---

## See Also

- [Action Handling](../action-handling/README.md)
- [Authentication & Access Control](../auth-access/README.md)
- [Validation Framework](../validation/README.md)
- [Event Hooks & Extensibility](../hooks/README.md)

---

_This README is part of the feature-based documentation for xresource-core-starter. For more features, see the [features directory](../)._