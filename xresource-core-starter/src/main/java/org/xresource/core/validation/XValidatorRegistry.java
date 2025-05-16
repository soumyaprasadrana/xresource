package org.xresource.core.validation;

import java.util.*;

public class XValidatorRegistry {

    private final Map<Class<?>, List<XValidator>> validatorMap = new HashMap<>();

    public void register(Class<?> clazz, XValidator validator) {
        validatorMap.computeIfAbsent(clazz, k -> new ArrayList<>()).add(validator);
    }

    public List<XValidator> getValidators(Class<?> clazz) {
        return validatorMap.getOrDefault(clazz, Collections.emptyList());
    }
}
