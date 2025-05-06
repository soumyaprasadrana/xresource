package org.xresource.core.actions;

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.xresource.core.annotation.XAction;
import org.xresource.core.actions.XActionType;
import org.xresource.core.exception.XResourceException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class XActionExecutor {

    @Autowired
    private ApplicationContext applicationContext;

    public Optional<ResponseEntity<?>> excute(XAction action, Object entity, HttpServletRequest request, HttpServletResponse response) {
        XActionType type = action.type();
        this.validateRequestMethod(action, request);
        
        Class<?> actionBeanClass = action.actionBeanClass();

        Object actionBean = applicationContext.getBean(actionBeanClass);

        try {
            Method methodToInvoke;

            switch (type) {
                case GET:
                    methodToInvoke = actionBeanClass.getMethod("handleGet", Object.class, HttpServletRequest.class, HttpServletResponse.class);
                    break;
                case POST:
                    methodToInvoke = actionBeanClass.getMethod("handlePost", Object.class, HttpServletRequest.class, HttpServletResponse.class);
                    break;
                case PUT:
                    methodToInvoke = actionBeanClass.getMethod("handlePut", Object.class, HttpServletRequest.class, HttpServletResponse.class);
                    break;
                case DELETE:
                    methodToInvoke = actionBeanClass.getMethod("handleDelete", Object.class, HttpServletRequest.class, HttpServletResponse.class);
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
            throw new XResourceException("Method for action type '" + type + "' not found in " + actionBeanClass.getSimpleName());
        } catch (Exception e) {
            throw new XResourceException("Error executing action: " + action.name());
        }
    }

    private void validateRequestMethod(XAction action, HttpServletRequest request) {
        String httpMethod = request.getMethod().toUpperCase();  // e.g., "GET", "POST"
        XActionType expectedType = action.type();
    
        switch (httpMethod) {
            case "GET":
                if (expectedType != XActionType.GET) {
                    throw new XResourceException("Invalid HTTP method: Expected " + expectedType + " but received GET");
                }
                break;
            case "POST":
                if (expectedType != XActionType.POST) {
                    throw new XResourceException("Invalid HTTP method: Expected " + expectedType + " but received POST");
                }
                break;
            case "PUT":
                if (expectedType != XActionType.PUT) {
                    throw new XResourceException("Invalid HTTP method: Expected " + expectedType + " but received PUT");
                }
                break;
            case "DELETE":
                if (expectedType != XActionType.DELETE) {
                    throw new XResourceException("Invalid HTTP method: Expected " + expectedType + " but received DELETE");
                }
                break;
            default:
                throw new XResourceException("Unsupported HTTP method: " + httpMethod);
        }
    }
    
}
