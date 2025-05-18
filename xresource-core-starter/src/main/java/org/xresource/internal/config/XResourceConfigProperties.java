package org.xresource.internal.config;

public class XResourceConfigProperties {

    public static final String BASE_PACKGE = "${xresource.scan.base-package}";
    public static final String API_BASE_PATH = "${xresource.api.base-path:/api/resources}";
    public static final String OPENAPI_ENABLE = "${xresource.openapi.enabled:true}";
    public static final String OPENAPI_BASEPATH = "${xresource.openapi.base-path:/api/xresources-openapi}";
    public static final String ENABLE_ACTION_LINKS = "${xresource.api.actions.href.enabled:true}";
    public static final String AUTO_SCAN_ENABLED = "${xresource.metadata.autoScanEnabled:true}";
    public static final String API_RESPONSE_ERROR_CONTEXT = "${xresource.api.response.error.context:false}";
}
