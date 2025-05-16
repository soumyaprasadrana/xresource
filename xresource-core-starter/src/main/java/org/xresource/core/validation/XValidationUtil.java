package org.xresource.core.validation;

import jakarta.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.xresource.core.annotations.XJSONFormValidatorType;
import org.xresource.core.logging.XLogger;
import org.xresource.core.model.XResourceMetadata;

public class XValidationUtil {

    public static void registerFieldValidators(Field field, Class<?> clazz, XValidatorRegistry registry, XLogger log) {
        field.setAccessible(true);
        Annotation[] annotations = field.getAnnotations();

        for (Annotation annotation : annotations) {
            XValidator validator = null;

            if (annotation instanceof NotNull) {
                validator = (entity, context) -> {
                    try {
                        Object value = field.get(entity);
                        if (value == null) {
                            context.addViolation(field.getName(), "Field '" + field.getName() + "' must not be null.",
                                    null, null, "NotNull");
                        }
                    } catch (Exception e) {
                        log.warn("NotNull validation skipped for field: " + field.getName());
                    }
                };
            } else if (annotation instanceof Size size) {
                validator = (entity, context) -> {
                    try {
                        Object value = field.get(entity);
                        if (value instanceof CharSequence seq) {
                            int len = seq.length();
                            if (len < size.min() || len > size.max()) {
                                context.addViolation(field.getName(),
                                        "Field '" + field.getName() + "' size must be between " + size.min()
                                                + " and " + size.max() + ".",
                                        null, value, "Size(Min:" + size.min() + ",Max:" + size.max() + ")");
                            }
                        } else if (value instanceof java.util.Collection<?> coll) {
                            int len = coll.size();
                            if (len < size.min() || len > size.max()) {
                                context.addViolation(field.getName(),
                                        "Collection field '" + field.getName() + "' size must be between "
                                                + size.min() + " and " + size.max() + ".",
                                        null, value,
                                        "Size(Min:" + size.min() + ",Max:" + size.max() + ")");
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Size validation skipped for field: " + field.getName());
                    }
                };
            } else if (annotation instanceof Min min) {
                validator = (entity, context) -> {
                    try {
                        Object value = field.get(entity);
                        if (value instanceof Number num && num.longValue() < min.value()) {
                            context.addViolation(field.getName(),
                                    "Field '" + field.getName() + "' must be ≥ " + min.value() + ".", null, value,
                                    "Min:" + min.value());
                        }
                    } catch (Exception e) {
                        log.warn("Min validation skipped for field: " + field.getName());
                    }
                };
            } else if (annotation instanceof Max max) {
                validator = (entity, context) -> {
                    try {
                        Object value = field.get(entity);
                        if (value instanceof Number num && num.longValue() > max.value()) {
                            context.addViolation(field.getName(),
                                    "Field '" + field.getName() + "' must be ≤ " + max.value() + ".", null, value,
                                    "Max:" + max.value());
                        }
                    } catch (Exception e) {
                        log.warn("Max validation skipped for field: " + field.getName());
                    }
                };
            } else if (annotation instanceof Pattern pattern) {
                validator = (entity, context) -> {
                    try {
                        Object value = field.get(entity);
                        if (value instanceof String str && !str.matches(pattern.regexp())) {
                            context.addViolation(field.getName(),
                                    "Field '" + field.getName() + "' does not match pattern: " + pattern.regexp(), null,
                                    value, "Pattern:" + pattern.regexp());
                        }
                    } catch (Exception e) {
                        log.warn("Pattern validation skipped for field: " + field.getName());
                    }
                };
            } else if (annotation instanceof Email) {
                validator = (entity, context) -> {
                    try {
                        Object value = field.get(entity);
                        if (value instanceof String str && !str.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                            context.addViolation(field.getName(),
                                    "Field '" + field.getName() + "' is not a valid email.", null, value, "Email");
                        }
                    } catch (Exception e) {
                        log.warn("Email validation skipped for field: " + field.getName());
                    }
                };
            }

            if (validator != null) {
                registry.register(clazz, validator);
            }
        }
    }

    public static void registerUIValidators(Field field, XResourceMetadata metadata) {
        field.setAccessible(true);
        Annotation[] annotations = field.getAnnotations();

        for (Annotation annotation : annotations) {
            Map<String, String> validatorEntry = new HashMap<>();
            validatorEntry.put("value", ""); // Default value

            if (annotation instanceof NotNull) {
                validatorEntry.put("type", XJSONFormValidatorType.REQUIRED.name());
            } else if (annotation instanceof Size size) {
                if (size.min() > 0) {
                    Map<String, String> minEntry = new HashMap<>();
                    minEntry.put("type", XJSONFormValidatorType.MIN_LENGTH.name());
                    minEntry.put("value", String.valueOf(size.min()));
                    metadata.addXJSONFormValidatorForField(field.getName(), minEntry);
                }
                if (size.max() < Integer.MAX_VALUE) {
                    Map<String, String> maxEntry = new HashMap<>();
                    maxEntry.put("type", XJSONFormValidatorType.MAX_LENGTH.name());
                    maxEntry.put("value", String.valueOf(size.max()));
                    metadata.addXJSONFormValidatorForField(field.getName(), maxEntry);
                }
                continue; // Skip default handling below
            } else if (annotation instanceof Pattern pattern) {
                validatorEntry.put("type", XJSONFormValidatorType.REGEX.name());
                validatorEntry.put("value", pattern.regexp());
            } else if (annotation instanceof Min min) {
                validatorEntry.put("type", XJSONFormValidatorType.MIN.name());
                validatorEntry.put("value", String.valueOf(min.value()));
            } else if (annotation instanceof Max max) {
                validatorEntry.put("type", XJSONFormValidatorType.MAX.name());
                validatorEntry.put("value", String.valueOf(max.value()));
            } else if (annotation instanceof Email) {
                validatorEntry.put("type", XJSONFormValidatorType.EMAIL.name());
            } else {
                continue;
            }

            metadata.addXJSONFormValidatorForField(field.getName(), validatorEntry);
        }
    }
}
