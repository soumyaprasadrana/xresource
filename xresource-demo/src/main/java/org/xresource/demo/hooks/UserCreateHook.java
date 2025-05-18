package org.xresource.demo.hooks;

import org.xresource.core.hook.XResourceEventContext;
import org.xresource.core.hook.XResourceHook;

public class UserCreateHook implements XResourceHook {

	@Override
	public void execute(XResourceEventContext context) {

		System.out.println("HOOK INVOKED");
	}

}
