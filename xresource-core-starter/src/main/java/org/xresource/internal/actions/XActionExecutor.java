package org.xresource.internal.actions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.xresource.core.actions.XActionType;
import org.xresource.core.actions.XFieldControlledByAction;
import org.xresource.core.annotations.XAction;
import org.xresource.core.annotations.XFieldAction;
import org.xresource.core.service.XResourceService;
import org.xresource.internal.exception.XResourceException;
import org.xresource.internal.util.XFieldValueResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class XActionExecutor {

    @Autowired
    private ApplicationContext applicationContext;

    public Optional<ResponseEntity<?>> excute(XAction action, Object entity, HttpServletRequest request,
            HttpServletResponse response) {
        XActionType type = action.type();
        this.validateRequestMethod(action, request);

        Class<?> actionBeanClass = action.actionBeanClass();

        Object actionBean = applicationContext.getBean(actionBeanClass);

        try {
            Method methodToInvoke;

            switch (type) {
                case GET:
                    methodToInvoke = actionBeanClass.getMethod("handleGet", Object.class, HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                case POST:
                    methodToInvoke = actionBeanClass.getMethod("handlePost", Object.class, HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                case PUT:
                    methodToInvoke = actionBeanClass.getMethod("handlePut", Object.class, HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                case DELETE:
                    methodToInvoke = actionBeanClass.getMethod("handleDelete", Object.class, HttpServletRequest.class,
                            HttpServletResponse.class);
                    break;
                default:
                    throw new XResourceException("Unsupported action type: " + type);
            }

            Object result = methodToInvoke.invoke(actionBean, entity, request, response);

            if (result instanceof ResponseEntity) {
                return Optional.of((ResponseEntity<?>) result);
            } else {
                return Optional.empty();
            }

        } catch (NoSuchMethodException e) {
            throw new XResourceException(
                    "Method for action type '" + type + "' not found in " + actionBeanClass.getSimpleName());
        } catch (Exception e) {
            throw new XResourceException("Error executing action: " + action.name());
        }
    }

    public Optional<ResponseEntity<?>> excuteXFieldAction(XFieldAction action, Object entity,
            HttpServletRequest request,
            HttpServletResponse response, String fieldName, Field field, String resourceName, List<String> roles,
            XResourceService service) {

        try {
            String value = action.value();
            Object valueObj = XFieldValueResolver.resolveValue(field, value);
            String message = action.message();
            String[] messageArgs = action.messageArgs();
            Class<?> actionClass = action.actionClass();

            String parsedMessage = resolveMessage(entity, fieldName, action.name(), value, message, messageArgs);

            Object inputEntity = service.convertMapToEntity(resourceName, Map.of(fieldName, valueObj));

            XFieldControlledByAction instance = (XFieldControlledByAction) applicationContext.getBean(actionClass);

            Object result = instance.action(request, response, resourceName, fieldName, entity, inputEntity, roles,
                    parsedMessage);

            if (result instanceof ResponseEntity) {
                return Optional.of((ResponseEntity<?>) result);
            } else {
                return Optional.empty();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new XResourceException("Error executing action: " + action.name());
        }
    }

    private void validateRequestMethod(XAction action, HttpServletRequest request) {
        String httpMethod = request.getMethod().toUpperCase(); // e.g., "GET", "POST"
        XActionType expectedType = action.type();

        switch (httpMethod) {
            case "GET":
                if (expectedType != XActionType.GET) {
                    throw new XResourceException("Invalid HTTP method: Expected " + expectedType + " but received GET");
                }
                break;
            case "POST":
                if (expectedType != XActionType.POST) {
                    throw new XResourceException(
                            "Invalid HTTP method: Expected " + expectedType + " but received POST");
                }
                break;
            case "PUT":
                if (expectedType != XActionType.PUT) {
                    throw new XResourceException("Invalid HTTP method: Expected " + expectedType + " but received PUT");
                }
                break;
            case "DELETE":
                if (expectedType != XActionType.DELETE) {
                    throw new XResourceException(
                            "Invalid HTTP method: Expected " + expectedType + " but received DELETE");
                }
                break;
            default:
                throw new XResourceException("Unsupported HTTP method: " + httpMethod);
        }
    }

    private String resolveMessage(Object entity, String actionField, String actionName, Object actionValue,
            String message, String[] messageArgs) {
        List<Object> resolvedArgs = new ArrayList<>();

        for (String arg : messageArgs) {
            switch (arg) {
                case "action.field":
                    resolvedArgs.add(actionField);
                    break;
                case "action.name":
                    resolvedArgs.add(actionName);
                    break;
                case "action.value":
                    resolvedArgs.add(String.valueOf(actionValue));
                    break;
                default:
                    if (arg.startsWith("entity.")) {
                        String fieldName = arg.substring("entity.".length());
                        try {
                            Field field = entity.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object value = field.get(entity);
                            resolvedArgs.add(String.valueOf(value));
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            resolvedArgs.add("null"); // or handle more gracefully
                        }
                    } else {
                        resolvedArgs.add(arg); // static string
                    }
                    break;
            }
        }

        return format(message, resolvedArgs.toArray());
    }

    private String format(String template, Object[] args) {
        for (Object arg : args) {
            template = template.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
        }
        return template;
    }

}
