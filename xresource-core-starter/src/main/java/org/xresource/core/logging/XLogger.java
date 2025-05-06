package org.xresource.core.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@code XLogger} is a structured, colorized logging utility designed for the
 * XResource Core Framework.
 * It wraps Spring Boot's default SLF4J + Logback stack and enhances it with:
 * <ul>
 * <li>Customizable log levels (INFO, WARN, ERROR, DEBUG, TRACE)</li>
 * <li>Colored, emoji-enhanced log outputs for better readability</li>
 * <li>Deep object serialization support for trace-level debugging</li>
 * <li>Convenient method entry/exit logging</li>
 * <li>Configuration via {@code application.properties}</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 *   private static final XLogger log = XLogger.forClass(MyService.class);
 *   
 *   public void doWork(MyInput input) {
 *       log.enter("doWork", input);
 *       ...
 *       log.exit("doWork");
 *   }
 * }</pre>
 * 
 * <p>
 * To configure the logging level, set the following property:
 * 
 * <pre>
 * xresource.logging.level = TRACE
 * </pre>
 * 
 * <p>
 * Recommended usage includes:
 * <ul>
 * <li>Using {@code trace()} for verbose, object-level diagnostics</li>
 * <li>Using {@code enter()} and {@code exit()} in service or utility
 * methods</li>
 * <li>Using {@code success()} for clearly logging successful operations</li>
 * </ul>
 * 
 * @author soumya
 * @since xresource-core 0.1
 */
public class XLogger {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";

    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static LogLevel configuredLevel = LogLevel.INFO;

    private final Logger delegate;
    private final String tag;

    public enum LogLevel {
        ERROR, WARN, INFO, DEBUG, TRACE;

        public boolean includes(LogLevel level) {
            return this.ordinal() >= level.ordinal();
        }

        public static LogLevel fromString(String str) {
            try {
                return LogLevel.valueOf(str.toUpperCase());
            } catch (Exception e) {
                return INFO;
            }
        }
    }

    public static void configureFromSpring(Environment env) {
        String level = env.getProperty("xresource.logging.level", "INFO");
        configuredLevel = LogLevel.fromString(level);
    }

    public static XLogger forClass(Class<?> clazz) {
        return new XLogger(clazz);
    }

    private XLogger(Class<?> clazz) {
        this.delegate = LoggerFactory.getLogger(clazz);
        this.tag = clazz.getSimpleName();
    }

    private void log(String emoji, String color, String level, String message, LogLevel levelType, Object... args) {
        if (!configuredLevel.includes(levelType))
            return;
        String formattedMessage = String.format(message, args);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("%s%s [%s] [%s] (%s): %s%s",
                color, emoji, timestamp, level, tag, formattedMessage, RESET);

        switch (levelType) {
            case INFO:
                delegate.info(logMessage);
                break;
            case WARN:
                delegate.warn(logMessage);
                break;
            case ERROR:
                delegate.error(logMessage);
                break;
            case DEBUG:
                delegate.debug(logMessage);
                break;
            case TRACE:
                delegate.trace(logMessage);
                break;
            default:
                delegate.info(logMessage);
        }
    }

    public void info(String message, Object... args) {
        log("ℹ️", BLUE, "INFO", message, LogLevel.INFO, args);
    }

    public void warn(String message, Object... args) {
        log("⚠️", YELLOW, "WARN", message, LogLevel.WARN, args);
    }

    public void error(String message, Object... args) {
        log("🚨", RED, "ERROR", message, LogLevel.ERROR, args);
    }

    public void debug(String message, Object... args) {
        log("🐞", CYAN, "DEBUG", message, LogLevel.DEBUG, args);
    }

    public void success(String message, Object... args) {
        log("✅", GREEN, "SUCCESS", message, LogLevel.INFO, args);
    }

    public void trace(String message, Object... args) {
        if (!configuredLevel.includes(LogLevel.TRACE))
            return;
        log("🔍", PURPLE, "TRACE", message, LogLevel.TRACE, args);
    }

    public void trace(String context, Object object) {
        if (!configuredLevel.includes(LogLevel.TRACE))
            return;
        try {
            String serialized = objectMapper.writeValueAsString(object);
            String compositeMessage = context + ":\n" + serialized;
            log("🔍", PURPLE, "TRACE", compositeMessage, LogLevel.TRACE);
        } catch (Exception e) {
            log("🔍", PURPLE, "TRACE", context + " [Failed to serialize object: " + e.getMessage() + "]",
                    LogLevel.TRACE);
        }
    }

    public void enter(String methodName, Object... args) {
        trace("➡️ Entering method: " + methodName, args);
    }

    public void exit(String methodName, Object result, Object... args) {
        trace("⬅️ Exiting method: " + methodName + " with result:\n" + result, args);
    }
}
