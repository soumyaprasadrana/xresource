package org.xresource.demo.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.xresource.core.hook.XResourceEventType;
import org.xresource.core.hook.XResourceHookRegistry;

import jakarta.annotation.PostConstruct;

@Component
public class EventHooksRegistration {

    @Autowired
    private XResourceHookRegistry xResourceHookRegistry;

    @PostConstruct
    public void registerHooks() {
        xResourceHookRegistry.registerHook("user", XResourceEventType.AFTER_CREATE, new UserCreateHook());
    }
}
