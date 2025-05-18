package org.xresource.core.validation;

import java.util.*;

/**
 * Registry for managing {@link XValidator} instances associated with entity
 * classes.
 * <p>
 * This class maintains a mapping between entity types (classes) and their
 * corresponding
 * lists of validators. It allows registering multiple validators per entity
 * class and
 * retrieving all validators applicable to a given entity type.
 * <p>
 * Validators registered here are typically invoked during entity lifecycle
 * operations
 * such as CREATE, UPDATE, or DELETE to perform custom validation logic.
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * XValidatorRegistry registry = new XValidatorRegistry();
 *
 * // Register a validator for User entity
 * registry.register(User.class, new UserPassValidator());
 *
 * // Retrieve validators for User entity
 * List<XValidator> userValidators = registry.getValidators(User.class);
 * }</pre>
 *
 * <p>
 * In a Spring context, this registry can be injected as a singleton bean and
 * validators can register themselves (e.g. in a @PostConstruct method) to this
 * registry.
 *
 * @see XValidator
 * @see ValidationContext
 * @since 1.0
 */
public class XValidatorRegistry {

    private final Map<Class<?>, List<XValidator>> validatorMap = new HashMap<>();

    /**
     * Registers a validator for a specific entity class.
     * Multiple validators can be registered for the same class and
     * they will be invoked in the order of registration.
     *
     * @param clazz     The entity class for which the validator applies.
     * @param validator The {@link XValidator} instance to register.
     */
    public void register(Class<?> clazz, XValidator validator) {
        validatorMap.computeIfAbsent(clazz, k -> new ArrayList<>()).add(validator);
    }

    /**
     * Retrieves all validators registered for the specified entity class.
     * If no validators are registered for the class, returns an empty list.
     *
     * @param clazz The entity class for which to retrieve validators.
     * @return An unmodifiable list of validators registered for the class,
     *         or an empty list if none are found.
     */
    public List<XValidator> getValidators(Class<?> clazz) {
        return Collections.unmodifiableList(validatorMap.getOrDefault(clazz, Collections.emptyList()));
    }
}
