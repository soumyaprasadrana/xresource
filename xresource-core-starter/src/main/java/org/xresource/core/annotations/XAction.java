package org.xresource.core.annotations;

import java.lang.annotation.*;

import org.xresource.core.actions.XActionAbstractImpl;
import org.xresource.core.actions.XActionType;

@Repeatable(XActions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XAction {
    String name();

    XActionType type();

    Class<? extends XActionAbstractImpl> actionBeanClass();

}
