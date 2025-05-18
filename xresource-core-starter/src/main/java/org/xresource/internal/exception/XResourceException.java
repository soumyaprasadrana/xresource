package org.xresource.internal.exception;

public class XResourceException extends RuntimeException {

    private final String originalMessage;
    private final String customMessage;

    public XResourceException(String customMessage) {
        super(customMessage);
        this.customMessage = customMessage;
        this.originalMessage = null;
    }

    public XResourceException(String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.customMessage = customMessage;
        this.originalMessage = cause != null ? cause.getMessage() : null;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    @Override
    public String getMessage() {
        if (originalMessage != null) {
            return customMessage + " | Caused by: " + originalMessage;
        } else {
            return customMessage;
        }
    }
}
