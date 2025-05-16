package org.xresource.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Violation {

    private final String field;
    private final String message;
    private Throwable exception;
    private Object rejectedValue;
    private String validatorType;

}
