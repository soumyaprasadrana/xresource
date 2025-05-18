package org.xresource.core.actions;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Internal strategy interface for defining custom behavior for HTTP
 * method-based
 * actions within the XResource framework.
 * <p>
 * This interface provides method-level hooks to override the default behavior
 * of
 * GET, POST, PUT, and DELETE operations for a given resource entity. Developers
 * should extend {@link XActionAbstractImpl} and override only the methods they
 * need, rather than implementing this interface directly.
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * Used in conjunction with the {@code @XAction} annotation to register an
 * action
 * handler for a specific HTTP method and resource.
 *
 * <pre>
 * {
 *      &#64;code
 *      &#64;XAction(name = "archive", type = XActionType.POST, actionBeanClass = ArchiveAction.class)
 *      public class ArchiveAction extends XActionAbstractImpl {
 *           @Override
 *           public ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request,
 *                     HttpServletResponse response) {
 *                // custom action logic
 *           }
 *      }
 * }
 * </pre>
 *
 * @apiNote While the framework invokes these methods based on the incoming HTTP
 *          method,
 *          it is recommended to subclass {@link XActionAbstractImpl} for more
 *          convenient defaults.
 *
 * @see XActionAbstractImpl
 * @see org.xresource.core.annotation.XAction
 * @see XActionType
 * @author soumya
 * @since xresource-core 0.1
 */
public interface XResourceAction {

     // Handle GET request
     ResponseEntity<?> handleGet(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);

     // Handle POST request
     ResponseEntity<?> handlePost(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);

     // Handle PUT request
     ResponseEntity<?> handlePut(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);

     // Handle DELETE request
     ResponseEntity<?> handleDelete(Object resourceEntity, HttpServletRequest request, HttpServletResponse response);

}
