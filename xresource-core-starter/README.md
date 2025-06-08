# XResource: A Robust, Extensible Metadata-Driven REST Framework

**XResource** is a next-generation Java framework designed to supercharge the creation of RESTful APIs for metadata-rich business applications. By leveraging declarative annotations, dynamic metadata, and event-driven extensibility, XResource enables rapid development of secure, maintainable, and highly customizable resource APIs with minimal boilerplate.

---

## 🚀 Why XResource?

- **Metadata-Driven**: Describe your resources, fields, and actions using Java annotations and metadata—no repetitive controller code.
- **Extensible**: Plug in custom validation, event hooks, and business actions at every stage of the entity lifecycle.
- **Field-Level Security & Workflow**: Control not just who can access a resource, but who can change each field, and through which custom actions.
- **Declarative Actions & State Machines**: Define business processes (approve, reject, archive, etc.) as RESTful actions—no more scattered service code.
- **Event Hooks**: Inject domain or integration logic on create, update, delete, and more.
- **Validation**: Combine standard bean validation with custom, context-aware rules to ensure data integrity.
- **Developer Productivity**: Focus on business logic—XResource handles the wiring, endpoint creation, and enforcement.

---

## 🧩 Core Features

### 1. **Resource & Metadata Management**

- Annotate entity classes and repositories to describe REST resources, fields, relationships, and controls.
- Metadata powers API generation, documentation, and dynamic behaviors.

### 2. **Validation Framework**

- Supports standard annotations (`@NotNull`, `@Size`, etc.) and custom validators.
- Collects violations contextually and throws exceptions with detailed error info.
- Integrates with event lifecycle for consistent enforcement.

### 3. **Event Hooks & Extensibility**

- Register hooks on resource events (`BEFORE_CREATE`, `AFTER_DELETE`, etc.).
- Write domain logic, audit, or downstream integration code in a decoupled way.

### 4. **Action Handling**

- Define custom REST actions using `@XAction` and `@XFieldAction`.
- Attach business workflows to entities and fields (e.g., approve, reject, escalate).
- Route actions to handler beans with full HTTP method support.

### 5. **Field-Level Control**

- Use `@XControlledByAction` to restrict which fields are settable via CRUD vs. actions.
- Model complex state transitions and secure sensitive updates.

### 6. **Integration-Ready**

- Built on Spring; compatible with Spring Data, Security, and other popular libraries.
- Easily add custom code, integrate with external systems, or extend via plugins.

---

## 🛠️ Example: Defining a Resource with Actions and Hooks

```java
@Entity
@XActions({
    @XAction(name = "approve", type = XActionType.POST, actionBeanClass = ApproveAction.class),
    @XAction(name = "reject", type = XActionType.POST, actionBeanClass = RejectAction.class)
})
public class Request {
    @Id
    private Long id;

    @NotNull
    private String data;

    @XControlledByAction(allowInsert = false, allowUpdate = false, actions = {
        @XFieldAction(name = "approve", value = "APPROVED"),
        @XFieldAction(name = "reject", value = "REJECTED")
    })
    private String status;
}
```

**Custom Action Handler:**

```java
public class ApproveAction extends XActionAbstractImpl {
    @Override
    public ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request, HttpServletResponse response) {
        // Custom approval logic
        return ResponseEntity.ok("Request approved!");
    }
}
```

**Event Hook Example:**

```java
@Component
public class LoggingHook implements XResourceHook {
    @Autowired
    private XResourceHookRegistry registry;

    @PostConstruct
    public void register() {
        registry.registerHook("request", XResourceEventType.AFTER_CREATE, this);
    }

    @Override
    public void execute(XResourceEventContext context) {
        System.out.println("Resource created: " + context.getResourceObject());
    }
}
```

---

## 📚 Learn More

- [docs/validation/README.md](docs/validation/README.md) — How validation works and how to extend it.
- [docs/hooks/README.md](features/hooks/README.md) — Custom hooks and event-driven workflows.
- [docs/action-handling/README.md](features/action-handling/README.md) — Declarative action design and state machines.
- [docs/resource-metadata/README.md](docs/resource-metadata/README.md) — Resource and field modeling.
- Explore the [docs/](features/) directory for more.

---

## 🚀 Getting Started

### 📦 Step 1: Add Dependencies

#### Maven

```xml
<dependency>
    <groupId>io.github.soumyaprasadrana</groupId>
    <artifactId>xresource-starter</artifactId>
    <version>0.2</version>
</dependency>

<!-- Optional: OpenAPI support -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

#### Gradle

```kotlin
implementation("io.github.soumyaprasadrana:xresource-starter:0.2")

// Optional: OpenAPI support
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
```

---

### ⚙️ Step 2: Configure `application.properties`

To enable OpenAPI and set up base scanning:

```properties
# Enable OpenAPI integration (optional)
xresource.openapi.enabled=true

# Required: base package containing your JPA repositories
xresource.scan.base-package=com.example.myapp.repositories
```

> 📌 **Note:** Make sure all your JPA repositories are explicitly annotated with `@Repository`. This annotation is **mandatory** for the framework to discover and process them correctly.

### **Annotate Your Entities**:

Describe resources, fields, actions, and hooks.

### **Implement Custom Logic**:

Extend actions, validators, or hooks as needed.

### **Run!**

XResource auto-generates REST endpoints and enforces your business rules.

---

## ⚖️ License

[MIT License](LICENSE)

---

_The heart of XResource: Let your metadata drive your business API. Build less, deliver more, and enforce robust, secure, and testable domain logic with ease._
