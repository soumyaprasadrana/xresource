package org.xresource.core.validation;

import java.util.*;

public class XValidatorRegistry {

    private static final Map<Class<?>, List<XValidator>> validatorMap = new HashMap<>();

    public static void register(Class<?> clazz, XValidator validator) {
        validatorMap.computeIfAbsent(clazz, k -> new ArrayList<>()).add(validator);
    }

    public static List<XValidator> getValidators(Class<?> clazz) {
        return validatorMap.getOrDefault(clazz, Collections.emptyList());
    }
}
