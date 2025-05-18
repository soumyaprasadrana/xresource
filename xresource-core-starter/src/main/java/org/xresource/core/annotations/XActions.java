package org.xresource.core.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for repeating {@link XAction} annotations on the same
 * target.
 * <p>
 * This allows multiple {@code @XAction} declarations to be applied to a single
 * entity class
 * or JPA repository. Each {@code @XAction} defines a dynamic REST action that
 * can be invoked
 * via the dynamic XResource endpoint.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * &#64;XActions({
 *     &#64;XAction(name = "approve", type = XActionType.POST, actionBeanClass = ApproveAction.class),
 *     &#64;XAction(name = "reject", type = XActionType.POST, actionBeanClass = RejectAction.class)
 * })
 * public class MyEntity { ... }
 * </pre>
 * 
 * @see XAction
 * @apiNote This annotation is part of the public API and intended for external
 *          use in defining
 *          multiple dynamic actions on an entity or repository class within the
 *          XResource framework.
 * @author soumya
 * @since xresource-core 0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XActions {
    XAction[] value();
}
