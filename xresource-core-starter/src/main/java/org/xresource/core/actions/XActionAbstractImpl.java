package org.xresource.core.actions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Base abstract implementation of the {@link XResourceAction} interface
 * intended to be extended by user-defined actions within the XResource
 * framework.
 * <p>
 * This class provides default implementations for all supported HTTP method
 * handlers
 * (GET, POST, PUT, DELETE) that respond with HTTP 405 (Method Not Allowed).
 * <p>
 * Developers should extend this class and override only the HTTP methods
 * they intend to support for a custom {@code @XAction} definition.
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;XAction(name = "customApprove", actionBeanClass = ApproveAction.class)
 *     public class ApproveAction extends XActionAbstractImpl {
 *         @Override
 *         public ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request,
 *                 HttpServletResponse response) {
 *             // custom post logic
 *         }
 *     }
 * }
 * </pre>
 *
 * @apiNote This class should be used as the base type for any custom action
 *          declared using {@code @XAction}. Only override methods that your
 *          action needs to support; all others will return a 405 response.
 * @author soumya
 * @since xresource-core 0.1
 */
public class XActionAbstractImpl implements XResourceAction {
    /**
     * Handles HTTP GET requests for a given resource entity.
     * <p>
     * Default implementation returns 405 Method Not Allowed.
     *
     * @param resourceEntity the entity instance this action is associated with
     * @param request        the incoming HTTP request
     * @param response       the outgoing HTTP response
     * @return HTTP 405 response indicating GET is not allowed
     * @apiNote Override this method in your subclass to support GET behavior
     */
    @Override
    public ResponseEntity<?> handleGet(Object resourceEntity, HttpServletRequest request,
            HttpServletResponse response) {
        return methodNotAllowed("GET");
    }

    /**
     * Handles HTTP POST requests for a given resource entity.
     * <p>
     * Default implementation returns 405 Method Not Allowed.
     *
     * @param resourceEntity the entity instance this action is associated with
     * @param request        the incoming HTTP request
     * @param response       the outgoing HTTP response
     * @return HTTP 405 response indicating POST is not allowed
     * @apiNote Override this method in your subclass to support POST behavior
     */
    @Override
    public ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request,
            HttpServletResponse response) {
        return methodNotAllowed("POST");
    }

    /**
     * Handles HTTP PUT requests for a given resource entity.
     * <p>
     * Default implementation returns 405 Method Not Allowed.
     *
     * @param resourceEntity the entity instance this action is associated with
     * @param request        the incoming HTTP request
     * @param response       the outgoing HTTP response
     * @return HTTP 405 response indicating PUT is not allowed
     * @apiNote Override this method in your subclass to support PUT behavior
     */
    @Override
    public ResponseEntity<?> handlePut(Object resourceEntity, HttpServletRequest request,
            HttpServletResponse response) {
        return methodNotAllowed("PUT");
    }

    /**
     * Handles HTTP DELETE requests for a given resource entity.
     * <p>
     * Default implementation returns 405 Method Not Allowed.
     *
     * @param resourceEntity the entity instance this action is associated with
     * @param request        the incoming HTTP request
     * @param response       the outgoing HTTP response
     * @return HTTP 405 response indicating DELETE is not allowed
     * @apiNote Override this method in your subclass to support DELETE behavior
     */
    @Override
    public ResponseEntity<?> handleDelete(Object resourceEntity, HttpServletRequest request,
            HttpServletResponse response) {
        return methodNotAllowed("DELETE");
    }

    /**
     * Utility method to generate a 405 Method Not Allowed response.
     *
     * @param method the HTTP method that was called
     * @return ResponseEntity with status 405 and a simple message
     * @apiNote Can be used in overridden methods to explicitly block certain HTTP
     *          methods
     */
    protected ResponseEntity<?> methodNotAllowed(String method) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Method " + method + " not supported for this action.");
    }
}
