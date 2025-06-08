package org.xresource.core.intent.core.annotations;

/**
 * Enum defining the possible sources of an IntentParameter's value.
 * 
 * - STATIC: parameter value is provided statically by the caller.
 * - USER_CONTEXT: value is derived dynamically from the logged-in user's
 * context or profile.
 * - SECURITY_PROFILE: value is derived from security or role information.
 * - REQUEST: value is extracted from the current HTTP request parameters or
 * headers.
 * 
 * @author soumya
 * @since xresource-core 0.2
 */
public enum ParamSource {
    STATIC,
    USER_CONTEXT,
    SECURITY_PROFILE,
    REQUEST
}
