# Feature: Event Hooks & Extensibility

The XResource framework enables custom, event-driven extensibility for resource lifecycle operations (create, update, delete, etc.) via a robust and type-safe hook system. Hooks allow developers to execute domain and integration logic automatically at key stages, without modifying core framework code.

---

## Core Concepts

### 1. XResourceHook Interface

- **XResourceHook**
  - Functional interface: implement `void execute(XResourceEventContext context)`.
  - Receives a context containing the resource instance, metadata, user roles, etc.
  - Used for custom logic triggered by resource lifecycle events.

```java
public interface XResourceHook {
    void execute(XResourceEventContext context);
}
```
([View source](https://github.com/soumyaprasadrana/xresource/blob/82b4120127e4e4edf7947bc6c0acca16c48894db/xresource-core-starter/src/main/java/org/xresource/core/hook/XResourceHook.java#L1-L28))

---

### 2. Hook Registration and Lifecycle

- **XResourceHookRegistry**
  - Thread-safe registry for managing hooks.
  - Register hooks per resource name and event type.
  - Hooks are executed automatically at the right lifecycle stage.

**Example:**
```java
@Autowired
private XResourceHookRegistry xResourceHookRegistry;

@PostConstruct
public void registerHooks() {
    xResourceHookRegistry.registerHook("user", XResourceEventType.AFTER_CREATE, new UserCreateHook());
}
```
([View source](https://github.com/soumyaprasadrana/xresource/blob/82b4120127e4e4edf7947bc6c0acca16c48894db/xresource-core-starter/src/main/java/org/xresource/core/hook/XResourceHookRegistry.java#L1-L64))

---

### 3. Event Types

- **XResourceEventType**
  - Enum for supported lifecycle events: BEFORE_CREATE, AFTER_CREATE, BEFORE_UPDATE, AFTER_DELETE, etc.
  - Scope hooks to fire at the correct stage.

---

### 4. Example: Custom Hook

```java
@Component
public class TeamHook implements XResourceHook {
    @Autowired
    private XResourceHookRegistry xResourceHookRegistry;
    @Autowired
    private UserRepository userRepo;

    @Override
    public void execute(XResourceEventContext context) {
        Team team = (Team) context.getResourceObject();
        Optional<List<User>> teamUsers = userRepo.findByTeam(team);
        if (teamUsers.isPresent() && !teamUsers.get().isEmpty()) {
            System.out.println("Error Users still present");
        } else {
            System.out.println("Users removed for the team");
        }
    }

    @PostConstruct
    public void register() {
        this.xResourceHookRegistry.registerHook("team", XResourceEventType.AFTER_DELETE, this);
    }
}
```
([Demo code](https://github.com/soumyaprasadrana/xresource/blob/82b4120127e4e4edf7947bc6c0acca16c48894db/xresource-demo/src/main/java/org/xresource/demo/hooks/TeamHook.java#L1-L46))

---

## Relationships to Other Features

- **Validation:**  
  Hooks can be used to enforce domain or cross-field validation beyond standard annotation-based rules.
- **Action Handling:**  
  Hooks can be combined with custom actions for even more powerful workflows.
- **Resource & Metadata Management:**  
  Hooks are registered by resource name as defined in metadata.

---

## File Locations

- `src/main/java/org/xresource/core/hook/`
- Example usage: `xresource-demo/src/main/java/org/xresource/demo/hooks/`

---

## See Also

- [Validation Framework](../validation/README.md)
- [Action Handling](../action-handling/README.md)
- [Resource & Metadata Management](../resource-metadata/README.md)

---

_Note: This summary is based on a partial code search. For more, explore the [repo hook files](https://github.com/soumyaprasadrana/xresource/search?q=hook) on GitHub._

_This README is part of the feature-based documentation for xresource-core-starter. For more features, see the [features directory](../)._