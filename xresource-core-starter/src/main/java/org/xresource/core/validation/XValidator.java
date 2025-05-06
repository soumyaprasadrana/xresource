package org.xresource.core.validation;

import org.xresource.core.exception.XValidationException;

@FunctionalInterface
public interface XValidator {
    void validate(Object entity, ValidationContext context) throws XValidationException;
}
