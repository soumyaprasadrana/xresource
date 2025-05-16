package org.xresource.core.exception;

/**
 * Thrown when an invalid configuration is detected during application startup.
 * This typically includes duplicate action names, missing required metadata,
 * or other misconfigurations in the XResource framework.
 *
 * Throwing this exception should cause application startup to fail.
 */
public class XInvalidConfigurationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public XInvalidConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public XInvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public XInvalidConfigurationException(Throwable cause) {
        super(cause);
    }
}