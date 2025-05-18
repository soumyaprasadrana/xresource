package org.xresource.internal.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Configuration properties for OpenAPI security integration in XResource.
 * <p>
 * Binds to properties under prefix <b>{@code xresource.openapi.auth}</b> in
 * your {@code application.properties}.
 *
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre>{@code
 * # Enable OpenAPI authentication support
 * xresource.openapi.auth.enabled=true
 *
 * # Define a JWT security scheme
 * xresource.openapi.auth.security-schemes.jwt.type=http
 * xresource.openapi.auth.security-schemes.jwt.scheme=bearer
 * xresource.openapi.auth.security-schemes.jwt.bearer-format=JWT
 *
 * # Define an OAuth2 security scheme with authorization code flow
 * xresource.openapi.auth.security-schemes.oauth2.type=oauth2
 * xresource.openapi.auth.security-schemes.oauth2.flows.authorizationCode.authorization-url=https://auth.example.com/oauth/authorize
 * xresource.openapi.auth.security-schemes.oauth2.flows.authorizationCode.token-url=https://auth.example.com/oauth/token
 * xresource.openapi.auth.security-schemes.oauth2.flows.authorizationCode.scopes.read=Grants read access
 * xresource.openapi.auth.security-schemes.oauth2.flows.authorizationCode.scopes.write=Grants write access
 *
 * # Define an API key scheme
 * xresource.openapi.auth.security-schemes.apikey.type=apiKey
 * xresource.openapi.auth.security-schemes.apikey.name=X-API-KEY
 * xresource.openapi.auth.security-schemes.apikey.in=header
 * 
 * xresource.openapi.auth.resource-security.default=jwt
 * xresource.openapi.auth.resource-security.demotable1=apikey
 * xresource.openapi.auth.resource-security.default=jwt
 * }</pre>
 */
@Configuration
@ConfigurationProperties(prefix = "xresource.openapi.auth")
public class XOpenApiAuthProperties {

    /**
     * Whether OpenAPI authentication support is enabled.
     */
    private boolean enabled = false;

    /**
     * All known security schemes (e.g., jwt, oauth2, apiKey).
     * Key is the scheme name (like 'jwt'), value is the security scheme definition.
     */
    private Map<String, SecuritySchemeDefinition> securitySchemes = new HashMap<>();

    /**
     * Mapping of resource names to security scheme keys.
     * <p>
     * Example: {@code user -> jwt, admin -> oauth2}
     * </p>
     */
    private Map<String, String> resourceSecurity = new HashMap<>();

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, SecuritySchemeDefinition> getSecuritySchemes() {
        return securitySchemes;
    }

    public void setSecuritySchemes(Map<String, SecuritySchemeDefinition> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }

    public Map<String, String> getResourceSecurity() {
        return resourceSecurity;
    }

    public void setResourceSecurity(Map<String, String> resourceSecurity) {
        this.resourceSecurity = resourceSecurity;
    }

    /**
     * Represents a security scheme such as HTTP Bearer or OAuth2.
     */
    public static class SecuritySchemeDefinition {

        /**
         * The type of security scheme: e.g., "http", "oauth2", "apiKey".
         */
        private String type;

        /**
         * The scheme for HTTP auth, e.g., "bearer".
         */
        private String scheme;

        /**
         * The format of the bearer token, typically "JWT".
         */
        private String bearerFormat;

        /**
         * Used only for API Key authentication: name of the header, query, or cookie
         * parameter.
         */
        private String name;

        /**
         * Used only for API Key authentication: where to send the key — "header",
         * "query", or "cookie".
         */
        private String in;

        /**
         * Map of OAuth2 flows (e.g., "authorizationCode", "clientCredentials").
         */
        private Map<String, OAuthFlowDefinition> flows = new HashMap<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIn() {
            return in;
        }

        public void setIn(String in) {
            this.in = in;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getBearerFormat() {
            return bearerFormat;
        }

        public void setBearerFormat(String bearerFormat) {
            this.bearerFormat = bearerFormat;
        }

        public Map<String, OAuthFlowDefinition> getFlows() {
            return flows;
        }

        public void setFlows(Map<String, OAuthFlowDefinition> flows) {
            this.flows = flows;
        }
    }

    /**
     * Represents an OAuth2 authorization flow configuration.
     */
    public static class OAuthFlowDefinition {

        /**
         * Authorization URL (used in flows like authorizationCode).
         */
        private String authorizationUrl;

        /**
         * Token URL to exchange code or credentials for tokens.
         */
        private String tokenUrl;

        /**
         * OAuth2 scopes: key is scope name, value is its description.
         */
        private Map<String, String> scopes = new HashMap<>();

        public String getAuthorizationUrl() {
            return authorizationUrl;
        }

        public void setAuthorizationUrl(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public Map<String, String> getScopes() {
            return scopes;
        }

        public void setScopes(Map<String, String> scopes) {
            this.scopes = scopes;
        }
    }
}
