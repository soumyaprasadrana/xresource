package org.xresource.core.annotations;

import java.lang.annotation.*;

import org.xresource.core.actions.XActionAbstractImpl;
import org.xresource.core.actions.XActionType;

/**
 * Annotation to define a dynamic action on a JPA entity or repository class
 * within the XResource framework.
 * <p>
 * This annotation is used to declare custom actions that can be dynamically
 * invoked via REST endpoints.
 * It can be applied directly to an entity class or a corresponding JPA
 * repository class.
 * Multiple {@code @XAction} annotations can be grouped using the container
 * annotation {@link XActions}.
 * </p>
 * 
 * <p>
 * <strong>Endpoint:</strong>
 * </p>
 * 
 * <pre>{@code
 *   ${xresource.api.base-path:/api/resources}/${resourceName}/{id}/actions/{actionName}
 * }</pre>
 * 
 * <p>
 * <strong>Usage example:</strong>
 * </p>
 * 
 * <pre>{@code
 * &#64;XAction(
 *     name = "approveUser",
 *     type = XActionType.POST,
 *     actionBeanClass = ApproveUserAction.class
 * )
 * }</pre>
 *
 * <p>
 * <strong>Fields:</strong>
 * </p>
 * <ul>
 * <li><b>name</b> - The unique name of the action. This is used to construct
 * the URL for dynamic invocation.
 * <li><b>type</b> - Defines the HTTP method type (e.g., GET, POST, PUT, DELETE)
 * to be used when calling this action.</li>
 * <li><b>actionBeanClass</b> - Specifies the implementation class of the
 * action. This class must extend {@link XActionAbstractImpl}
 * and contains the logic to be executed when the action is invoked.</li>
 * </ul>
 *
 * @see XActions
 * @see XActionAbstractImpl
 * @see XActionType
 * 
 * @apiNote This annotation is part of the public API and is intended to be
 *          used by consumers of the XResource library.
 * 
 * @author soumya
 * @since xresource-core 0.1
 */
@Repeatable(XActions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XAction {

    /**
     * The unique name of the action, used in the dynamic action endpoint path.
     *
     * @return the name of the action
     */
    String name();

    /**
     * The HTTP method type for this action (e.g., GET, POST, PUT, DELETE).
     *
     * @return the type of the action
     */
    XActionType type();

    /**
     * The class containing the implementation logic for this action.
     * Must extend {@link XActionAbstractImpl}.
     *
     * @return the action implementation class
     */
    Class<? extends XActionAbstractImpl> actionBeanClass();
}
